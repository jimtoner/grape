package grape.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 读写 ".ini" 文件的工具
 *
 * @author jingqi
 */
public class ConfigFile {

    /**
     * 每一行是这样构成的
     * space0 key space1 '=' space2 value space3 comment
     */
	private static class Line {
		String space0;
		String key = "";
		String space1;
		boolean equalSign;
		String space2;
		String value;
		String space3;
		String comment;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (space0 != null)
				sb.append(space0);
			if (key != null)
				sb.append(key);
			if (space1 != null)
				sb.append(space1);
			if (equalSign)
				sb.append('=');
			if (space2 != null)
				sb.append(space2);
			if (value != null)
				sb.append(value);
			if (space3 != null)
				sb.append(space3);
			if (comment != null)
				sb.append(comment);
			return sb.toString();
		}
	}

    /**
     * 每个块的头部是这样构成的
     * space0 '[' space1 name space2 ']' space3 comment
     */
	private static class Sector {
		String space0;
		String space1;
		String name;
		String space2;
		String space3;
		String comment;

		final Collection<Line> lines = new ArrayList<Line>();

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (space0 != null)
				sb.append(space0);
			sb.append('[');
			if (space1 != null)
				sb.append(space1);
			if (name != null)
				sb.append(name);
			if (space2 != null)
				sb.append(space2);
			sb.append(']');
			if (space3 != null)
				sb.append(space3);
			if (comment != null)
				sb.append(comment);
			sb.append('\n');

			for (Line l : lines)
				sb.append(l.toString()).append('\n');
			return sb.toString();
		}
	}

	private String filePath;
	private final Collection<Line> globalLines = new ArrayList<Line>();
	private final Collection<Sector> sectors = new ArrayList<Sector>();
	private boolean dirty = false;
	private Charset charset;

	public ConfigFile(String path) throws IOException {
		filePath = path;
		charset = Charset.forName("UTF-8");
		if (!new File(path).exists())
			return;


		Collection<Line> currentLines = globalLines;
		InputStreamReader isr = new InputStreamReader(new FileInputStream(path), charset);
		BufferedReader br = new BufferedReader(isr);
		try {
			String l = br.readLine();
			while (l != null) {
				// 解决跨平台文本文件换行符不同的问题
				while (!l.isEmpty() &&
						(l.charAt(l.length() - 1) == '\r' || l.charAt(l.length() - 1) == '\n'))
					l = l.substring(0, l.length() - 1);

				Sector sec = parseSectorName(l);
				if (sec != null) {
					currentLines = sec.lines;
					sectors.add(sec);
				} else {
					Line line = parseLine(l);
					currentLines.add(line);
				}

				l = br.readLine();
			}
		} finally {
			br.close();
			isr.close();
		}
	}

	private static final String SPACES = " \t";
	private static final String COMMENT_STARTERS = "#;";
	private static final char DEFAULT_LIST_SPLIT_CHAR = ',';

    private static Line parseLine(String line) {
        Line ret = new Line();
        String s = line;

        // 注释
        int index = StringUtil.find_first_of(s, COMMENT_STARTERS);
        if (index >= 0) {
        	ret.comment = s.substring(index);
        	s = s.substring(0, index);
        } else {
        	ret.comment = null;
        }

        // space0
        index = StringUtil.find_first_not_of(s, SPACES);
        if (index >= 0) {
        	ret.space0 = s.substring(0, index);
        	s = s.substring(index);
        } else {
        	ret.space0 = s;
        	s = "";
        }

        // space3
        index = StringUtil.find_last_not_of(s, SPACES);
        if (index >= 0) {
            ret.space3 = s.substring(index + 1);
            s = s.substring(0, index + 1);
        } else {
            ret.space3 = s;
            s = "";
        }

        // '='
        index = s.indexOf('=');
        String strKey, strValue;
        if (index >= 0) {
            ret.equalSign = true;
            strKey = s.substring(0, index);
            strValue = s.substring(index + 1);
        } else {
            ret.equalSign = false;
            strKey = s;
            strValue = "";
        }

        // key, space1
        index = StringUtil.find_last_not_of(strKey, SPACES);
        if (index >= 0) {
            ret.space1 = strKey.substring(index + 1);
            ret.key = strKey.substring(0, index + 1);
        } else {
            ret.space1 = strKey;
            ret.key = "";
        }

        // space2, value
        index = StringUtil.find_first_not_of(strValue, " \t");
        if (index >= 0) {
            ret.space2 = strValue.substring(0, index);
            ret.value = strValue.substring(index);
        } else {
            ret.space2 = strValue;
            ret.value = null;
        }
        return ret;
    }

    private static Sector parseSectorName(String line) {

        int index1 = line.indexOf('[');
        int index2 = line.indexOf(']');
        if (index1 < 0 || index2 < 0 || index1 > index2)
        	return null;
        int index3 = StringUtil.find_first_of(line, COMMENT_STARTERS, index2);

        // 检查 space0
        int index = StringUtil.find_first_not_of(line, SPACES);
        if (index < 0 || index < index1)
        	return null;

        // 检查 space3
        index = StringUtil.find_first_not_of(line, SPACES, index2 + 1);
        if ((index3 < 0 && index >= 0) || (index < index3))
        	return null;

        Sector ret = new Sector();
        ret.space0 = line.substring(0, index1);

        if (index3 >= 0)
            ret.space3 = line.substring(index2 + 1, index3);
        else
            ret.space3 = line.substring(index2 + 1);

        if (index3 >= 0)
            ret.comment = line.substring(index3);
        else
            ret.comment = null;

        ret.name = line.substring(index1 + 1, index2);
        index = StringUtil.find_first_not_of(ret.name, SPACES);
        if (index >= 0) {
            ret.space1 = ret.name.substring(0, index);
            ret.name = ret.name.substring(index);
            index = StringUtil.find_last_not_of(ret.name, SPACES);
            if (index >= 0) {
                ret.space2 = ret.name.substring(index + 1);
                ret.name = ret.name.substring(0, index + 1);
            } else {
                ret.space2 = null;
            }
        } else {
            ret.space1 = ret.name;
            ret.name = null;
            ret.space2 = null;
        }
        return ret;
    }

    public void flush() throws IOException {
    	if (!dirty)
    		return;

    	FileOutputStream fos = null;
    	OutputStreamWriter osw = null;
    	try {
        	fos = new FileOutputStream(filePath);
        	osw = new OutputStreamWriter(fos, charset);

		    // 全局数据
    		for (Line l : globalLines) {
    			osw.write(l.toString());
    			osw.write('\n');
    		}

		    // 各个块
    		for (Sector s : sectors) {
    			osw.write(s.toString());
    		}

		    dirty = false;
    	} finally {
    		// XXX 因为stream之间有一定的依赖顺序，必须按照一定的顺序关闭流
    		if (osw != null)
    			osw.close();
    		if (fos != null)
    			fos.close();
    	}
    }

    public void setDirty() {
    	setDirty(true);
    }

    public void setDirty(boolean d) {
    	dirty = d;
    }

    public String getFilePath() {
    	return filePath;
    }

    /**
     * 清除所有内容
     */
    public void clear() {
    	globalLines.clear();
    	sectors.clear();
    	dirty = true;
    }

    public Collection<String> listSectors() {
    	Collection<String> ret = new ArrayList<String>();
    	for (Sector s : sectors)
    		ret.add(s.name);
    	return ret;
    }

    private Collection<Line> getSectorLines(String sector) {
    	if (sector == null)
    		return globalLines;
    	for (Sector s : sectors)
    		if (s.name.equals(sector))
    			return s.lines;
    	return null;
    }

    /**
     * @param name 为 null 时指代文件起首的全局 sector(无名称)
     */
    public boolean hasSector(String name) {
    	return getSectorLines(name) != null;
    }

    /**
     * @param name 为 null 时指代文件起首的全局 sector(无名称)
     * @return true, 内容被更改
     */
    public boolean removeSector(String name) {
    	if (name == null) {
    		globalLines.clear();
    		dirty = true;
    		return true;
    	}

    	Iterator<Sector> iter = sectors.iterator();
    	while (iter.hasNext()) {
    		Sector s = iter.next();
    		if (s.name.equals(name)) {
    			iter.remove();
    			dirty = true;
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * @param sector 为 null 时指代文件起首的全局 sector(无名称)
     * @return null，则没有找到相应sector
     */
    public Collection<String> listKeys(String sector) {
    	Collection<Line> lines = getSectorLines(sector);
    	if (lines == null)
    		return null;
    	Collection<String> ret = new ArrayList<String>();
    	for (Line l : lines)
    		ret.add(l.key);
    	return ret;
    }

    /**
     * @param sector 为 null 时指代文件起首的全局 sector(无名称)
     */
    public boolean hasKey(String sector, String key) {
    	Collection<Line> lines = getSectorLines(sector);
    	if (lines == null)
    		return false;
    	for (Line l : lines)
    		if (l.key.equals(key))
    			return true;
    	return false;
    }

    public boolean removeKey(String sector, String key) {
    	Collection<Line> lines = getSectorLines(sector);
    	if (lines == null)
    		return false;
    	Iterator<Line> iter = lines.iterator();
    	while (iter.hasNext()) {
    		Line l = iter.next();
    		if (l.key.equals(key)) {
    			iter.remove();
    			dirty = true;
    			return true;
    		}
    	}
    	return false;
    }

    public String getString(String sector, String key) {
    	return getString(sector, key, null);
    }

    public String getString(String sector, String key, String default_value) {
    	Collection<Line> lines = getSectorLines(sector);
    	if (lines == null)
    		return default_value;
    	for (Line l : lines)
    		if (l.key.equals(key))
    			return l.value;
    	return default_value;
    }

    public Boolean getBool(String sector, String key) {
    	return getBool(sector, key, null);
    }

    public Boolean getBool(String sector, String key, Boolean default_value) {
    	String s = getString(sector, key);
    	if (s == null)
    		return default_value;
    	if (s.equals("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes"))
    		return Boolean.TRUE;
    	else if (s.equals("0") || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no"))
    		return Boolean.FALSE;
    	return default_value;
    }

    public Integer getInt(String sector, String key) {
    	return getInt(sector, key, null);
    }

    public Integer getInt(String sector, String key, Integer default_value) {
    	String s = getString(sector, key);
    	if (s == null)
    		return default_value;

    	try {
    		return Integer.valueOf(s);
    	} catch (NumberFormatException e) {
    		return default_value;
    	}
    }

    public Double getDecimal(String sector, String key) {
    	return getDecimal(sector, key, null);
    }

    public Double getDecimal(String sector, String key, Double default_value) {
    	String s = getString(sector, key);
    	if (s == null)
    		return default_value;

    	try {
    		return Double.valueOf(s);
    	} catch (NumberFormatException e) {
    		return default_value;
    	}
    }

    public List<String> getList(String sector, String key) {
    	return getList(sector, key, DEFAULT_LIST_SPLIT_CHAR);
    }

    public List<String> getList(String sector, String key, char splitChar) {
    	String s = getString(sector, key);
    	if (s == null)
    		return null;

    	List<String> ret = new ArrayList<String>();
    	int begin = 0, end = s.indexOf(splitChar);
    	while (end >= 0) {
    		ret.add(s.substring(begin, end));
    		begin = end + 1;
    		end = s.indexOf(splitChar, begin);
    	}
    	if (begin < s.length())
    		ret.add(s.substring(begin));
    	return ret;
    }

    public void setString(String sector, String key, String value) {
    	dirty = true;

    	Collection<Line> lines = getSectorLines(sector);
    	if (lines == null) {
    		Sector s = new Sector();
    		s.name = sector;
    		sectors.add(s);
    		lines = s.lines;
    	}

    	for (Line l : lines) {
    		if (l.key.equals(key)) {
    			l.value = value;
    			return;
    		}
    	}

    	Line l = new Line();
    	l.key = key;
    	l.equalSign = true;
    	l.value = value;
    	lines.add(l);
    }

    public void setBool(String sector, String key, boolean value) {
    	setString(sector, key, value ? "true" : "false");
    }

    public void setInt(String sector, String key, long value) {
    	setString(sector, key, Long.toString(value));
    }

    public void setDecimal(String sector, String key, double value) {
    	setString(sector, key, Double.toString(value));
    }

    public void setList(String sector, String key, List<String> value) {
    	setList(sector, key, value, DEFAULT_LIST_SPLIT_CHAR);
    }

    public void setList(String sector, String key, List<String> value, char splitChar) {
    	StringBuilder sb = new StringBuilder();
    	for (String s : value) {
    		if (sb.length() > 0)
    			sb.append(splitChar);
    		sb.append(s);
    	}
    	setString(sector, key, sb.toString());
    }
}
