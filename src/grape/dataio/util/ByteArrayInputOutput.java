package grape.dataio.util;

import grape.dataio.*;

import java.util.Arrays;

public class ByteArrayInputOutput implements RandomAccessInput, RandomAccessOutput {

	private static final int DEFAULT_INIT_CAP = 16;

	private byte[] _buf;
	private int _size = 0, _index = 0;
	private boolean _littleEndian = true;

	public ByteArrayInputOutput(int initialCapacity) {
		if (initialCapacity <= 0)
			throw new IllegalArgumentException();
		_buf = new byte[initialCapacity];
	}

	public ByteArrayInputOutput() {
		this(DEFAULT_INIT_CAP);
	}

	public boolean isLittleEndian() {
		return _littleEndian;
	}

	public void setLittleEndian(boolean le) {
		_littleEndian = le;
	}

	private void checkReadPosition(int i) {
		if (i > _size - _index)
			throw new RuntimeException("Buffer overrun");
	}

	@Override
	public byte readByte() {
		checkReadPosition(1);
		return _buf[_index++];
	}

	@Override
	public int readUByte() {
		checkReadPosition(1);
		return _buf[_index++] & 0xFF;
	}

	@Override
	public short readShort() {
		return (short) readUShort();
	}

	@Override
	public int readUShort() {
		checkReadPosition(DataConsts.SHORT_SIZE);
		int ret = _littleEndian ? LittleEndian.getUShort(_buf, _index) :
			BigEndian.getUShort(_buf, _index);
		_index += DataConsts.SHORT_SIZE;
		return ret;
	}

	@Override
	public int readInt() {
		return (int) readUInt();
	}

	@Override
	public long readUInt() {
		checkReadPosition(DataConsts.INT_SIZE);
		long ret = _littleEndian ? LittleEndian.getUInt(_buf, _index) :
			BigEndian.getUInt(_buf, _index);
		_index += DataConsts.INT_SIZE;
		return ret;
	}

	@Override
	public long readLong() {
		checkReadPosition(DataConsts.LONG_SIZE);
		long ret = _littleEndian ? LittleEndian.getLong(_buf, _index) :
			BigEndian.getLong(_buf, _index);
		_index += DataConsts.LONG_SIZE;
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
		checkReadPosition(len);
		System.arraycopy(_buf, _index, buf, off, len);
		_index += len;
	}

	private void checkWritePosition(int i) {
		if (_index + i < _buf.length)
			return;

		int newCap = _buf.length * 3 / 2;
		if (newCap < _index + i)
			newCap = _index + i;
		byte[] newBuf = new byte[newCap];
		System.arraycopy(_buf, 0, newBuf, 0, _size);
		_buf = newBuf;
	}

	@Override
	public void writeByte(byte v) {
		checkWritePosition(1);
		_buf[_index++] = v;
	}

	@Override
	public void writeShort(short v) {
		checkWritePosition(DataConsts.SHORT_SIZE);
		if (_littleEndian)
			LittleEndian.putShort(_buf, _index, v);
		else
			BigEndian.putShort(_buf, _index, v);
		_index += DataConsts.SHORT_SIZE;
	}

	@Override
	public void writeInt(int v) {
		checkWritePosition(DataConsts.INT_SIZE);
		if (_littleEndian)
			LittleEndian.putInt(_buf, _index, v);
		else
			BigEndian.putInt(_buf, _index, v);
		_index += DataConsts.INT_SIZE;
	}

	@Override
	public void writeLong(long v) {
		checkWritePosition(DataConsts.LONG_SIZE);
		if (_littleEndian)
			LittleEndian.putLong(_buf, _index, v);
		else
			BigEndian.putLong(_buf, _index, v);
		_index += DataConsts.LONG_SIZE;
	}

	@Override
	public void writeFloat(float v) {
		writeInt(Float.floatToIntBits(v));
	}

	@Override
	public void writeDouble(double v) {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int offset, int len) {
		checkWritePosition(len);
		System.arraycopy(b, offset, _buf, _index, len);
		_index += len;
	}

	@Override
	public RandomAccessOutput createDelayedOutput(int size) {
		checkWritePosition(size);
		return new DelayedRandomAcceesOutput(this, _index, size);
	}

	@Override
	public long tell() {
		return _index;
	}

	@Override
	public long length() {
		return _size;
	}

	@Override
	public long seek(long pos) {
		if (pos < 0 || pos > _size)
			throw new IllegalArgumentException();
		_index = (int) pos;
		return _index;
	}

	@Override
	public int available() {
		return _size - _index;
	}

	@Override
	public long skip(long len) {
		if (_index + len < 0 || _index + len > _size)
			throw new IllegalArgumentException();

		_index += len;
		return _index;
	}

	public byte[] buffer() {
		return _buf;
	}

	public byte[] toArray() {
		return Arrays.copyOf(_buf, _size);
	}
}
