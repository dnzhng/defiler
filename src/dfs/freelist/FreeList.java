package dfs.freelist;

public class FreeList {

	private class Node {
		public int _start;
		public int _size;
		public Node _next;

		public Node(int start, int size, Node next) {
			_start = start;
			_size = size;
			_next = next;
		}

		public String toString() {
			return "(" + _start + "," + _size + ")";
		}

		public void append(Node node) {
			if (this._size + this._start == node._start) {
				this._size += node._size;

				if (node._next != null) {
					this._next = node._next;
				}
			}
			else{
				node._next = this._next;
				this._next = node;
			}

		}

	}

	private Node _head;
	private int _totalSize;

	public FreeList(int totalSize) {
		_head = new Node(0, totalSize, null);
		_totalSize = totalSize;
	}

	public int getTotalSize() {
		return _totalSize;
	}

	public int allocate() {
		Node current = _head;
		Node prev = null;
		while (current._next != null) {
			prev = current;
			current = current._next;
		}
		int res = current._start + current._size - 1;

		current._size = current._size - 1;
		if (current._size == 0) {
			if (prev == null) {
				_head = null;
			} else {
				prev._next = current._next;
			}
		}

		return res;
	}

	public void allocate(int blockID) {

		Node current = _head;

		// TODO: verify this for off by one
		while (current._next != null && current._next._start < blockID) {
			current = current._next;
		}
		if (current._size + current._start < blockID) {
			// block is already allocated
			return;
		}
		Node newFrag = new Node(blockID + 1, current._size - blockID
				+ current._start - 1, current._next);
		current._next = newFrag;
	}

	public boolean isAllocated(int blockID) {
		Node current = getNode(blockID);
		return current._start + current._size > blockID;
	}

	public void free(int block) {

		// case where we free a single block
		if (_head == null) {
			_head = new Node(block, 1, null);
			return;
		}
		if (block < _head._start) {
			Node temp = _head;
			_head = new Node(block, 1, null);
			_head.append(temp);
			return;
		}

		Node current = _head;
		while (current._next != null && current._start + current._size <= block) {
			current = current._next;
		}

		current.append(new Node(block, 1, null));
	}

	private Node getNode(int block) {
		Node current = _head;

		if (_head == null) {
			return null;
		}

		while (current._next != null && current._next._start > block) {
			current = current._next;
		}
		return current;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Node current = _head;
		while (current != null) {
			sb.append(" ");
			sb.append(current.toString());
			current = current._next;
		}
		return sb.toString();
	}

}
