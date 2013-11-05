package grape.dataio.util;

import grape.dataio.DataConsts;
import grape.dataio.RandomAccessInput;

public class FixedByteArrayInput implements RandomAccessInput {

	private final byte[] _buf;
	private final int _endIndex;
	private int _readIndex;
	private boolean _littleEndian = true;

	public FixedByteArrayInput(byte[] buf, int startOffset, int len) {
		if (startOffset < 0 || len < 0 || startOffset + len > buf.length)
			throw new IllegalArgumentException();

		_buf = buf;
		_readIndex = startOffset;
		_endIndex = startOffset + len;
	}

	public FixedByteArrayInput(byte[] buf, int startOffset) {
		this(buf, startOffset, buf.length - startOffset);
	}

	public FixedByteArrayInput(byte[] buf) {
		this(buf, 0, buf.length);
	}

	public boolean isLittleEndian() {
		return _littleEndian;
	}

	public void setLittleEndian(boolean le) {
		_littleEndian = le;
	}

	private void checkPosition(int i) {
		if (i > _endIndex - _readIndex)
			throw new RuntimeException("Buffer overrun");
	}

	@Override
	public byte readByte() {
		checkPosition(1);
		return _buf[_readIndex++];
	}

	@Override
	public int readUByte() {
		checkPosition(1);
		return _buf[_readIndex++] & 0xFF;
	}

	@Override
	public short readShort() {
		return (short) readUShort();
	}

	@Override
	public int readUShort() {
		checkPosition(DataConsts.SHORT_SIZE);
		int ret = _littleEndian ? LittleEndian.getUShort(_buf, _readIndex) :
			BigEndian.getUShort(_buf, _readIndex);
		_readIndex += DataConsts.SHORT_SIZE;
		return ret;
	}

	@Override
	public int readInt() {
		return (int) readUInt();
	}

	@Override
	public long readUInt() {
		checkPosition(DataConsts.INT_SIZE);
		long ret = _littleEndian ? LittleEndian.getUInt(_buf, _readIndex) :
			BigEndian.getUInt(_buf, _readIndex);
		_readIndex += DataConsts.INT_SIZE;
		return ret;
	}

	@Override
	public long readLong() {
		checkPosition(DataConsts.LONG_SIZE);
		long ret = _littleEndian ? LittleEndian.getLong(_buf, _readIndex) :
			BigEndian.getLong(_buf, _readIndex);
		_readIndex += DataConsts.LONG_SIZE;
		return ret;
	}

	@Override
	public float readFloat() {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public void readFully(byte[] buf) {
		readFully(buf, 0, buf.length);
	}

	@Override
	public void readFully(byte[] buf, int off, int len) {
		checkPosition(len);
		System.arraycopy(_buf, _readIndex, buf, off, len);
		_readIndex += len;
	}

	@Override
	public int available() {
		return _endIndex - _readIndex;
	}

	@Override
	public long skip(long len) {
		checkPosition((int) len);
		_readIndex += len;
		return len;
	}

	@Override
	public long tell() {
		return _readIndex;
	}

	@Override
	public long seek(long pos) {
		_readIndex = (int) pos;
		return _readIndex;
	}
}
