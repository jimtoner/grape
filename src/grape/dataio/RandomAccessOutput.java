package grape.dataio;

public interface RandomAccessOutput extends DelayableOutput {
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
