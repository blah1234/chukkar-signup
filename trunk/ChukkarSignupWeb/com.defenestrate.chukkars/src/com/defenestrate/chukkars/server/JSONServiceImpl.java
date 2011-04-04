package com.defenestrate.chukkars.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.Player;
import com.google.gson.Gson;


public class JSONServiceImpl extends HttpServlet 
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( JSONServiceImpl.class.getName() );

	
	///////////////////////////////// METHODS //////////////////////////////////
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws ServletException, java.io.IOException
	{
		handleRequest(req, resp);
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) 
		throws ServletException, java.io.IOException
	{
		handleRequest(req, resp);
	}
	
	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, java.io.IOException
	{
		String path = req.getPathInfo();
		if( path.startsWith("/") )
		{
			path = path.substring(1);
		}
		
		try
		{
			Method invokeMethod = this.getClass().getMethod(
				path, 
				HttpServletRequest.class, 
				HttpServletResponse.class);
			
			invokeMethod.invoke(this, req, resp);
		}
		catch(NoSuchMethodException e)
		{
			throw new ServletException(e);
		}
		catch(InvocationTargetException e)
		{
			throw new ServletException(e);
		}
		catch(IllegalAccessException e)
		{
			throw new ServletException(e);
		}
	}
	
	/**
	 * Returns a list of all players and a list of total game chukkars by day.
	 * A <code>TotalsAndPlayers</code> object will be written into JSON in the
	 * HTTP response.
	 * @param req
	 * @param resp
	 */
	public void getAllPlayers(HttpServletRequest req, HttpServletResponse resp) 
	{
		List<Player> playersList = PlayerServiceImpl.getPlayersImpl();
		List<DayTotal> totalsList = calculateGameChukkars(playersList);
		TotalsAndPlayers responseObj = new TotalsAndPlayers(totalsList, playersList);
		
		Gson gson = new Gson();
		String json = gson.toJson(responseObj);
		
		resp.setContentType("text/plain;charset=UTF-8");
		
		try
		{
			PrintWriter charWriter = resp.getWriter();
			charWriter.write(json);
			
			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
		{
			LOG.log(
				Level.SEVERE, 
				"Error encountered trying to write to the ServletResponse:\n" + json + "\n\n" + e.getMessage(), 
				e);
		}
	}
	
	/**
	 * Add a new player's name, requested play day, and requested number of 
	 * chukkars to the signup. Returns a list of all players and a list of total 
	 * game chukkars by day. A <code>TotalsAndPlayers</code> object will be 
	 * written into JSON in the HTTP response.
	 * @param req
	 * @param resp
	 */
	public void addPlayer(HttpServletRequest req, HttpServletResponse resp) 
	{
		Day requestDay = Day.valueOf( req.getParameter("_requestDay") );
		String name = req.getParameter("_name");
		Integer numChukkars = new Integer( req.getParameter("_numChukkars") );
		
		Player addedPlayer = PlayerServiceImpl.addPlayerImpl(name, numChukkars, requestDay);
		List<Player> playersList = PlayerServiceImpl.getPlayersImpl();
		List<DayTotal> totalsList = calculateGameChukkars(playersList);
		
		TotalsAndPlayers responseObj = new TotalsAndPlayers(totalsList, playersList, addedPlayer);
		
		Gson gson = new Gson();
		String json = gson.toJson(responseObj);
		
		resp.setContentType("text/plain;charset=UTF-8");
		
		try
		{
			PrintWriter charWriter = resp.getWriter();
			charWriter.write(json);
			
			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
		{
			LOG.log(
				Level.SEVERE, 
				"Error encountered trying to write to the ServletResponse:\n" + json + "\n\n" + e.getMessage(), 
				e);
		}
	}
	
	/**
	 * Looks up an existing player and edits the number of chukkars signed up.
	 * Returns a list of all players and a list of total game chukkars by day.
	 * A <code>TotalsAndPlayers</code> object will be written into JSON in the
	 * HTTP response.
	 * @param req
	 * @param resp
	 */
	public void editChukkars(HttpServletRequest req, HttpServletResponse resp) 
	{
		Long playerId = new Long( req.getParameter("_id") );
		Integer numChukkars = new Integer( req.getParameter("_numChukkars") );
		
		PlayerServiceImpl.editChukkarsImpl(playerId, numChukkars);
		List<Player> playersList = PlayerServiceImpl.getPlayersImpl();
		List<DayTotal> totalsList = calculateGameChukkars(playersList);
		
		TotalsAndPlayers responseObj = new TotalsAndPlayers(totalsList, playersList);
		
		Gson gson = new Gson();
		String json = gson.toJson(responseObj);
		
		resp.setContentType("text/plain;charset=UTF-8");
		
		try
		{
			PrintWriter charWriter = resp.getWriter();
			charWriter.write(json);
			
			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
		{
			LOG.log(
				Level.SEVERE, 
				"Error encountered trying to write to the ServletResponse:\n" + json + "\n\n" + e.getMessage(), 
				e);
		}
	}
	
	private List<DayTotal> calculateGameChukkars(List<Player> playersList)
	{
		Map<Day, Total> dayToTotalsMap = new HashMap<Day, Total>();
		
		for(Player currPlayer : playersList)
		{
			Total currTotal;
			
			if( dayToTotalsMap.containsKey(currPlayer.getRequestDay()) )
			{
				currTotal = dayToTotalsMap.get( currPlayer.getRequestDay() );
			}
			else
			{
				currTotal = new Total();
				dayToTotalsMap.put(currPlayer.getRequestDay(), currTotal);
			}
			
			if(currPlayer.getChukkarCount() > 0)
			{
				currTotal._numPlayers++;
			}
			
			currTotal._numChukkars += currPlayer.getChukkarCount();
		}
		
		
		List<DayTotal> totalsList = new ArrayList<DayTotal>();
		
		for( Day currDay : dayToTotalsMap.keySet() )
		{
			Total currTotal = dayToTotalsMap.get(currDay);
			int numGameChukkars;
				
			if(currTotal._numPlayers < 4)
			{
				numGameChukkars = 0;
			}
			else if(currTotal._numPlayers >= 6)
			{
				numGameChukkars = currTotal._numChukkars / 6;
			}
			else
			{
				numGameChukkars = currTotal._numChukkars / 4;
			}
			
			
			DayTotal currDayTotal = new DayTotal(currDay, numGameChukkars);
			
			int index = Collections.binarySearch(totalsList, currDayTotal, new Comparator<DayTotal>()
			{
				public int compare(DayTotal o1, DayTotal o2)
				{
					return o1._day.compareTo(o2._day);
				}
			});
			
			//index = (-(insertion point) - 1);
			int insertIndex = (index + 1) * -1;
			totalsList.add(insertIndex, currDayTotal);
		}
		
		return totalsList;
	}
	

	////////////////////////////// INNER CLASSES ///////////////////////////////
	private class Total
	{
		int _numPlayers = 0;
		int _numChukkars = 0;
	}
	
	private class DayTotal
	{
		Day _day;
		int _numGameChukkars;
		
		
		DayTotal(Day day, int numGameChukkars)
		{
			_day = day;
			_numGameChukkars = numGameChukkars;
		}
	}
	
	private class TotalsAndPlayers
	{
		List<DayTotal> _totalsList;
		List<Player> _playersList;
		Player _currPersisted;
		
		
		TotalsAndPlayers(List<DayTotal> totalsList, List<Player> playersList)
		{
			this(totalsList, playersList, null);
		}
		
		TotalsAndPlayers(List<DayTotal> totalsList, List<Player> playersList, Player currPersisted)
		{
			_totalsList = totalsList;
			_playersList = playersList;
			_currPersisted = currPersisted;
		}
	}
}