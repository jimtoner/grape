package grape.dataio.util;

import grape.dataio.DataConsts;

import java.io.IOException;
import java.io.OutputStream;

public final class LittleEndian {

	private LittleEndian() {}

	public static int getUByte(byte[] data, int offset) {
		return data[offset] & 0xFF;
	}

	public static short getShort(byte[] data, int offset) {
		int b0 = data[offset] & 0xFF;
		int b1 = data[offset + 1] & 0xFF;
		return (short) ((b1 << 8) + b0);
	}

	public static int getUShort(byte[] data, int offset) {
		int b0 = data[offset] & 0xFF;
		int b1 = data[offset + 1] & 0xFF;
		return (b1 << 8) + b0;
	}

	public static int getInt(byte[] data, int offset) {
		int b0 = data[offset] & 0xFF;
		int b1 = data[offset + 1] & 0xFF;
		int b2 = data[offset + 2] & 0xFF;
		int b3 = data[offset + 3] & 0xFF;
		return (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
	}

	public static long getUInt(byte[] data, int offset) {
		return getInt(data, offset) & 0x00FFFFFFFFL;
	}

	public static long getLong(byte[] data, int offset) {
		long result = 0;
		for (int i = offset + DataConsts.LONG_SIZE - 1; i >= offset; --i) {
			result <<= 8;
			result |= data[i] & 0xFF;
		}
		return result;
	}

	public static float getFloat(byte[] data, int offset) {
		return Float.intBitsToFloat(getInt(data, offset));
	}

	public static double getDouble(byte[] data, int offset) {
		return Double.longBitsToDouble(getLong(data, offset));
	}

	public static void putShort(byte[] data, int offset, short value) {
		data[offset] = (byte) (value & 0xFF);
		data[offset + 1] = (byte) ((value >>> 8) & 0xFF);
	}

	public static void putInt(byte[] data, int offset, int value) {
		data[offset] = (byte) (value & 0xFF);
		data[offset + 1] = (byte) ((value >>> 8) & 0xFF);
		data[offset + 2] = (byte) ((value >>> 16) & 0xFF);
		data[offset + 3] = (byte) ((value >>> 24) & 0xFF);
	}

	public static void putLong(byte[] data, int offset, long value) {
		final int limit = DataConsts.LONG_SIZE + offset;
		for (int i = offset; i < limit; ++i) {
			data[i] = (byte) (value & 0xFF);
			value >>>= 8;
		}
	}

	public static void putFloat(byte[] data, int offset, float value) {
		putInt(data, offset, Float.floatToIntBits(value));
	}

	public static void putDouble(byte[] data, int offset, double value) {
		putLong(data, offset, Double.doubleToLongBits(value));
	}

	public static short readShort(DataInputStream stream) throws IOException {
		int b0 = stream.read();
		int b1 = stream.read();
		if ((b0 | b1) < 0)
			throw new IOException();
		return (short) ((b1 << 8) + b0);
	}

	public static int readUShort(DataInputStream stream) throws IOException {
		int b0 = stream.read();
		int b1 = stream.read();
		if ((b0 | b1) < 0)
			throw new IOException();
		return (b1 << 8) + b0;
	}

	public static int readInt(DataInputStream stream) throws IOException {
		int b0 = stream.read();
		int b1 = stream.read();
		int b2 = stream.read();
		int b3 = stream.read();
		if ((b0 | b1 | b2 | b3) < 0)
			throw new IOException();
		return (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
	}

	public static long readLong(DataInputStream stream) throws IOException {
		int b0 = stream.read();
		int b1 = stream.read();
		int b2 = stream.read();
		int b3 = stream.read();
		int b4 = stream.read();
		int b5 = stream.read();
		int b6 = stream.read();
		int b7 = stream.read();
		if ((b0 | b1 | b2 | b3 | b4 | b5 | b6 | b7) < 0)
			throw new IOException();

		return (((long) b7) << 56) + (((long) b6) << 48) + (((long) b5) << 40) +
				(((long) b4) << 32) + (((long) b3) << 24) +
				(b2 << 16) + (b1 << 8) + b0;
	}

	public static float readFloat(DataInputStream stream) throws IOException {
		return Float.intBitsToFloat(readInt(stream));
	}

	public static double readDouble(DataInputStream stream) throws IOException {
		return Double.longBitsToDouble(readLong(stream));
	}

	public static void writeShort(OutputStream stream, byte value) throws IOException {
		stream.write(value & 0xFF);
		stream.write((value >>> 8) & 0xFF);
	}

	public static void writeInt(OutputStream stream, int value) throws IOException {
		stream.write(value & 0xFF);
		stream.write((value >>> 8) & 0xFF);
		stream.write((value >>> 16) & 0xFF);
		stream.write((value >>> 24) & 0xFF);
	}

	public static void writeLong(OutputStream stream, long value) throws IOException {
		for (int i = 0; i < DataConsts.LONG_SIZE; ++i) {
			stream.write((int) (value & 0xFF));
			value >>>= 8;
		}
	}

	public static void writeFloat(OutputStream stream, float value) throws IOException {
		writeInt(stream, Float.floatToIntBits(value));
	}

	public static void writeDouble(OutputStream stream, double value) throws IOException {
		writeLong(stream, Double.doubleToLongBits(value));
	}
}
