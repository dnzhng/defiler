package defiler;

import dblockcache.DBuffer;

public class defilerDBuffer extends DBuffer{
	
	public void startFetch() {
		// Start an asynchronous fetch of associated block from the volume
	}
	
	public void startPush() {
		// Start an asynchronous write of buffer contents to block on volume
	}
	
	public boolean checkValid() {
		/*
		 * Check whether the buffer has valid data
		 * Examples of invalid:
		 * - Matching DFileID
		 * - Each size must be legal
		 * - Block maps of all dfiles have a valid block number for every block in dfile
		 */
	}
	
	public boolean waitValid() {
		/*
		 * Wait until the buffer is clean, wait for push to complete
		 */
	}
	
	public boolean checkClean() {
		/*
		 * Check whether the buffer is dirty
		 */
	}

	
	public boolean isBusy() {
		/*
		 * Check if buffer is evictable
		 */
	}
	
	public int read(byte[] ubuffer, int startOffset, int count) {
		//Reads
	}
	
	public int write(byte[] ubuffer, int startOffset, int count) {
		//writes
	}

}
