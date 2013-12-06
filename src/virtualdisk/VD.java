package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;

import common.Constants;
import common.Constants.DiskOperationType;

import dblockcache.DBuffer;

public class VD extends VirtualDisk{

	public VD(String filename, boolean format) throws FileNotFoundException, IOException {
		super(filename, format);
	}
	
	public VD(boolean format) throws FileNotFoundException, IOException {
		super(format);
	}
	
	public VD() throws FileNotFoundException, IOException {
		super();
	}

	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation)
			throws IllegalArgumentException, IOException {
		if (operation == Constants.DiskOperationType.READ) {
			readBlock(buf);
		}
		else if (operation == Constants.DiskOperationType.WRITE) {
			writeBlock(buf);
		}
		
		buf.ioComplete();
	}

}
