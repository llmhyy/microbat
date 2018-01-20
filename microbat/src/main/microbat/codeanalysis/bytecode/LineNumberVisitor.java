package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;

import microbat.model.BreakPoint;
import sav.strategies.dto.AppJavaClassPath;

/**
 * collect information such as method signature, and read/written variables in a certain line.
 * 
 * @author Yun Lin
 *
 */
public class LineNumberVisitor extends ByteCodeVisitor {
	
	private List<BreakPoint> breakPoints;
	private AppJavaClassPath appJavaClassPath;
	
	public LineNumberVisitor(List<BreakPoint> breakPoints, AppJavaClassPath appClassPath) {
		super();
		this.breakPoints = breakPoints;
		this.appJavaClassPath = appClassPath;
	}

	
	public void visitMethod(Method method){
		Code code = method.getCode();
		
		if(code == null){
			return;
		}
		
		List<BreakPoint> includedBreakPoints = findIncludeBreakPoints(code, this.breakPoints);
		if(includedBreakPoints.isEmpty()){
			return;
		}
		
		CFG cfg = new CFGConstructor().buildCFGWithControlDomiance(code);
		if(code != null){
			for(BreakPoint breakPoint: breakPoints){
				
				List<InstructionHandle> correspondingInstructions = findCorrespondingInstructions(breakPoint.getLineNumber(), code);
				
				if(!correspondingInstructions.isEmpty()){
					String methodSig = breakPoint.getClassCanonicalName() + "#" + method.getName() + method.getSignature();
					if(!(methodSig.contains("class$") && breakPoint.getMethodSign()!=null)) {
						breakPoint.setMethodSign(methodSig);						
					}
					
					parseReadWrittenVariable(breakPoint, correspondingInstructions, code, cfg, appJavaClassPath);
				}
				
			}
			
		}
		
		
    }

	private List<BreakPoint> findIncludeBreakPoints(Code code, List<BreakPoint> breakPoints) {
		List<BreakPoint> includedPoints = new ArrayList<>();
		
		LineNumber[] table = code.getLineNumberTable().getLineNumberTable();
		
		int startLine = table[0].getLineNumber()-1;
		int endLine = table[table.length-1].getLineNumber();
		
		for(BreakPoint point: breakPoints){
			if(startLine<=point.getLineNumber() && point.getLineNumber()<=endLine){
				includedPoints.add(point);
			}
		}
		
		return includedPoints;
	}


	
	
}
