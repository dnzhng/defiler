package dblockcache;

public class PushWorker implements Runnable{
	private DBuffer _buffer;
	
	public PushWorker(DBuffer buf) {
		_buffer = buf;
	}

	@Override
	public void run() {
		System.out.println("Starting push to block " + _buffer.getBlockID());
		_buffer.startPush();
		System.out.println("Finished push to block " + _buffer.getBlockID());
	}


}
