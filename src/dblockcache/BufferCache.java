package dblockcache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


import virtualdisk.VD;
import virtualdisk.VirtualDisk;

public class BufferCache extends DBufferCache {
	
	private VirtualDisk _virtualdisk;
	private int _blockcount;
	private int _maxblockcount;
	private ArrayList<DBuffer> _bufferlist;
	private Set<DBuffer> _heldlist;

	// maybe make filename + format instead of VD
	
	public BufferCache(int cacheSize, VD disk) {
		// 		_cacheSize = cacheSize * Constants.BLOCK_SIZE; This is what happens in the super?? 
			super(cacheSize);
		// TODO Auto-generated constructor stub
		_virtualdisk = disk;
		_bufferlist = new ArrayList<DBuffer>();
		_blockcount = 0;
		_maxblockcount = cacheSize;
		_heldlist = new HashSet<DBuffer>();
	}

	@Override
	public synchronized DBuffer getBlock(int blockID) {
		// TODO Auto-generated method stub
		// block is in the cache already
		for (int i = 0; i < _bufferlist.size(); i++) {
			DBuffer currentbuf = _bufferlist.get(i);
			if (currentbuf.getBlockID() == blockID) {
				while (currentbuf.isBusy()) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				while (_heldlist.contains(currentbuf)) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				_bufferlist.remove(currentbuf);
				_bufferlist.add(currentbuf); //MRU is the last block in the list, LRU is first. 
				_heldlist.add(currentbuf);
				return currentbuf;
			}
		}
		//block is not in teh cache
		
		DBuffer newBlock = new Buffer(blockID, _virtualdisk);
		
		if(_blockcount == _maxblockcount) {//cache is full!!!
			DBuffer LRUblock = _bufferlist.get(0);
			while (_heldlist.contains(LRUblock)) { //Theoretically this hsouldn't happen...
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			_bufferlist.remove(0); //remove head, the LRU. 
			_bufferlist.add(newBlock);	
		}
		else { //There is space to add the new block!!!!
			_bufferlist.add(newBlock);
			_blockcount++;
		}
		
		createFThread(newBlock);
		_heldlist.add(newBlock);
		newBlock.waitValid();
		return newBlock;
		
	}
		
	private void createFThread(DBuffer buf) {
		FetchWorker worker = new FetchWorker(buf);
		Thread t = new Thread(worker);
		t.start();
	}

	@Override
	public synchronized void sync() {
		// TODO Auto-generated method stub
		for (int i = 0; i < _bufferlist.size(); i++) {
			DBuffer currentbuf = _bufferlist.get(i);
			if (!currentbuf.checkClean()) {
				createPThread(currentbuf);
			}
		}
		
	}
	
	private void createPThread(DBuffer buf) {
		PushWorker worker = new PushWorker(buf);
		Thread t = new Thread(worker);
		t.start();
	}

	@Override
	public synchronized void releaseBlock(DBuffer buf) {
		//Not sure what to do with this, because I made a Buffer class rather than DBuffer
		_heldlist.remove(buf);
		notifyAll();
	}

}
