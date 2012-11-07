package com.dooapp.gaedo.prevalence.space.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.ExecutionSpace;
import com.dooapp.gaedo.prevalence.space.StorageSpace;

/**
 * Default implementation of space persister
 * 
 * @author ndx
 * 
 */
public class DefaultSpacePersister implements SpacePersister {
	private static final String SPACE_NAME = "space.serialized";

	private static final Logger logger = Logger
			.getLogger(DefaultSpacePersister.class.getName());
	
	private class StorageSpaceSynchronizer<Key extends Serializable> implements Runnable {

		private StorageSpace<Key> storageSpace;

		public StorageSpaceSynchronizer(StorageSpace<Key> storageSpace) {
			this.storageSpace = storageSpace;
		}

		@Override
		public void run() {
			save(storageSpace);
		}
		
	}

	/**
	 * Some config informations that will be stored
	 */
	private DefaultSpacePersisterConfig config = new DefaultSpacePersisterConfig();

	/**
	 * Directory where all commands should be logged and where space will be
	 * persisted
	 */
	private File storageDirectory;

	public DefaultSpacePersister() {
		this(new File(".", DefaultSpacePersister.class.getSimpleName()));
	}

	public DefaultSpacePersister(File storageDirectory) {
		super();
		setStorageDirectory(storageDirectory);
	}

	/**
	 * Formatter used to generate file name from command index
	 */
	private static transient DecimalFormat format = new DecimalFormat("0");

	/**
	 * Logs command to a file named from commandIndex. Notice only Commands
	 * implementing the Serializable interface will be logged, as the other ones
	 * don't need to be logged.
	 * 
	 * @param toLog
	 *            command to log
	 */
	@Override
	public <CommandReturnType, Key extends Serializable> void logCommand(
			Command<CommandReturnType, Key> toLog) {
		if (toLog instanceof Serializable) {
			// This is a kind of double effect synchronized locking, as all code
			// from this class should only be called by a specific executor
			// service
			synchronized (this) {
				writeCommand(config.getCommandIndexAndIncrement(), toLog);
			}
		}
	}

	protected <CommandReturnType, Key extends Serializable> void writeCommand(
			long commandIndex, Command<CommandReturnType, Key> command) {
		File toRead = buildCommandLogFileName(commandIndex);
		ObjectOutputStream objectStream = null;
		try {
			objectStream = new ObjectOutputStream(new FileOutputStream(toRead));
			objectStream.writeObject(command);
		} catch (IOException e) {
			throw new ConsistencyException(e);
		} finally {
			if (objectStream != null) {
				try {
					objectStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Read a given command
	 * 
	 * @param <CommandReturnType>
	 * @param <Key>
	 * @param commandIndex
	 * @return
	 */
	protected <CommandReturnType, Key extends Serializable> Command<CommandReturnType, Key> readCommand(
			long commandIndex) {
		File toRead = buildCommandLogFileName(commandIndex);
		ObjectInputStream objectStream = null;
		try {
			objectStream = new ObjectInputStream(new FileInputStream(toRead));

			return (Command<CommandReturnType, Key>) objectStream.readObject();
		} catch (IOException e) {
			throw new ConsistencyException(e);
		} catch (ClassNotFoundException e) {
			throw new ConsistencyException(e);
		} finally {
			if (objectStream != null) {
				try {
					objectStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Load a collection of command from a collection of command log files.
	 * 
	 * @return
	 */
	<Key extends Serializable> Collection<Command<?, Key>> loadCommands(
			long low, long high) {
		Collection<Command<?, Key>> returned = new LinkedList<Command<?, Key>>();
		for (long index = low; index < high; index++) {
			if (buildCommandLogFileName(index).exists()) {
				// Weird generics bug, no ? Will solve it later
				returned.add((Command<?, Key>) readCommand(index));
			} else {
				// If command does not exists, stop here, as next commands won't
				// be appliable
				break;
			}
		}
		return returned;
	}

	public File getStorageDirectory() {
		return storageDirectory;
	}

	/**
	 * Sets {@link #storageDirectory}. notice that if argument is not a
	 * directory, its parent will be used instead.
	 * 
	 * @param storageDirectory
	 */
	public void setStorageDirectory(File storageDirectory) {
		if (!storageDirectory.exists()) {
			storageDirectory.mkdirs();
		}
		if (!storageDirectory.isDirectory()) {
			storageDirectory = storageDirectory.getParentFile();
		}
		this.storageDirectory = storageDirectory;
	}

	/**
	 * Get a command log File object from the {@link #commandIndex} and
	 * {@link #storageDirectory}
	 * 
	 * @return {@link #storageDirectory}+"/command_"+{@link #commandIndex}
	 *         +".log"
	 */
	File buildCommandLogFileName(long commandIndex) {
		return new File(getStorageDirectory(), format.format(commandIndex)
				+ ".commandLog");
	}

	public long getCommandIndex() {
		return config.getCommandIndex();
	}

	/**
	 * Get file name holding all space content
	 * 
	 * @return
	 */
	File getSpaceFile() {
		return new File(getStorageDirectory(), SPACE_NAME);
	}

	/**
	 * When trying to restore, we first try to load file given by
	 * {@link #getSpaceFile()}, create an object input stream on it, and read it
	 * content.
	 * 
	 * @param storageSpace
	 *            default storage space is none was memorized
	 * @return used storage space
	 */
	@Override
	public <Key extends Serializable> StorageSpace<Key> restore(
			StorageSpace<Key> storageSpace) {
		StorageSpace<Key> returned = storageSpace;
		File spaceFile = getSpaceFile();
		if (spaceFile.exists()) {
			ObjectInputStream stream = null;
			try {
				stream = new ObjectInputStream(
						new FileInputStream(spaceFile));
				// First object to read is space persister config
				config = (DefaultSpacePersisterConfig) stream.readObject();
				// Then we have to read full space data
				returned = (StorageSpace<Key>) stream
						.readObject();
			} catch (Exception e) {
				logger.log(Level.INFO, "space was not readable", e);
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// Now, load and apply all remaining commands
		Collection<Command<?, Key>> toApply = loadCommands(
				config.getSpacePersistedAt(), Long.MAX_VALUE);
		config.setCommandIndex(config.getSpacePersistedAt()+toApply.size());
		for (Command<?, Key> command : toApply) {
			command.execute(returned);
		}
		// Finally, re-persist all to have a compressed and consistent space
		save(returned);
		return returned;
	}

	/**
	 * Save storage space and all required data
	 */
	public <Key extends Serializable> void save(StorageSpace<Key> storageSpace) {
		// Now remove all command logs
		for(long index=config.getSpacePersistedAt(); index<=config.getCommandIndex(); index++) {
			buildCommandLogFileName(index).delete();
			config.setSpacePersistedAt(index);
			updateSpaceFile(storageSpace);
		}
	}

	/**
	 * Update space file with new values of both config and storageSapce
	 * @param <Key>
	 * @param storageSpace
	 */
	private <Key extends Serializable> void updateSpaceFile(StorageSpace<Key> storageSpace) {
		File spaceFile = getSpaceFile();
		ObjectOutputStream persister = null;
		try {
			persister = new ObjectOutputStream(new FileOutputStream(spaceFile));
			persister.writeObject(config);
			persister.writeObject(storageSpace);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				persister.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Each second, the storage space will be resynchronized on disk
	 */
	@Override
	public <Key extends Serializable> void install(
			ScheduledExecutorService executor, StorageSpace<Key> storageSpace) {
		executor.scheduleWithFixedDelay(new StorageSpaceSynchronizer<Key>(storageSpace), 1, 1, TimeUnit.SECONDS);
	}
}
