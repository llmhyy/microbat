package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.util.JavaUtil;
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
				boolean isStartOfClass = isStartOfClass(breakPoint);
				if(!isStartOfClass){
					List<InstructionHandle> correspondingInstructions = findCorrespondingInstructions(breakPoint.getLineNumber(), code);
					
					if(!correspondingInstructions.isEmpty()){
						String methodSig = breakPoint.getClassCanonicalName() + "#" + method.getName() + method.getSignature();
						if(!(methodSig.contains("class$") && breakPoint.getMethodSign()!=null)) {
							breakPoint.setMethodSign(methodSig);						
						}
						
						parseReadWrittenVariable(breakPoint, correspondingInstructions, code, cfg, appJavaClassPath);
					}
				}
				else{
					breakPoint.setStartOfClass(true);
					breakPoint.setMethodSign(ClassLocation.UNKNOWN_METHOD_SIGN);
				}
			}
			
		}
		
		
    }

	private boolean isStartOfClass(BreakPoint breakPoint) {
		if(breakPoint.getLineNumber()==1) {
			return true;
		}
		
		CompilationUnit cu = JavaUtil.findCompilationUnitInProject(
				breakPoint.getDeclaringCompilationUnitName(), appJavaClassPath);
		final List<TypeDeclaration> list = new ArrayList<>();
		cu.accept(new ASTVisitor() {
			public boolean visit(TypeDeclaration type) {
				list.add(type);
				return true;
			}
		});
		
		for(TypeDeclaration type: list) {
			int startLine = cu.getLineNumber(type.getName().getStartPosition());
			if(breakPoint.getLineNumber()==startLine) {
				return true;
			}
		}
		
		return false;
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
