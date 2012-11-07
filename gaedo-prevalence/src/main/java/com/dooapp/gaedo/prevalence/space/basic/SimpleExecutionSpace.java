package com.dooapp.gaedo.prevalence.space.basic;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.ExecutionSpace;
import com.dooapp.gaedo.prevalence.space.PrevalenceException;
import com.dooapp.gaedo.prevalence.space.StorageSpace;

public class SimpleExecutionSpace<Key extends Serializable> implements
		ExecutionSpace<Key> {
	/**
	 * Thread factory creating only one thread
	 * 
	 * @author ndx
	 * 
	 */
	private static class UniThread implements ThreadFactory {
		private boolean created = false;

		@Override
		public Thread newThread(Runnable r) {
			if (!created) {
				synchronized (this) {
					if (!created) {
						created = true;
						return new Thread(r);
					}
				}
			}
			return null;
		}

	}

	private static class CommandExecutionException extends PrevalenceException {

		public CommandExecutionException(Command<?, ?> command, Exception e) {
			super("unable to execute command " + command, e);
		}

	}

	/**
	 * Command executor runs command on space and memorizes it for later use
	 * 
	 * @author ndx
	 * 
	 * @param <CommandReturnType>
	 */
	private class CommandExecutor<CommandReturnType extends Object> implements
			Callable<CommandReturnType> {
		private final Command<CommandReturnType, Key> toExecute;

		public CommandExecutor(Command<CommandReturnType, Key> toExecute) {
			super();
			this.toExecute = toExecute;
		}

		@Override
		public CommandReturnType call() throws Exception {
			CommandReturnType toReturn = toExecute.execute(storage);
			commandLogger.logCommand(toExecute);
			return toReturn;
		}
	}

	/**
	 * Used storage space
	 */
	private StorageSpace<Key> storage;

	/**
	 * Executor service used to ensure all commands are executed in the right
	 * order.
	 */
	private ScheduledExecutorService executor;

	private SpacePersister commandLogger;

	/**
	 * Default constructor providing sensible defaults.
	 * <ul>
	 * <li>Storage space is a {@link SimpleStorageSpace}</li>
	 * <li>Executor service is a {@link ScheduledThreadPoolExecutor} using as
	 * {@link ThreadFactory} a factory that always return null, to ensure no
	 * thread is created after the first one.</li>
	 * </ul>
	 * 
	 * @see SimpleExecutionSpace#SimpleExecutionSpace(StorageSpace,
	 *      ExecutorService)
	 */
	public SimpleExecutionSpace() {
		this(new SimpleStorageSpace<Key>(), new ScheduledThreadPoolExecutor(1,
				new UniThread()), new DefaultSpacePersister());
	}

	/**
	 * The easiest to use constructor : it creates a
	 * {@link SimpleExecutionSpace} with a given folder
	 * 
	 * @param storageDir
	 *            storage directory
	 */
	public SimpleExecutionSpace(File storageDir) {
		this(new SimpleStorageSpace<Key>(), new ScheduledThreadPoolExecutor(1,
				new UniThread()), new DefaultSpacePersister(storageDir));
	}

	/**
	 * Full configurable constructor. Notice that at construction, Three
	 * sophisticated things are done :
	 * <ul>
	 * <li>Restore storage from its persisted state, if any</li>
	 * <li>Schedule periodic execution of full storage space persistance</li>
	 * </ul>
	 * 
	 * @param storageSpace
	 *            used storage space. This space is given only as an indication,
	 *            as it should theorically be restored from storage by the space
	 *            persister
	 * @param executor
	 *            used schedulable executor
	 * @param spacePersister
	 *            used space persister
	 */
	public SimpleExecutionSpace(StorageSpace<Key> storageSpace,
			ScheduledExecutorService executor, SpacePersister spacePersister) {
		this.executor = executor;
		this.commandLogger = spacePersister;
		// Restore space from command logger
		this.storage = spacePersister.restore(storageSpace);
		// Now space is restored, install space persister execution mechanisms
		spacePersister.install(executor, this.storage);
		// this.storage = spacePersister.restore(this, storageSpace);
	}

	/**
	 * Execute given command in the associated {@link #executor} and silently
	 * throws its associated exception (if any) thanks to advices of
	 * http://blog.
	 * developpez.com/adiguba/p8434/java/unchecked-checked-exception/
	 */
	@Override
	public <CommandReturnType> CommandReturnType execute(
			Command<CommandReturnType, Key> command) {
		try {
			Future<CommandReturnType> futureResult = executor
					.submit(new CommandExecutor<CommandReturnType>(command));
			return futureResult.get();
		} catch (InterruptedException e) {
			throw new CommandExecutionException(command, e);
		} catch (ExecutionException e) {
			/*
			 * Inner process failed and must be rethrowed. We cast this
			 * exception as a Runtime one, event if it is not the case, as Java
			 * virtual machine do not handle type checking at runtime, but only
			 * at compile time.
			 */
			throw (RuntimeException) e.getCause();
		} catch (RuntimeException e) {
			throw e;
		}
	}
}
