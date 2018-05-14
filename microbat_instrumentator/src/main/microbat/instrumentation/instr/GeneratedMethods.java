package microbat.instrumentation.instr;

import java.util.List;

import org.apache.bcel.generic.MethodGen;

import sav.common.core.utils.CollectionUtils;

public class GeneratedMethods {
	private MethodGen rootMethod;
	private List<MethodGen> extractedMethods;
	
	public GeneratedMethods(MethodGen rootMethod) {
		this.rootMethod = rootMethod;
	}

	public MethodGen getRootMethod() {
		return rootMethod;
	}

	public void setRootMethod(MethodGen rootMethod) {
		this.rootMethod = rootMethod;
	}

	public List<MethodGen> getExtractedMethods() {
		return CollectionUtils.nullToEmpty(extractedMethods);
	}

	public void setExtractedMethods(List<MethodGen> extractedMethods) {
		this.extractedMethods = extractedMethods;
	}

}
