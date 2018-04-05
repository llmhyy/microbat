package microbat.codeanalysis.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.ReturnInstruction;

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
			
			int min = -1;
			int max = -1;
			for(LineNumber lineNumber: method.getLineNumberTable().getLineNumberTable()){
				if(min==-1){
					min = lineNumber.getLineNumber();
				}
				else if(min>lineNumber.getLineNumber()){
					min = lineNumber.getLineNumber();
				}
				
				if(max==-1){
					max = lineNumber.getLineNumber();
				}
				else if(max<lineNumber.getLineNumber()){
					max = lineNumber.getLineNumber();
				}
			}
			
			if(min<=point.getLineNumber() && point.getLineNumber()<=max){
				this.setMethod(method);
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
	
	public boolean isThrow(){
		for(InstructionHandle handle: handles){
			Instruction ins = handle.getInstruction();
			if(ins instanceof ATHROW){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isReturn(){
		for(InstructionHandle handle: handles){
			Instruction ins = handle.getInstruction();
			if(ins instanceof ReturnInstruction){
				return true;
			}
		}
		
		return false;
	}

	public List<InstructionHandle> getHandles() {
		return handles;
	}

	public void setHandles(List<InstructionHandle> handles) {
		this.handles = handles;
	}
}
