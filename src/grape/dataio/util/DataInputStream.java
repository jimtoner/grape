package grape.dataio.util;

import grape.dataio.Input;

import java.io.*;

public class DataInputStream extends FilterInputStream implements Input {

	private boolean _littleEndian = true;

	public DataInputStream(InputStream is) {
		super(is);
	}

	public boolean isLittleEndian() {
		return _littleEndian;
	}

	public void setLittleEndian(boolean le) {
		_littleEndian = le;
	}

	private static void checkEOF(int value) {
		if (value < 0)
			throw new RuntimeException("Unexpected end-of-file");
	}

	@Override
	public byte readByte() {
		return (byte) readUByte();
	}

	@Override
	public int readUByte() {
		int b;
		try {
			b = in.read();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		checkEOF(b);
		return b;
	}

	@Override
	public short readShort() {
		return (short) readUShort();
	}

	@Override
	public int readUShort() {
		int b0, b1;
		try {
			if (_littleEndian) {
				b0 = in.read();
				b1 = in.read();
			} else {
				b1 = in.read();
				b0 = in.read();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		checkEOF(b0 | b1);
		return (b1 << 8) + (b0 << 0);
	}

	@Override
	public int readInt() {
		return (int) readUInt();
	}

	@Override
	public long readUInt() {
		int b0, b1, b2, b3;
		try {
			if (_littleEndian) {
				b0 = in.read();
				b1 = in.read();
				b2 = in.read();
				b3 = in.read();
			} else {
				b3 = in.read();
				b2 = in.read();
				b1 = in.read();
				b0 = in.read();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		checkEOF(b0 | b1 | b2 | b3);
		return ((long) b3 << 24) + ((long) b2 << 16) + ((long) b1 << 8) + ((long) b0 << 0);
	}

	@Override
	public long readLong() {
		int b0, b1, b2, b3, b4, b5, b6, b7;
		try {
			if (_littleEndian) {
				b0 = in.read();
				b1 = in.read();
				b2 = in.read();
				b3 = in.read();
				b4 = in.read();
				b5 = in.read();
				b6 = in.read();
				b7 = in.read();
			} else {
				b7 = in.read();
				b6 = in.read();
				b5 = in.read();
				b4 = in.read();
				b3 = in.read();
				b2 = in.read();
				b1 = in.read();
				b0 = in.read();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		checkEOF(b0 | b1 | b2 | b3 | b4 | b5 | b6 | b7);
		return (((long) b7 << 56) + ((long) b6 << 48) + ((long) b5 << 40)
				+ ((long) b4 << 32) + ((long) b3 << 24) + (b2 << 16)
				+ (b1 << 8) + (b0 << 0));
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
		int max = off + len;
		for (int i = off; i < max; i++) {
			int ch;
			try {
				ch = in.read();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			checkEOF(ch);
			buf[i] = (byte) ch;
		}
	}

	@Override
	public int available() {
		try {
			return super.available();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long skip(long len) {
		try {
			return in.skip(len);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
