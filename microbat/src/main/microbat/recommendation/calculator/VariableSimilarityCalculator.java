package microbat.recommendation.calculator;

import java.util.List;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;

public class VariableSimilarityCalculator {
	private VarValue missingAssignValue;
	
	public VariableSimilarityCalculator(VarValue missingAssignVar){
		this.missingAssignValue = missingAssignVar;
	}
	
	public VariableSimilarity[] calculateVarSimilarity(TraceNode node){
		VariableSimilarity[] vs = new VariableSimilarity[2];
		
		VariableSimilarity readVS = findVariableSimilarity(node.getReadVariables());
		vs[0] = readVS;
		
		VariableSimilarity writtenVS = findVariableSimilarity(node.getWrittenVariables());
		vs[1] = writtenVS;
		
		return vs;
	}
	
	private VariableSimilarity findVariableSimilarity(List<VarValue> variables) {
		VariableSimilarity vs = new VariableSimilarity(0, 0, 0, 0, 0, 0, 0, 0, 0);
		if(variables.isEmpty()){
			return vs;
		}
		
		double bestSim = 0;
		for(VarValue value: variables){
			VariableSimilarity tmp = calculateVarSimilarity(value);
			double sim = tmp.computeSimilarity();
			if(sim > bestSim){
				bestSim = sim;
				vs = tmp;
			}
		}
		
		return vs;
	}

	private VariableSimilarity calculateVarSimilarity(VarValue value){
		Variable var = value.getVariable();
		Variable missingAssignVar = this.missingAssignValue.getVariable();
		if(missingAssignVar instanceof FieldVar && var instanceof FieldVar){	
			FieldVar fVar1 = (FieldVar)missingAssignVar;
			FieldVar fVar2 = (FieldVar)var;
			return calculateFieldSimilarity(fVar1, missingAssignValue, fVar2, value);
		}
		else if(missingAssignVar instanceof LocalVar && var instanceof LocalVar){
			LocalVar lVar1 = (LocalVar)missingAssignVar;
			LocalVar lVar2 = (LocalVar)var;
			return calculateLocalVarSimilarity(lVar1, lVar2);
		}
		else if(missingAssignVar instanceof ArrayElementVar && var instanceof ArrayElementVar){
			ArrayElementVar aVar1 = (ArrayElementVar)missingAssignVar;
			ArrayElementVar aVar2 = (ArrayElementVar)var;
			return calculateArrayElementSimilarity(aVar1, missingAssignValue, aVar2, value);
		}
		
		return new VariableSimilarity(0, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	private VariableSimilarity calculateArrayElementSimilarity(ArrayElementVar aVar1, VarValue aValue1,
			ArrayElementVar aVar2, VarValue aValue2) {
		int isSameObject = 0;
		if(!aValue1.getParents().isEmpty() && !aValue2.getParents().isEmpty()){
			VarValue p1 = aValue1.getParents().get(0);
			VarValue p2 = aValue2.getParents().get(0);
			isSameObject = p1.getVarID().equals(p2.getVarID()) ? 1 : 0;
		}
		
		int isSameType = aVar1.getType().equals(aVar2.getType()) ? 1 : 0;
		int isSameName = aVar1.getName().equals(aVar2.getName()) ? 1 : 0;
		
//		return new VariableSimilarity(0, 0, 0, 0, 0, 0, isSameObject, isSameType, isSameName);
		return new VariableSimilarity(0, 0, isSameObject, isSameType, isSameType, isSameName, 0, 0, 0);
	}

	private VariableSimilarity calculateLocalVarSimilarity(LocalVar lVar1, LocalVar lVar2) {
		int isSameType = lVar1.getType().equals(lVar2.getType()) ? 1 : 0;
		int isSameName = lVar1.getName().equals(lVar2.getName()) ? 1 : 0;
		
		return new VariableSimilarity(isSameType, isSameName, 0, 0, 0, 0, 0, 0, 0);
	}

	private VariableSimilarity calculateFieldSimilarity(FieldVar fVar1, VarValue fValue1, FieldVar fVar2, VarValue fValue2) {
		int isSameObject = 0;
		int isSameObjectType = 0;
		if(fValue1.getParents().isEmpty() && fValue2.getParents().isEmpty()){
			isSameObject = fVar1.getDeclaringType().equals(fVar2.getDeclaringType()) ? 1 : 0;
			isSameObjectType = isSameObject;
		}
		else if(!fValue1.getParents().isEmpty() && !fValue2.getParents().isEmpty()){
			VarValue p1 = fValue1.getParents().get(0);
			VarValue p2 = fValue2.getParents().get(0);
			isSameObject = p1.getVarID().equals(p2.getVarID()) ? 1 : 0;
			isSameObjectType = fVar1.getDeclaringType().equals(fVar2.getDeclaringType()) ? 1 : 0;
		}
		
		int isSameType = fVar1.getType().equals(fVar2.getType()) ? 1 : 0;
		int isSameName = fVar1.getName().equals(fVar2.getName()) ? 1 : 0;
		
		
		return new VariableSimilarity(0, 0, isSameObject, isSameObjectType, isSameType, isSameName, 0, 0, 0);
	}
}
