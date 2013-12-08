package test;

import java.util.Random;

import common.Constants;
import common.DFileID;
import dfs.DFS;

public class TestClient implements Runnable {

	private DFS _DFS;


	private byte[] _writeBuffer;
	private DFileID _ID;

	private boolean exit = false;
	private int _name;
	
	public TestClient(DFS dfs, int clientName, int dataAmount) {
		_DFS = dfs;
		_writeBuffer = getLotsOfBytes(dataAmount);//writeText.getBytes();
		_name = clientName;
	}
	
	//comment for errorz lolz

	private byte[] getLotsOfBytes(int dataAmount) {
		Random r = new Random();
		byte[] data = new byte[dataAmount];
		
		r.nextBytes(data);
		
		
		
		return data;
	}

	public void createFile(){
		_ID = _DFS.createDFile();
		boolean x = true;
		for( DFileID id : _DFS.listAllDFiles()){
			if(!id.equals(_ID)){
				x = false;
			}
		}
		assert(x);
		System.out.println("Created file: " + _ID.getDFileID());
	}

	public void writeFile(){
		_DFS.write(_ID, _writeBuffer, 0, _writeBuffer.length);
		System.out.println("Wrote file: " + _ID.getDFileID());
	}
	
	public void readFile(){
		byte[] buffer = new byte[_writeBuffer.length];

		_DFS.read(_ID, buffer, 0, _writeBuffer.length);

		for (int i = 0; i < buffer.length; ++i) {
			if (buffer[i] != _writeBuffer[i]){
				System.err.println("FAILURE READING!!!");
				break;
			}
		}
		System.out.println("read file " + _ID.getDFileID());
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
		System.out.println("Deleted file: " + _ID.getDFileID());
	}
	
	
	
	
	@Override
	public void run() {
		
		// spin it
		while(exit){
			// lala
		}
	}

}
