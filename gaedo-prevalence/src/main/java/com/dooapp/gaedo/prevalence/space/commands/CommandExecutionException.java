package com.dooapp.gaedo.prevalence.space.commands;

import com.dooapp.gaedo.prevalence.space.PrevalenceException;

public abstract class CommandExecutionException extends PrevalenceException {

	public CommandExecutionException() {
	}

	public CommandExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandExecutionException(String message) {
		super(message);
	}

	public CommandExecutionException(Throwable cause) {
		super(cause);
	}

}
