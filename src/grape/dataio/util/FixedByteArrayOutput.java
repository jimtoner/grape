package grape.dataio.util;

import grape.dataio.DataConsts;
import grape.dataio.RandomAccessOutput;

public class FixedByteArrayOutput implements RandomAccessOutput {

	private final byte[] _buf;
	private final int _endIndex;
	private int _writeIndex;
	private boolean _littleEndian = true;

	public FixedByteArrayOutput(byte[] buf, int startOffset, int len) {
		if (startOffset < 0 || len < 0 || startOffset + len > buf.length)
			throw new IllegalArgumentException();

		_buf = buf;
		_writeIndex = startOffset;
		_endIndex = startOffset + len;
	}

	public FixedByteArrayOutput(byte[] buf, int startOffset) {
		this(buf, startOffset, buf.length - startOffset);
	}

	public FixedByteArrayOutput(byte[] buf) {
		this(buf, 0, buf.length);
	}

	public boolean isLittleEndian() {
		return _littleEndian;
	}

	public void setLittleEndian(boolean le) {
		_littleEndian = le;
	}

	private void checkPosition(int i) {
		if (i > _endIndex - _writeIndex)
			throw new RuntimeException("Buffer overrun");
	}

	@Override
	public void writeByte(int v) {
		checkPosition(1);
		_buf[_writeIndex++] = (byte) v;
	}

	@Override
	public void writeShort(int v) {
		checkPosition(DataConsts.SHORT_SIZE);
		if (_littleEndian)
			LittleEndian.putShort(_buf, _writeIndex, (short) v);
		else
			BigEndian.putShort(_buf, _writeIndex, (short) v);
		_writeIndex += DataConsts.SHORT_SIZE;
	}

	@Override
	public void writeInt(int v) {
		checkPosition(DataConsts.INT_SIZE);
		if (_littleEndian)
			LittleEndian.putInt(_buf, _writeIndex, v);
		else
			BigEndian.putInt(_buf, _writeIndex, v);
		_writeIndex += DataConsts.INT_SIZE;
	}

	@Override
	public void writeLong(long v) {
		checkPosition(DataConsts.LONG_SIZE);
		if (_littleEndian)
			LittleEndian.putLong(_buf, _writeIndex, v);
		else
			BigEndian.putLong(_buf, _writeIndex, v);
		_writeIndex += DataConsts.LONG_SIZE;
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
		checkPosition(len);
		System.arraycopy(b, offset, _buf, _writeIndex, len);
		_writeIndex += len;
	}

	@Override
	public FixedByteArrayOutput createDelayedOutput(int size) {
		checkPosition(size);
		FixedByteArrayOutput result = new FixedByteArrayOutput(_buf, _writeIndex, size);
		_writeIndex += size;
		return result;
	}

	@Override
	public long tell() {
		return _writeIndex;
	}

	@Override
	public long length() {
		return _buf.length;
	}

	@Override
	public long seek(long pos) {
		_writeIndex = (int) pos;
		return _writeIndex;
	}

}
