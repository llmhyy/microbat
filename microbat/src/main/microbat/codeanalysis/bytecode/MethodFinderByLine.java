package microbat.codeanalysis.bytecode;

import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;

import microbat.model.BreakPoint;

public class MethodFinderByLine extends ByteCodeMethodFinder {
	private BreakPoint point;
	
	public BreakPoint getPoint() {
		return point;
	}

	public void setPoint(BreakPoint point) {
		this.point = point;
	}
	
	public MethodFinderByLine(BreakPoint point){
		this.setPoint(point);
	}
	
	public void visitMethod(Method method){
		if(method.getLineNumberTable()!=null){
			for(LineNumber lineNumber: method.getLineNumberTable().getLineNumberTable()){
				if(lineNumber.getLineNumber()==point.getLineNumber()){
					this.setMethod(method);
				}
			}			
		}
	}
}
