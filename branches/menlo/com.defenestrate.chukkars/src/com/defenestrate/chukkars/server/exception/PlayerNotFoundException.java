package com.defenestrate.chukkars.server.exception;

public class PlayerNotFoundException extends Exception {
	public Long mRequestedId;

	public PlayerNotFoundException(Long requestedId) {
		mRequestedId = requestedId;
	}
}
