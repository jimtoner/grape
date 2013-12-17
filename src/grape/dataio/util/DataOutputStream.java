package grape.dataio.util;

import grape.dataio.Output;

import java.io.*;

public class DataOutputStream extends FilterOutputStream implements Output {

	private boolean _littleEndian = true;

	public DataOutputStream(OutputStream out) {
		super(out);
	}

	public boolean isLittleEndian() {
		return _littleEndian;
	}

	public void setLittleEndian(boolean le) {
		_littleEndian = le;
	}

	@Override
	public void writeByte(int v) {
		try {
			out.write(v);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeShort(int v) {
		int b0 = v & 0xFF;
		int b1 = (v >>> 8) & 0xFF;
		try {
			if (_littleEndian) {
				out.write(b0);
				out.write(b1);
			} else {
				out.write(b1);
				out.write(b0);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeInt(int v) {
		int b0 = v & 0xFF;
		int b1 = (v >>> 8) & 0xFF;
		int b2 = (v >>> 16) & 0xFF;
		int b3 = (v >>> 24) & 0xFF;
		try {
			if(_littleEndian) {
				out.write(b0);
				out.write(b1);
				out.write(b2);
				out.write(b3);
			} else {
				out.write(b3);
				out.write(b2);
				out.write(b1);
				out.write(b0);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeLong(long v) {
		if (_littleEndian) {
			writeInt((int) v);
			writeInt((int) (v >>> 32));
		} else {
			writeInt((int) (v >>> 32));
			writeInt((int) v);
		}
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
		// suppress IOException for interface method
		try {
			super.write(b);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) {
		// suppress IOException for interface method
		try {
			super.write(b, off, len);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
