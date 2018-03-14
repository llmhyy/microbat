package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import microbat.model.BreakPoint;

public class MethodFinderByLine extends ByteCodeMethodFinder {
	private BreakPoint point;
	private List<InstructionHandle> handles = new ArrayList<>();
	
	
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
			
			if(method.getCode()!=null){
				InstructionList insList = new InstructionList(method.getCode().getCode());
				for(InstructionHandle handle: insList){
					int line = method.getLineNumberTable().getSourceLine(handle.getPosition());
					if(line == point.getLineNumber()){
						this.handles.add(handle);
					}
				}
				
			}
		}
	}
}
