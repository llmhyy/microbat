package microbat.codeanalysis.bytecode;

import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;

import microbat.model.ClassLocation;

public class MethodFinderByLine extends ByteCodeMethodFinder {
	private ClassLocation point;
	
	public ClassLocation getPoint() {
		return point;
	}

	public void setPoint(ClassLocation point) {
		this.point = point;
	}
	
	public MethodFinderByLine(ClassLocation point){
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
