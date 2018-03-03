package microbat.recommendation.calculator;

public class VariableSimilarity {
	public int isSameLocalVarType;
	public int isSameLocalVarName;
	
	public int isSameFieldParent;
	public int isSameFieldParentType;
	public int isSameFieldType;
	public int isSameFieldName;
	
	public int isSameArrayParent;
	public int isSameArrayType;
	public int isSameArrayIndex;
	
	public VariableSimilarity(int isSameLocalVarType, int isSameLocalVarName, int isSameFieldParent, int isSameFieldParentType,
			int isSameFieldType, int isSameFieldName, int isSameArrayParent, int isSameArrayType,
			int isSameArrayIndex) {
		super();
		this.isSameLocalVarType = isSameLocalVarType;
		this.isSameLocalVarName = isSameLocalVarName;
		this.isSameFieldParent = isSameFieldParent;
		this.isSameFieldParentType = isSameFieldParentType;
		this.isSameFieldType = isSameFieldType;
		this.isSameFieldName = isSameFieldName;
		this.isSameArrayParent = isSameArrayParent;
		this.isSameArrayType = isSameArrayType;
		this.isSameArrayIndex = isSameArrayIndex;
	}
	
	public double computeSimilarity(){
		double numerator = isSameLocalVarType + isSameLocalVarName +
				isSameFieldParent + isSameFieldType + isSameFieldParentType +
				isSameFieldName + isSameArrayParent +
				isSameArrayType + isSameArrayIndex;
		double demoninator = 8;
		
		return numerator/demoninator;
	}
}
