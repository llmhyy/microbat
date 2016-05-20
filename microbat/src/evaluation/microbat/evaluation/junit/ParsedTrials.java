package microbat.evaluation.junit;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import microbat.evaluation.io.ExcelReader;
import microbat.evaluation.model.Trial;

public class ParsedTrials {
	private Set<Trial> trialSet = new HashSet<>();
	private int startFileOrder;
	
	public ParsedTrials(){
		ExcelReader reader = new ExcelReader();
		try {
			setStartFileOrder(reader.readXLSX());
			trialSet = reader.getSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean contains(Trial trial){
		return trialSet.contains(trial);
	}

	public int getStartFileOrder() {
		return startFileOrder;
	}

	public void setStartFileOrder(int startFileOrder) {
		this.startFileOrder = startFileOrder;
	}
}
