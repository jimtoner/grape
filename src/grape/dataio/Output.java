package grape.dataio;

public interface Output {

	void writeByte(int v);

	void writeShort(int v);

	void writeInt(int v);

	void writeLong(long v);

	void writeFloat(float v);

	void writeDouble(double v);

	void write(byte[] b);

	void write(byte[] b, int offset, int len);
}
