package grape.dataio;

public interface RandomAccessInput extends Input {
	/**
	 * 返回当前指针位置
	 */
	long tell();

	/**
	 * 移动指针
	 */
	long seek(long pos);
}
