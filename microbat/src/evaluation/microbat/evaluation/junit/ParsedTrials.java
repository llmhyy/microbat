package microbat.evaluation.junit;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import microbat.evaluation.io.ExcelReader;
import microbat.evaluation.model.Trial;

public class ParsedTrials {
	private Set<Trial> trialSet = new HashSet<>();
	
	public ParsedTrials(){
		ExcelReader reader = new ExcelReader();
		try {
			reader.readXLSX();
			trialSet = reader.getSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean contains(Trial trial){
		return trialSet.contains(trial);
		
//		for(Trial t: trialSet){
//			if(t.getTestCaseName().equals(trial)){
//				return true;
//			}
//		}
//		
//		return false;
	}
}
