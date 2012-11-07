package com.dooapp.gaedo.prevalence.space.basic;

import java.io.Serializable;
import java.util.concurrent.ScheduledExecutorService;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.ExecutionSpace;
import com.dooapp.gaedo.prevalence.space.StorageSpace;

/**
 * Space persister persists space (crazy, no ?)
 * @author ndx
 *
 */
public interface SpacePersister {
	/**
	 * Log input command, whatever it means
	 * @param <CommandReturnType>
	 * @param toLog
	 */
	public <CommandReturnType, Key extends Serializable> void logCommand(Command<CommandReturnType, Key> toLog);

	/**
	 * Restore if possible, storage space and persister configuration from its storage location
	 */
	public <Key extends Serializable> StorageSpace<Key> restore(StorageSpace<Key> storageSpace);
	
	/**
	 * Install recuring actions for this space persister on executor and using storage space
	 * @param executor executor service used to perform actions
	 * @param storageSpace storage space that will be used
	 */
	public <Key extends Serializable> void install(ScheduledExecutorService executor, StorageSpace<Key> storageSpace);
}
