package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;

import common.Constants;
import common.Constants.DiskOperationType;

import dblockcache.DBuffer;

public class VD extends VirtualDisk{

	public VD() throws FileNotFoundException, IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation)
			throws IllegalArgumentException, IOException {
		// TODO Auto-generated method stub
		if (operation == Constants.DiskOperationType.READ) {
			readBlock(buf);
		}
		else if (operation == Constants.DiskOperationType.WRITE) {
			writeBlock(buf);
		}
	}

}
