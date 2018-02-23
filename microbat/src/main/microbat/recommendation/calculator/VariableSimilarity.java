package microbat.recommendation.calculator;

public class VariableSimilarity {
	int isSameLocalVarType;
	int isSameLocalVarName;
	
	int isSameFieldParent;
	int isSameFieldType;
	int isSameFieldName;
	
	int isSameArrayParent;
	int isSameArrayType;
	int isSameArrayIndex;
	
	public VariableSimilarity(int isSameLocalVarType, int isSameLocalVarName, int isSameFieldParent,
			int isSameFieldType, int isSameFieldName, int isSameArrayParent, int isSameArrayType,
			int isSameArrayIndex) {
		super();
		this.isSameLocalVarType = isSameLocalVarType;
		this.isSameLocalVarName = isSameLocalVarName;
		this.isSameFieldParent = isSameFieldParent;
		this.isSameFieldType = isSameFieldType;
		this.isSameFieldName = isSameFieldName;
		this.isSameArrayParent = isSameArrayParent;
		this.isSameArrayType = isSameArrayType;
		this.isSameArrayIndex = isSameArrayIndex;
	}
	
	public double computeSimilarity(VariableSimilarity vs){
		double numerator = isSameLocalVarType*vs.isSameLocalVarType + isSameLocalVarName*vs.isSameLocalVarName +
				isSameFieldParent*vs.isSameFieldParent + isSameFieldType*vs.isSameFieldType +
				isSameFieldName*vs.isSameFieldName + isSameArrayParent*vs.isSameArrayParent +
				isSameArrayType*vs.isSameArrayType + isSameArrayIndex*vs.isSameArrayIndex;
		double demoninator = 7;
		
		return numerator/demoninator;
	}
}
