package grape.littleendian;

/**
 * 随机写流
 */
public interface LittleEndianRandomAccessOutput extends
		DelayableLittleEndianOutput {
	/**
	 * 返回当前指针位置
	 */
	long tell();

	/**
	 * 已经写入的总长度
	 */
	long length();

	/**
	 * 移动指针(从起始位置写入时覆盖原有数据)
	 */
	long seek(long pos);
}
