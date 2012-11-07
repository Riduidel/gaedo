package com.dooapp.gaedo.prevalence.space;

import java.io.Serializable;

/**
 * This one is the visible space, on which commands are to be applied.
 * @author ndx
 *
 */
public interface ExecutionSpace<Key extends Serializable> {
	/**
	 * Execute command and returns the execution result, whichever it is.
	 * Execution order of these commands must be guaranteed.
	 * @param <CommandReturnType> data returned by command
	 * @param command command to execute
	 * @return the return value of the command
	 */
	public <CommandReturnType> CommandReturnType execute(Command<CommandReturnType, Key> command);
}
