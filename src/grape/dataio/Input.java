package grape.dataio;

public interface Input {

	byte readByte();

	int readUByte();

	short readShort();

	int readUShort();

	int readInt();

	long readUInt();

	long readLong();

	float readFloat();

	double readDouble();

	void readFully(byte[] buf);

	void readFully(byte[] buf, int off, int len);

	int available();

	long skip(long len);
}
