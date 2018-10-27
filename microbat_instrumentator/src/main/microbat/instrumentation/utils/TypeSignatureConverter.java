package microbat.instrumentation.utils;

public class TypeSignatureConverter {
	private TypeSignatureConverter() {
	}

	public static String convertToClassName(String classSignature) {
		String name = classSignature.replace("/", ".");
		if (name.startsWith("[")) {
			 return name;
		} else if (name.startsWith("L") && name.endsWith(";")){
			return name.substring(1, name.length() - 1);
		}
		
		if (name.equals("I")) {
			return "int";
		} else if (name.equals("B")) {
			return "byte";
		} else if (name.equals("J")) {
			return "long";
		} else if (name.equals("F")) {
			return "float";
		} else if (name.equals("D")) {
			return "double";
		} else if (name.equals("S")) {
			return "short";
		} else if (name.equals("C")) {
			return "char";
		} else if (name.equals("Z")) {
			return "boolean";
		}
		
		return name;
	}
}
