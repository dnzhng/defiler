package dblockcache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import virtualdisk.IVirtualDisk;

public class BufferCache extends DBufferCache {

	private IVirtualDisk _virtualdisk;
	private int _maxblockcount;
	private ArrayList<DBuffer> _bufferlist;
	public ArrayList<DBuffer> bufferlistcopy;
	private Set<DBuffer> _heldlist;

	public BufferCache(int cacheSize, IVirtualDisk disk) {
		super(cacheSize);
		_virtualdisk = disk;
		_bufferlist = new ArrayList<DBuffer>();
		_maxblockcount = cacheSize;
		_heldlist = new HashSet<DBuffer>();
	}
	
	@Override
	public synchronized DBuffer getBlock(int blockID){
		DBuffer buffer;
		if(inCache(blockID)){
			buffer = pullFromCache(blockID);
			bufferlistcopy = _bufferlist;
		}
		else{
			buffer = pullFromDisk(blockID);
			bufferlistcopy = _bufferlist;
		}
		buffer.waitValid();
		while(_heldlist.contains(buffer)){
			try {
				//System.out.println("ERROR: BLOCK IS HELD");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		_heldlist.add(buffer);
		//System.out.println("GETTING BLOCK " + blockID);
		return buffer;
	}
	
	
	private DBuffer pullFromCache(int blockID) {
		for(int i = 0; i < _bufferlist.size(); ++i){
			if(_bufferlist.get(i).getBlockID() == blockID){
				DBuffer res = _bufferlist.remove(i);
				_bufferlist.add(res);
				return res;
			}
		}
		return null;
	}

	private void evictLRU() {
		if(_bufferlist.size() == 0){
			// TODO: error.
			return;
		}
		
		// if all of the blocks are held, wait until someone frees one.
		if(_heldlist.size() == _bufferlist.size()){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < _bufferlist.size(); ++i){
			DBuffer currentBlock = _bufferlist.get(i);
			if(!_heldlist.contains(currentBlock)){
				evict(_bufferlist.remove(i));
				break;
			}
		}
		
	}
	
	private void evict(DBuffer currentBlock){
		while(currentBlock.isBusy()){
			try {
				currentBlock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(!currentBlock.checkClean()){
			startPushThread(currentBlock);
		}
	}
	
	
	private DBuffer pullFromDisk(int blockID){
		if(_bufferlist.size() == _maxblockcount){
			evictLRU();
		}
		DBuffer newBlock = new Buffer(blockID, _virtualdisk);
		startFetchThread(newBlock);
		_bufferlist.add(newBlock);
		return newBlock;
	}
	
	private boolean inCache(int blockID){	
		for (int i = 0; i < _bufferlist.size(); i++) {
			DBuffer currentbuf = _bufferlist.get(i);
			if(currentbuf.getBlockID() == blockID){
				return true;
			}
		}
		return false;
	}

	private void startFetchThread(DBuffer buf) {
		
		FetchWorker worker = new FetchWorker(buf);
		Thread t = new Thread(worker);
		//System.out.println("Starting Fetch Thread");
		t.start();
	}

	@Override
	public synchronized void sync() {
		for (int i = 0; i < _bufferlist.size(); i++) {
			DBuffer currentbuf = _bufferlist.get(i);
			if (!currentbuf.checkClean()) {
				startPushThread(currentbuf);
			}
		}

	}

	private void startPushThread(DBuffer buf) {
		PushWorker worker = new PushWorker(buf);
		Thread t = new Thread(worker);
		//System.out.println("Starting Push Thread");
		t.start();
	}

	@Override
	public synchronized void releaseBlock(DBuffer buf) {
		_heldlist.remove(buf);
		notifyAll();
	}

}
