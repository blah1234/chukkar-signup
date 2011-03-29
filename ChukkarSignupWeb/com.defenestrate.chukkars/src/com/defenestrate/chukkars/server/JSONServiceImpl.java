package com.defenestrate.chukkars.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.defenestrate.chukkars.shared.Player;
import com.google.gson.Gson;


public class JSONServiceImpl extends HttpServlet 
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( JSONServiceImpl.class.getName() );

	
	///////////////////////////////// METHODS //////////////////////////////////
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
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
	
	public void getAllPlayers(HttpServletRequest req, HttpServletResponse resp) 
	{
		List<Player> playersList = PlayerServiceImpl.getPlayersImpl();
		
		Gson gson = new Gson();
		String json = gson.toJson(playersList);
		
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
}