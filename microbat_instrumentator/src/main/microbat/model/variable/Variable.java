package microbat.model.variable;

import java.io.Serializable;

public abstract class Variable implements Serializable {
	private static final long serialVersionUID = -1126075634497698926L;

	public static final String UNKNOWN_TYPE = "unknown type";
	
	public static String READ = "read";
	public static String WRITTEN = "written";
	
	protected String type;
	protected String variableName;
	
	/**
	 * the JVM heap address
	 */
	protected String aliasVarID;
	
	/**
	 * The id of an object (non-primitive type) is its object id + the order of trace node defining it, 
	 * e.g., 100.a:33 . 
	 * <br><br>
	 * If a variable is a non-static field, its id is: its parent's object id + field name + the order of trace node defining it, 
	 * e.g., 100.a:33 ;
	 * if it is a static field, its id is: its field name + the order of trace node defining it,
	 * e.g., Class.a:33;
	 * if it is an array element, its id is: its parent's object id + index + the order of trace node defining it,
	 * e.g., 100[1]:33 ;
	 * if it is a local variable, its id is: its scope (i.e., class[startLine, endLine]) + variable name 
	 * + invocation_layer + the order of trace node defining it, invocation_layer is for distinguish recursive
	 * methods.
	 * e.g., com/Main{12, 21}a-3:33 ;
	 * if it is a virtual variable, its id is: "virtualVar" + the order of the relevant return-trace-node. 
	 * <br><br>
	 * Note that if the user want to concanate a variable ID, such as local variable ID, field ID, etc. He
	 * or she should use the following three static method: <br>
	 * 
	 * <code>Variable.concanateFieldVarID()</code><br>
	 * <code>Variable.concanateArrayElementVarID()</code><br>
	 * <code>Variable.concanateLocalVarID()</code><br>
	 */
	protected String varID;

	public Variable(String name, String type){
		this.variableName = name;
		this.type = type;
	}
	
	public String getName() {
		return variableName;
	}

	public void setName(String variableName) {
		this.variableName = variableName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getVarID() {
		return varID;
	}

	/**
	 * Note that if the user want to concanate a variable ID, such as local variable ID, field ID, etc. He
	 * or she should use the following three static method: <br>
	 * 
	 * <code>Variable.concanateFieldVarID()</code><br>
	 * <code>Variable.concanateArrayElementVarID()</code><br>
	 * <code>Variable.concanateLocalVarID()</code><br>
	 * 
	 * @param varID
	 */
	public void setVarID(String varID) {
		this.varID = varID;
	}
	
	public static String concanateFieldVarID(String parentID, String fieldName){
		return parentID + "." + fieldName;
	}
	
	public static String concanateArrayElementVarID(String parentID, String indexValueString){
		return parentID + "[" + indexValueString + "]";
	}
	
	public static String concanateLocalVarID(String className, String varName, int startLine, int endLine){
		String clazzName = className.replace(".", "/");
		return clazzName + "{" + startLine + "," + endLine + "}" + varName;	
	}
	
	public static String truncateSimpleID(String completeVarID){
		if(completeVarID==null){
			return null;
		}
		
		if(completeVarID.contains(":")){
			String simpleID = completeVarID.substring(0, completeVarID.indexOf(":"));
			return simpleID;			
		}
		else{
			return completeVarID;
		}
	}
	
	public static String truncateStepOrder(String completeVarID) {
		if(completeVarID.contains(":")){
			String order = completeVarID.substring(completeVarID.indexOf(":")+1, completeVarID.length());
			return order;			
		}
		else{
			return "";
		}
	}
	
	public String getAliasVarID() {
		return aliasVarID;
	}

	public void setAliasVarID(String aliasVarID) {
		this.aliasVarID = aliasVarID;
	}
	
//	public static boolean isPrimitiveVariable(String varID){
//		if(varID.contains("[") || varID.contains(".")){
//			return true;
//		}
//		return false;
//	}

	public abstract String getSimpleName();
	public abstract Variable clone();
}
