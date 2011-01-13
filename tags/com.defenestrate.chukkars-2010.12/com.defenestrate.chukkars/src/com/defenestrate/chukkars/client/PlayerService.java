package com.defenestrate.chukkars.client;

import java.util.List;

import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.Player;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("player")
public interface PlayerService extends RemoteService 
{
	/**
	 * Adds a player
	 * @param name name of the player
	 * @param numChukkars number of chukkars the player is requesting
	 * @return persisted Player object
	 */
	Player addPlayer(String name, Integer numChukkars, Day requestDay);

	/**
	 * Edits the number of chukkars for a player 
	 * @param playerId persistence store Id of the player
	 * @param numChukkars new number of chukkars for the player
	 */
	void editChukkars(Long playerId, Integer numChukkars);
	
	void removePlayer(Long id);
	List<Player> getPlayers();
}