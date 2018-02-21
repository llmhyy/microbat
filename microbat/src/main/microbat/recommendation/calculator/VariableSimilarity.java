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
	
	
}
