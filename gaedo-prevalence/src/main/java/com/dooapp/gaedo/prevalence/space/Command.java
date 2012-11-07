package com.dooapp.gaedo.prevalence.space;

import java.io.Serializable;

/**
 * Define a command to be executed
 * @author ndx
 *
 * @param <CommandReturnType>
 */
public interface Command<CommandReturnType, Key extends Serializable> {
	/**
	 * Execute command on the given space.
	 * May throw a CommandExecutionException
	 * @param storage
	 * @return
	 */
	public CommandReturnType execute(StorageSpace<Key> storage);
}
