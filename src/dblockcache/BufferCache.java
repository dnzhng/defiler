package dblockcache;

import java.util.ArrayList;

import common.Constants;

import virtualdisk.VD;

public class BufferCache extends DBufferCache {
	
	private int _cachesize;
	private VD _virtualdisk;
	private int _blockcount;
	private int _maxblockcount;
	private ArrayList<Buffer> _bufferlist;

	public BufferCache(int cacheSize, VD disk) {
		// 		_cacheSize = cacheSize * Constants.BLOCK_SIZE; This is what happens in the super?? 
			super(cacheSize);
		// TODO Auto-generated constructor stub
		_virtualdisk = disk;
		_bufferlist = new ArrayList<Buffer>();
		_blockcount = 0;
		_maxblockcount = cacheSize;
	}

	@Override
	public synchronized DBuffer getBlock(int blockID) {
		// TODO Auto-generated method stub
		// block is in the cache already
		for (int i = 0; i < _bufferlist.size(); i++) {
			Buffer currentbuf = _bufferlist.get(i);
			if (currentbuf.getBlockID() == blockID) {
				while (currentbuf.isBusy()) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				_bufferlist.remove(currentbuf);
				_bufferlist.add(currentbuf); //MRU is the last block in the list, LRU is first. 
				
				// What if the block doesn't do any i/o?
				currentbuf._isBusy = true;
				return currentbuf;
			}
		}
		//block is not in teh cache
		
		Buffer newBlock = new Buffer(blockID, _virtualdisk);
		
		if(_blockcount == _maxblockcount) {//cache is full!!!
			_bufferlist.remove(0); //remove head, the LRU. 
			_bufferlist.add(newBlock);	
		}
		else { //There is space to add the new block!!!!
			_bufferlist.add(newBlock);
			_blockcount++;
		}
		
		// Is the block returned necessarily busy?
		newBlock._isBusy = true;
		newBlock.startFetch();
		return newBlock;
		
	}

	// we need to get rid of this. It doesn't conform to the interface
	public synchronized void releaseBlock(Buffer buf) {
		// TODO Auto-generated method stub
		buf._isBusy = false;
		notifyAll();
		
	}

	@Override
	public synchronized void sync() {
		// TODO Auto-generated method stub
		for (int i = 0; i < _bufferlist.size(); i++) {
			Buffer currentbuf = _bufferlist.get(i);
			if (!currentbuf.checkClean()) {
				currentbuf.startPush();
			}
		}
		
	}

	@Override
	public void releaseBlock(DBuffer buf) {
		// TODO Auto-generated method stub
		//Not sure what to do with this, because I made a Buffer class rather than DBuffer		
	}

}
