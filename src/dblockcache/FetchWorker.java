package dblockcache;

public class FetchWorker implements Runnable{
	
	private DBuffer _buffer;
	
	public FetchWorker(DBuffer buf) {
		_buffer = buf;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		_buffer.startFetch();
	}

}
