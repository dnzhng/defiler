package dblockcache;

import java.io.IOException;

import common.Constants;
import common.Constants.DiskOperationType;
import virtualdisk.VD;

public class Buffer extends DBuffer {
	
	private int _blockID; //the ID used to "getBuffer"
	private boolean _isDirty; //Check if the buffer is dirty or not
	public boolean _isBusy; //Check if the block is busy doing I/O stuff
	private boolean _isValid; //Check if the block has contents in it
	private byte[] _buffer;  //The actual buffer 
	private VD _virtualdisk;
	
	public Buffer (int blockID, VD disk) {
		_blockID = blockID;
		_virtualdisk = disk;
		_buffer = new byte[Constants.BLOCK_SIZE];
		_isDirty = false;
		_isBusy = false;
		_isValid = false; 
	}
	

	@Override
	public void startFetch() {
		// TODO Auto-generated method stub
		_isValid = false;
		_isBusy = true;
		
		try {
			_virtualdisk.startRequest(this, DiskOperationType.READ);
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void startPush() {
		// TODO Auto-generated method stub
		_isValid = false;
		_isBusy = true;
		
		try {
			_virtualdisk.startRequest(this, DiskOperationType.WRITE);
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkValid() {
		// TODO Auto-generated method stub
		return _isValid;
	}

	@Override
	public synchronized boolean waitValid() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return !_isDirty;
	}

	@Override
	public synchronized boolean waitClean() {
		// TODO Auto-generated method stub
		while (_isDirty) {
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
	public boolean isBusy() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		int byteCount = count;
		_isBusy = true; 
		
		//Check possible errors errors:
		if (startOffset < 0 || startOffset > buffer.length 
				|| startOffset + count > buffer.length) return -1;
	
		while(!_isValid) {
			waitValid();
		}
		
		for (int i = startOffset; i < startOffset + count; i++) {
			if (i > _buffer.length) return i - startOffset; //end of the buffer, can only read so much
			buffer[i] = _buffer[i - startOffset];
		}
		
		notifyAll();
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
		// TODO Auto-generated method stub
		int byteCount = count;
		_isBusy = true;
		
		if (startOffset < 0 || startOffset > buffer.length
				|| startOffset + count > buffer.length) return -1;
		
		while(!_isValid) {
			waitValid();
		}
		
		for (int i = startOffset; i < startOffset + count; i++) {
			if (i > _buffer.length) return i - startOffset;
			_buffer[i - startOffset] = buffer[i];
		}
		
		_isDirty = true;
		notifyAll();
		return byteCount;
	}

	@Override
	public synchronized void ioComplete() {
		// TODO Auto-generated method stub
		_isValid = true;
		_isBusy = false; 
		_isDirty = false;
		notifyAll();
		
	}

	@Override
	public int getBlockID() {
		// TODO Auto-generated method stub
		return _blockID;
	}

	@Override
	public byte[] getBuffer() {
		// TODO Auto-generated method stub
		return _buffer;
	}

}
