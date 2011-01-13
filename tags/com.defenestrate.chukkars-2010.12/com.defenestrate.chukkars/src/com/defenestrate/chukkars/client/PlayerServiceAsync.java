package com.defenestrate.chukkars.client;

import java.util.List;

import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.Player;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PlayerServiceAsync 
{
	void addPlayer(String name, Integer numChukkars, Day requestDay, AsyncCallback<Player> async);
	void editChukkars(Long playerId, Integer numChukkars, AsyncCallback<Void> async);
	void removePlayer(Long id, AsyncCallback<Void> async);
	void getPlayers(AsyncCallback<List<Player>> async);
}