package com.dooapp.gaedo.prevalence.space.basic;

import java.io.Serializable;

/**
 * A bean holding all default space persister config
 * @author ndx
 *
 */
public class DefaultSpacePersisterConfig implements Serializable {

	/**
	 * Index of the last executed command, used to generate a file name (with
	 * the right DecimalFormat)
	 */
	private long commandIndex = 0;

	/**
	 * Index of the last command after which a space persistence file was
	 * writtent. This index is used to reconstruct the space at startup, and to
	 * delete log files
	 */
	private long spacePersistedAt = 0;

	public long getCommandIndex() {
		return commandIndex;
	}

	public void setCommandIndex(long commandIndex) {
		this.commandIndex = commandIndex;
	}

	public long getSpacePersistedAt() {
		return spacePersistedAt;
	}

	public void setSpacePersistedAt(long spacePersistedAt) {
		this.spacePersistedAt = spacePersistedAt;
	}

	public long getCommandIndexAndIncrement() {
		return commandIndex++;
	}
}