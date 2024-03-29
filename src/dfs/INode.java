package dfs;

import common.Constants;
import common.DFileID;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents an INode for an individual file.
 * Contains the size, the location of the start of the file,
 * if this entry is currently valid (TODO: this may not be needed),
 * and the ID of the block -- be careful using this because of singleton
 * w.r.t. the DFileID objects.
 * 
 * @author Scott
 *
 */
public class INode {

	private DFileID _ID;
	private boolean _valid;
	private int _headBlock;
	private int _size;

	public INode(byte[] inodeData) throws IOException {

		int offset = 0;
		_ID = new DFileID(readInt(inodeData, offset));

		initData(inodeData);
	}

	public INode(DFileID id, byte[] inodeData) {
		_ID = id;
		initData(inodeData);
	}

	public INode(DFileID id, boolean valid, int size, int headBlock) {
		_ID = id;
		_valid = valid;
		_size = size;
		_headBlock = headBlock;
	}

	private void initData(byte[] data) {
		int offset = Integer.SIZE / 8;
		int valid = readInt(data, offset);
		offset += Integer.SIZE / 8;

		_size = readInt(data, offset);
		offset += Integer.SIZE / 8;
		_headBlock = readInt(data, offset);

		if (valid == -1) {
			_valid = true;
		} else {
			_valid = false;
		}

	}

	private int readInt(byte[] data, int offset) {

		byte[] integer = new byte[Integer.SIZE / 8];

		for (int i = 0; i < Integer.SIZE / 8; ++i) {
			integer[i] = data[offset + i];
		}
		ByteBuffer wrapped = ByteBuffer.wrap(integer);
		return wrapped.getInt(0);
	}

	public DFileID getDFileID() {
		return _ID;
	}

	public boolean isValid() {
		return _valid;
	}

	public int getFileSize() {
		return _size;
	}

	public int getHeadBlock() {
		return _headBlock;
	}

	private byte[] toByteArray(int val) {
		ByteBuffer dbuf = ByteBuffer.allocate(Integer.SIZE / 8);
		dbuf.putInt(val);
		return dbuf.array();
	}

	public byte[] toByteArray() {
		byte[] res = new byte[Constants.INODE_SIZE];

		byte[] id = toByteArray(_ID.getDFileID());

		int v = 0;
		if (_valid) {
			v = -1;
		}
		byte[] valid = toByteArray(v);
		byte[] size = toByteArray(_size);
		byte[] headBlock = toByteArray(_headBlock);

		int offset = 0;

		copyArray(res, id, offset);
		offset += Integer.SIZE / 8;
		copyArray(res, valid, offset);
		offset += Integer.SIZE / 8;
		copyArray(res, size, offset);
		offset += Integer.SIZE / 8;
		copyArray(res, headBlock, offset);
		offset += Integer.SIZE / 8;

		for (int i = offset; i < res.length; i++) {
			res[i] = 0;
		}

		return res;
	}

	private void copyArray(byte[] copy, byte[] orig, int offset) {

		for (int i = 0; i < orig.length && i + offset < copy.length; ++i) {
			copy[i + offset] = orig[i];
		}
	}

	public void setValid(boolean status) {
		_valid = status;
	}

	public void setSize(int newFileSize) {
		_size = newFileSize;
	}

}
