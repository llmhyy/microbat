package microbat.handler.xml;

public class XmlFilter {
	public static final String SPECIAL_STRING_PREFIX = "$__byteArr_";
	
	public static String filter(String in) {
		if (in == null || in.isEmpty()) {
			return "";
		}
		char c;
		boolean valid = true;
		for (int i = 0; i < in.length(); i++) {
			c = in.charAt(i);
			if (!XMLChar.isName(c)) {
				valid = false;
				break;
			}
		}
		if (valid) {
			return in;
		}
		return convert(in);
	}

	public static String convert(String strVal) {
		// convert to byteArray
		StringBuilder sb = new StringBuilder();
		sb.append(SPECIAL_STRING_PREFIX);
		byte[] bytes = strVal.getBytes();
		for (int i = 0; i < bytes.length;) {
			sb.append(bytes[i]);
			if ((i++) != bytes.length) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public static String getValue(String str) {
		if (str != null && str.startsWith(SPECIAL_STRING_PREFIX)) {
			str = str.substring(SPECIAL_STRING_PREFIX.length());
			String[] bytesArr = str.split(",");
			byte[] bytes = new byte[bytesArr.length];
			for (int i = 0; i < bytesArr.length; i++) {
				bytes[i] = Byte.valueOf(bytesArr[i]);
			}
			return new String(bytes);
		}
		return str;
	}

}
