package com.defenestrate.chukkars.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.defenestrate.chukkars.server.entity.CronTask;
import com.defenestrate.chukkars.server.exception.PlayerNotFoundException;
import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.Player;
import com.google.gson.Gson;


public class JSONServiceImpl extends HttpServlet
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( JSONServiceImpl.class.getName() );
	private static final String SIGNUP_CLOSED = "!!!SIGNUP_CLOSED!!!";
	private static final String PLAYER_NOT_FOUND = "!!!PLAYER_NOT_FOUND!!!";


	/////////////////////////// HttpServlet METHODS ////////////////////////////
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


	///////////////////////////////// METHODS //////////////////////////////////
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
		PrintWriter charWriter = null;

		try
		{
			charWriter = resp.getWriter();
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
		finally
		{
			if(charWriter != null)
			{
				charWriter.close();
			}
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
		Map<Day, Date> closeDateMap = LayoutConfigServiceImpl.getSignupClosedImpl();
		boolean isSignupClosed;

		if( closeDateMap.containsKey(requestDay) )
		{
			Date closeDate = closeDateMap.get(requestDay);
			Date now = new Date();
			isSignupClosed = now.after(closeDate);
		}
		else
		{
			isSignupClosed = false;
			LOG.log(
				Level.SEVERE,
				requestDay + " could not be found in the closeDateMap. This should never happen! closeDateMap:\n" + closeDateMap,
				new Throwable().fillInStackTrace() );
		}

		String respStr;

		if(!isSignupClosed)
		{
			String name = req.getParameter("_name");
			Integer numChukkars = new Integer( req.getParameter("_numChukkars") );

			Player addedPlayer = PlayerServiceImpl.addPlayerImpl(name, numChukkars, requestDay);
			List<Player> playersList = PlayerServiceImpl.getPlayersImpl();

			//this is for the bug where getExtent() in getPlayersImpl()
			//occasionally leaves out the newly added player
			if(addedPlayer != null) {
				if( !playersList.contains(addedPlayer) ) {
					//sort in ascending chronological order
					Comparator<Player> dateComp = new Comparator<Player>()
					{
						@Override
						public int compare(Player o1, Player o2)
						{
							return o1.getCreateDate().compareTo( o2.getCreateDate() );
						}
					};

					int index = Collections.binarySearch(playersList, addedPlayer, dateComp);

					if(index < 0) {
						//index = (-(insertion point) - 1);
						int insertIndex = (index + 1) * -1;
						playersList.add(insertIndex, addedPlayer);
					}
				}
			}

			List<DayTotal> totalsList = calculateGameChukkars(playersList);

			TotalsAndPlayers responseObj = new TotalsAndPlayers(totalsList, playersList, addedPlayer);

			Gson gson = new Gson();
			String json = gson.toJson(responseObj);
			respStr = json;
		}
		else
		{
			respStr = SIGNUP_CLOSED;
		}

		resp.setContentType("text/plain;charset=UTF-8");
		PrintWriter charWriter = null;

		try
		{
			charWriter = resp.getWriter();
			charWriter.write(respStr);

			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to write to the ServletResponse:\n" + respStr + "\n\n" + e.getMessage(),
				e);
		}
		finally
		{
			if(charWriter != null)
			{
				charWriter.close();
			}
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
		String respStr = null;

		try {
			Player playerToEdit = PlayerServiceImpl.getPlayerImpl(playerId);
			Map<Day, Date> closeDateMap = LayoutConfigServiceImpl.getSignupClosedImpl();
			boolean isSignupClosed;

			if( closeDateMap.containsKey(playerToEdit.getRequestDay()) )
			{
				Date closeDate = closeDateMap.get( playerToEdit.getRequestDay() );
				Date now = new Date();
				isSignupClosed = now.after(closeDate);
			}
			else
			{
				isSignupClosed = false;
				LOG.log(
					Level.SEVERE,
					playerToEdit.getRequestDay() + " could not be found in the closeDateMap. This should never happen! closeDateMap:\n" + closeDateMap,
					new Throwable().fillInStackTrace() );
			}


			if(isSignupClosed) {
				respStr = SIGNUP_CLOSED;
			}
		} catch(PlayerNotFoundException e) {
			respStr = PLAYER_NOT_FOUND;
		}



		if(respStr == null)
		{
			Integer numChukkars = new Integer( req.getParameter("_numChukkars") );

			Player editedPlayer = PlayerServiceImpl.editChukkarsImpl(playerId, numChukkars);
			List<Player> playersList = PlayerServiceImpl.getPlayersImpl();
			List<DayTotal> totalsList = calculateGameChukkars(playersList);

			TotalsAndPlayers responseObj = new TotalsAndPlayers(totalsList, playersList, editedPlayer);

			Gson gson = new Gson();
			String json = gson.toJson(responseObj);
			respStr = json;
		}

		resp.setContentType("text/plain;charset=UTF-8");
		PrintWriter charWriter = null;

		try
		{
			charWriter = resp.getWriter();
			charWriter.write(respStr);

			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to write to the ServletResponse:\n" + respStr + "\n\n" + e.getMessage(),
				e);
		}
		finally
		{
			if(charWriter != null)
			{
				charWriter.close();
			}
		}
	}

	/**
	 * Returns a list of all days marked active in the DB.
	 * An array of active day names will be written into JSON in the HTTP response.
	 * @param req
	 * @param resp
	 */
	public void getActiveDays(HttpServletRequest req, HttpServletResponse resp)
	{
		List<Day> activeDaysList = getActiveDaysImpl();

		Gson gson = new Gson();
		String json = gson.toJson(activeDaysList);

		resp.setContentType("text/plain;charset=UTF-8");
		PrintWriter charWriter = null;

		try
		{
			charWriter = resp.getWriter();
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
		finally
		{
			if(charWriter != null)
			{
				charWriter.close();
			}
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


		List<Day> enabledDaysList = getActiveDaysImpl();

		if( dayToTotalsMap.size() < enabledDaysList.size() )
		{
			//add in blank data for any days that don't have anybody signed up yet
			for(Day currDay : enabledDaysList)
			{
				if( !dayToTotalsMap.containsKey(currDay) )
				{
					dayToTotalsMap.put( currDay, new Total() );
				}
			}
		}


		List<DayTotal> totalsList = new ArrayList<DayTotal>();
		Comparator<DayTotal> naturalDayOrder = new Comparator<DayTotal>()
		{
			@Override
			public int compare(DayTotal o1, DayTotal o2)
			{
				return o1._day.compareTo(o2._day);
			}
		};


		ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
		int playersPerChukkar = Integer.parseInt( strings.getString("playersPerChukkar") );
		int minPlayersPerChukkar = Integer.parseInt( strings.getString("minPlayersPerChukkar") );

		for( Day currDay : dayToTotalsMap.keySet() )
		{
			Total currTotal = dayToTotalsMap.get(currDay);
			int numGameChukkars;

			if(currTotal._numPlayers < minPlayersPerChukkar)
			{
				numGameChukkars = 0;
			}
			else if(currTotal._numPlayers >= playersPerChukkar)
			{
				numGameChukkars = currTotal._numChukkars / playersPerChukkar;
			}
			else
			{
				numGameChukkars = currTotal._numChukkars / minPlayersPerChukkar;
			}


			DayTotal currDayTotal = new DayTotal(currDay, numGameChukkars);

			int index = Collections.binarySearch(totalsList, currDayTotal, naturalDayOrder);

			//index = (-(insertion point) - 1);
			int insertIndex = (index + 1) * -1;
			totalsList.add(insertIndex, currDayTotal);
		}

		return totalsList;
	}

	/**
	 * Looks up from the CronTask table the last time all player signup data
	 * was reset. A <code>Date</code> object will be written into JSON in the
	 * HTTP response. If the RESET task cannot be found in the CronTask table,
	 * then the current Date will be returned.
	 * @param req
	 * @param resp
	 */
	public void getResetDate(HttpServletRequest req, HttpServletResponse resp)
	{
		Date responseObj = CronServiceImpl.getTaskRunDate(CronTask.RESET);
		Gson gson = new Gson();
		String json = gson.toJson(responseObj);

		resp.setContentType("text/plain;charset=UTF-8");
		PrintWriter charWriter = null;

		try
		{
			charWriter = resp.getWriter();
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
		finally
		{
			if(charWriter != null)
			{
				charWriter.close();
			}
		}
	}

	private List<Day> getActiveDaysImpl()
	{
		Day[] allDays = Day.getAll();
		List<Day> retList = new ArrayList<Day>();

		for(Day currDay : allDays)
		{
			if( currDay.isEnabled() )
			{
				retList.add(currDay);
			}
		}

		return retList;
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