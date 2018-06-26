package microbat.instrumentation.runtime;

import java.util.Stack;

import microbat.model.trace.TraceNode;
import sav.strategies.dto.AppJavaClassPath;

public class MethodCallStack {
	Stack<TraceNode> stack = new Stack<>();
	
	public TraceNode safePop() {
		if (stack.size() != 0) {
			return stack.pop();
		}
		return null;
	}

	public TraceNode push(TraceNode node) {
		return stack.push(node);
	}
	
	public synchronized TraceNode peek() {
		if (stack.isEmpty()) {
			return null;
		}
		return stack.peek();
	}
	
	public boolean isEmpty(){
		return stack.isEmpty();
	}

	/**
	 * return whether we need to change the invocation layer structure by exception
	 * @param methodSignature
	 * @return
	 */
	public boolean popForException(String methodSignature, AppJavaClassPath appPath) {
		if(!stack.isEmpty()){
			int popLayer = 0;
			boolean needPop = false;
			
			if(!stack.isEmpty()){
				TraceNode caller = stack.peek();
				String m = caller.getInvokingMethod();
				
				if(m == null || m.equals(methodSignature)){
					return false;
				}
				System.currentTimeMillis();
			}
			
			for(int i=stack.size()-1; i>=0; i--){
				TraceNode caller = stack.get(i);
				popLayer++;
				if(caller.getMethodSign().equals(methodSignature)){
					needPop = true;
					break;
				}
			}
			
			String enterMethodString = appPath.getOptionalTestClass() + "#" + appPath.getOptionalTestMethod();
			if(methodSignature.contains(enterMethodString)) {
				needPop = true;
			}
			
			if(needPop){
				for(int i=0; i<popLayer; i++){
					stack.pop();
				}
				
				return true;
			}
		}
		
		return false;
	}
	
}
