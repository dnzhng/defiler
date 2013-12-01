package test;

import common.DFileID;

import dfs.DFS;

public class TestClient implements Runnable {

	private DFS _DFS;

	private byte[] _writeBuffer;
	private DFileID _ID;

	private boolean exit = false;
	
	public TestClient(DFS dfs, String writeText) {
		_DFS = dfs;
		_writeBuffer = writeText.getBytes();

	}
	
	//comment for errorz lolz

	public void createFile(){
		_ID = _DFS.createDFile();
		boolean x = false;
		for( DFileID id : _DFS.listAllDFiles()){
			if(id.equals(_ID)){
				x = true;
			}
		}
		assert(x);
	}

	public void writeFile(){
		_DFS.write(_ID, _writeBuffer, 0, _writeBuffer.length);
	}
	
	public void readFile(){
		byte[] buffer = new byte[_writeBuffer.length];

		_DFS.read(_ID, buffer, 0, _writeBuffer.length);

		for (int i = 0; i < buffer.length; ++i) {
			assert (buffer[i] == _writeBuffer[i]);
		}
	}
	
	public void destroyFile(){
		byte[] buffer = new byte[_writeBuffer.length];
		_DFS.destroyDFile(_ID);
		try{
			_DFS.read(_ID, buffer, 0, _writeBuffer.length);
		}
		catch(NullPointerException e){
			System.out.println("successfully made a file, wrote it, read it back, and deleted it");
		}
		exit = true;
	}
	
	
	
	
	@Override
	public void run() {
		
		// spin it
		while(exit){
			// lala
		}
	}

}
