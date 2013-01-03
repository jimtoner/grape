package grape.littleendian;

public interface LittleEndianRandomAccessInput extends LittleEndianInput {
	/**
	 * 返回当前指针位置
	 */
	long tell();

	/**
	 * 移动指针
	 */
	long seek(long pos);
}
