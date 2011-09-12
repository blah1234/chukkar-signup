package com.defenestrate.chukkars.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import com.defenestrate.chukkars.client.PlayerService;
import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.Player;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class PlayerServiceImpl extends RemoteServiceServlet
							   implements PlayerService
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( PlayerServiceImpl.class.getName() );


	///////////////////////////////// METHODS //////////////////////////////////
	/**
	 * Adds a player
	 * @param name name of the player
	 * @param numChukkars number of chukkars the player is requesting
	 * @return persisted Player object
	 */
	@Override
	public Player addPlayer(String name, Integer numChukkars, Day requestDay)
	{
		return addPlayerImpl(name, numChukkars, requestDay);
	}

	static protected Player addPlayerImpl(String name, Integer numChukkars, Day requestDay)
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

		Player currPlayer = null;
		Transaction tx = pm.currentTransaction();

		try
		{
			tx.begin();

			currPlayer = new Player(name, numChukkars, requestDay);
			pm.makePersistent(currPlayer);

			tx.commit();
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to add a player (" + name + "; " + numChukkars + "; " + requestDay + ")",
				e);

			if( tx.isActive() )
		    {
		        tx.rollback();
		    }

		    throw new RuntimeException(e);
		}
		finally
		{
			pm.close();
		}

		return currPlayer;
	}

	@Override
	public void editChukkars(Long playerId, Integer numChukkars)
	{
		editChukkarsImpl(playerId, numChukkars);
	}

	static protected Player editChukkarsImpl(Long playerId, Integer numChukkars)
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

		Player editPlayer = null;
		Transaction tx = pm.currentTransaction();

		try
		{
		    tx.begin();

		    editPlayer = pm.getObjectById(Player.class, playerId);

			if(editPlayer != null)
			{
				editPlayer.setChukkarCount(numChukkars);
			}
			else
			{
				throw new IllegalArgumentException("Player not found with id = " + playerId);
			}

		    tx.commit();
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to modify number of chukkars for player with id = " + playerId + ":\n" + e.getMessage(),
				e);

		    if( tx.isActive() )
		    {
		        tx.rollback();
		    }

		    throw new RuntimeException(e);
		}
		finally
		{
			pm.close();
		}

		return editPlayer;
	}

	@Override
	public void removePlayer(Long id)
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

		Transaction tx = pm.currentTransaction();

		try
		{
			tx.begin();

			Player delPlayer = pm.getObjectById(Player.class, id);

			if(delPlayer != null)
			{
				pm.deletePersistent(delPlayer);
			}
			else
			{
				throw new IllegalArgumentException("Player not found with id = " + id);
			}

			tx.commit();
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to remove player with id = " + id + ":\n" + e.getMessage(),
				e);

		    if( tx.isActive() )
		    {
		        tx.rollback();
		    }

		    throw new RuntimeException(e);
		}
		finally
		{
			pm.close();
		}
	}

	@Override
	public List<Player> getPlayers()
	{
		return getPlayersImpl();
	}

	static protected List<Player> getPlayersImpl()
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

		List<Player> retList = new ArrayList<Player>();

		try
		{
			Extent<Player> e = pm.getExtent(Player.class);
			Iterator<Player> iter = e.iterator();

			//sort in ascending chronological order
			Comparator<Player> dateComp = new Comparator<Player>()
			{
				@Override
				public int compare(Player o1, Player o2)
				{
					return o1.getCreateDate().compareTo( o2.getCreateDate() );
				}
			};

			while( iter.hasNext() )
			{
				Player currPlayer = iter.next();

				int index = Collections.binarySearch(retList, currPlayer, dateComp);

				//index = (-(insertion point) - 1);
				int insertIndex = (index + 1) * -1;
				retList.add(insertIndex, currPlayer);
			}
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to get all players:\n" + e.getMessage(),
				e);

		    throw new RuntimeException(e);
		}
		finally
		{
			pm.close();
		}

		return retList;
	}
}