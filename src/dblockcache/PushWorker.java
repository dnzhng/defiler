package dblockcache;

public class PushWorker implements Runnable{
	private DBuffer _buffer;
	
	public PushWorker(DBuffer buf) {
		_buffer = buf;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		_buffer.startPush();
	}


}
