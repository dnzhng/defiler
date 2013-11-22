package defiler;

import java.io.IOException;

import common.Constants;
import common.Constants.DiskOperationType;

import dblockcache.DBuffer;
import virtualdisk.VirtualDisk;

public class defilerVirtualDisk extends VirtualDisk {

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
