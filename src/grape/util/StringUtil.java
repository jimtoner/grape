package grape.util;

public class StringUtil {

	private StringUtil() {}

	/**
	 * 在字符串 s 中查找第一个出现在 chars 中的字符
	 */
	public static int find_first_of(String s, String chars) {
		return find_first_of(s, chars, 0);
	}

	public static int find_first_of(String s, String chars, int start) {
		assert start >= 0;
		for (int i = start, len = s.length(); i < len; ++i)
			if (chars.indexOf(s.charAt(i)) >= 0)
				return i;
		return -1;
	}

	/**
	 * 在字符串 s 中查找第一个不出现在 chars 中的字符
	 */
	public static int find_first_not_of(String s, String chars) {
		return find_first_not_of(s, chars, 0);
	}

	public static int find_first_not_of(String s, String chars, int start) {
		assert start >= 0;
		for (int i = start, len = s.length(); i < len; ++i)
			if (chars.indexOf(s.charAt(i)) < 0)
				return i;
		return -1;
	}

	/**
	 * 在字符串 s 中查找最后一个不出现在 chars 中的字符
	 */
	public static int find_last_not_of(String s, String chars) {
		for (int i = s.length() - 1; i >= 0; --i)
			if (chars.indexOf(s.charAt(i)) < 0)
				return i;
		return -1;
	}
}
