package dblockcache;

import java.io.IOException;

import common.Constants;
import common.Constants.DiskOperationType;
import virtualdisk.IVirtualDisk;

public class Buffer extends DBuffer {

	private int _blockID; // the ID used to "getBuffer"
	private boolean _isDirty; // Check if the buffer is dirty or not
	private boolean _isBusy; // Check if the block is busy doing I/O stuff
	private boolean _isValid; // Check if the block has contents in it
	private byte[] _buffer; // The actual buffer
	private IVirtualDisk _virtualdisk;

	public Buffer(int blockID, IVirtualDisk virtualdisk) {
		assert(blockID >=0);
		_blockID = blockID;
		_virtualdisk = virtualdisk;
		_buffer = new byte[Constants.BLOCK_SIZE];
		_isDirty = false;
		_isBusy = false;
		_isValid = false;
	}

	@Override
	public void startFetch() {
		_isValid = false;
		_isBusy = true;

		try {
			_virtualdisk.startRequest(this, DiskOperationType.READ);
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startPush() {
		//_isValid = false;
		// pushing assumes that the block is valid
		_isBusy = true;

		try {
			_virtualdisk.startRequest(this, DiskOperationType.WRITE);
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkValid() {
		return _isValid;
	}

	@Override
	public synchronized boolean waitValid() {
		while (!_isValid) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean checkClean() {
		return !_isDirty;
	}

	@Override
	public synchronized boolean waitClean() {
		while (_isDirty) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean isBusy() {
		return _isBusy;
	}

	/*
	 * reads into the buffer[] array from the contents of the DBuffer. Check
	 * first that the DBuffer has a valid copy of the data! startOffset and
	 * count are for the buffer array, not the DBuffer. Upon an error, it should
	 * return -1, otherwise return number of bytes read.
	 */
	@Override
	public synchronized int read(byte[] buffer, int startOffset, int count) {
		int byteCount = count;
		_isBusy = true;

		// Check possible errors errors:
		if (startOffset < 0 || startOffset > buffer.length
				|| startOffset + count > buffer.length)
			return -1;

		waitValid();

		for (int i = startOffset; i < startOffset + count; i++) {
			if (i > _buffer.length) {
				// end of the buffer, can only read so much
				return i - startOffset; 

			}
			buffer[i] = _buffer[i - startOffset];
		}

		notifyAll();
		// TODO: set _isBusy back to false?
		_isBusy = false;
		return byteCount;
	}

	/*
	 * writes into the DBuffer from the contents of buffer[] array. startOffset
	 * and count are for the buffer array, not the DBuffer. Mark buffer dirty!
	 * Upon an error, it should return -1, otherwise return number of bytes
	 * written.
	 */

	@Override
	public synchronized int write(byte[] buffer, int startOffset, int count) {
		int byteCount = count;
		_isBusy = true;

		if (startOffset < 0 || startOffset > buffer.length
				|| startOffset + count > buffer.length)
			return -1;

		waitValid();
		
		for (int i = startOffset; i < startOffset + count; i++) {
			if (i > _buffer.length) {
				return i - startOffset;
			}
			_buffer[i - startOffset] = buffer[i];
		}

		_isDirty = true;
		notifyAll();
		// TODO: set _isBusy back to false?
		_isBusy = false;
		return byteCount;
	}

	@Override
	public synchronized void ioComplete() {
		_isValid = true;
		_isBusy = false;
		_isDirty = false;
		notifyAll();

	}

	@Override
	public int getBlockID() {
		return _blockID;
	}

	@Override
	public byte[] getBuffer() {
		return _buffer;
	}

}
