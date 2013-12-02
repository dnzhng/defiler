package test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import common.DFileID;
import dfs.INode;

public class INodeIO {

	@Test
	public void test() {
		
		int id = 4;
		boolean valid = true;
		int size = 746323;
		int start = -1;
		
		
		INode in = new INode(new DFileID(id), valid, size, start);
		
		byte[] data = in.toByteArray();
		INode read;
		try {
			read = new INode(data);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
			return;
		}
		
		assertEquals(id, read.getDFileID().getDFileID());
		assertEquals(valid, read.isValid());
		assertEquals(size, read.getFileSize());
		assertEquals(start, read.getHeadBlock());
		
	}

}
