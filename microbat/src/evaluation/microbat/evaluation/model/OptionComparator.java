package microbat.evaluation.model;

import java.util.Comparator;

import microbat.model.value.VarValue;
import microbat.recommendation.ChosenVariableOption;
import microbat.util.PrimitiveUtils;

/**
 * I prefer simple variable to complex variable
 * 
 * @author Yun Lin
 *
 */
public class OptionComparator implements Comparator<ChosenVariableOption> {

	@Override
	public int compare(ChosenVariableOption o1, ChosenVariableOption o2) {
		
		VarValue readVar1 = o1.getReadVar();
		VarValue readVar2 = o2.getReadVar();
		
		if(readVar1 == null && readVar2 == null){
			return 0;
		}
		else if(readVar1 != null && readVar2 == null){
			return -1;
		}
		else if(readVar1 == null && readVar2 != null){
			return 1;
		}
		else{
			String varType1 = readVar1.getVariable().getType();
			String varType2 = readVar2.getVariable().getType();
			
			if(PrimitiveUtils.isPrimitiveTypeOrString(varType1) 
					&& !PrimitiveUtils.isPrimitiveTypeOrString(varType2)){
				return -1;
			}
			else if(!PrimitiveUtils.isPrimitiveTypeOrString(varType1) 
					&& PrimitiveUtils.isPrimitiveTypeOrString(varType2)){
				return 1;
			}
			else{
				return 0;
			}
		}
		
	}

}
