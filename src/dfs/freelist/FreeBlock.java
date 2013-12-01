package dfs.freelist;

public class FreeBlock {

	private int _location;
	private int _size;
	
	public FreeBlock(int local, int size){
		_location = local;
		_size = size;
	}
	
	public int getLocation() {
		return _location;
	}
	public void setLocation(int myLocation) {
		this._location = myLocation;
	}
	public int getSize() {
		return _size;
	}
	public void setSize(int mySize) {
		this._size = mySize;
	}
	
}
