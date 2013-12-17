package grape.dataio.util;

import grape.dataio.DataConsts;
import grape.dataio.RandomAccessOutput;

public class DelayedRandomAcceesOutput implements RandomAccessOutput {

	private final RandomAccessOutput _output;
	private final int _beginIndex, _endIndex;
	private int _writeIndex;

	public DelayedRandomAcceesOutput(RandomAccessOutput output, int beginIndex, int len) {
		if (beginIndex < 0 || len < 0)
			throw new IllegalArgumentException();

		_output = output;
		_beginIndex = beginIndex;
		_endIndex = beginIndex + len;
		_writeIndex = beginIndex;
	}

	private void checkPosition(int i) {
		if (_writeIndex + i > _endIndex)
			throw new RuntimeException();
	}

	@Override
	public void writeByte(int v) {
		checkPosition(1);

		final long oldIndex = _output.tell();
		_output.seek(_writeIndex);
		_output.writeByte(v);
		_output.seek(oldIndex);
		_writeIndex += 1;
	}

	@Override
	public void writeShort(int v) {
		checkPosition(DataConsts.SHORT_SIZE);

		final long oldIndex = _output.tell();
		_output.seek(_writeIndex);
		_output.writeShort(v);
		_output.seek(oldIndex);
		_writeIndex += DataConsts.SHORT_SIZE;
	}

	@Override
	public void writeInt(int v) {
		checkPosition(DataConsts.INT_SIZE);

		final long oldIndex = _output.tell();
		_output.seek(_writeIndex);
		_output.writeInt(v);
		_output.seek(oldIndex);
		_writeIndex += DataConsts.INT_SIZE;
	}

	@Override
	public void writeLong(long v) {
		checkPosition(DataConsts.LONG_SIZE);

		final long oldIndex = _output.tell();
		_output.seek(_writeIndex);
		_output.writeLong(v);
		_output.seek(oldIndex);
		_writeIndex += DataConsts.LONG_SIZE;
	}

	@Override
	public void writeFloat(float v) {
		checkPosition(DataConsts.FLOAT_SIZE);

		final long oldIndex = _output.tell();
		_output.seek(_writeIndex);
		_output.writeFloat(v);
		_output.seek(oldIndex);
		_writeIndex += DataConsts.FLOAT_SIZE;
	}

	@Override
	public void writeDouble(double v) {
		checkPosition(DataConsts.DOUBLE_SIZE);

		final long oldIndex = _output.tell();
		_output.seek(_writeIndex);
		_output.writeDouble(v);
		_output.seek(oldIndex);
		_writeIndex += DataConsts.DOUBLE_SIZE;
	}

	@Override
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int offset, int len) {
		checkPosition(len);

		final long oldIndex = _output.tell();
		_output.seek(_writeIndex);
		_output.write(b, offset, len);
		_output.seek(oldIndex);
		_writeIndex += len;
	}

	@Override
	public RandomAccessOutput createDelayedOutput(int size) {
		checkPosition(size);
		return new DelayedRandomAcceesOutput(this, _writeIndex, size);
	}

	@Override
	public long tell() {
		return _writeIndex;
	}

	@Override
	public long length() {
		return _endIndex;
	}

	@Override
	public long seek(long pos) {
		if (pos < _beginIndex || pos > _endIndex)
			throw new RuntimeException();
		_writeIndex = (int) pos;
		return _writeIndex;
	}
}
