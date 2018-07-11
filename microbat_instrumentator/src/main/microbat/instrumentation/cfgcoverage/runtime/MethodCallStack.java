package microbat.instrumentation.cfgcoverage.runtime;

import java.util.Stack;

import sav.strategies.dto.AppJavaClassPath;

public class MethodCallStack {
	Stack<String> stack = new Stack<>();
	
	public String safePop() {
		if (stack.size() != 0) {
			return stack.pop();
		}
		return null;
	}

	public String push(String methodId) {
		return stack.push(methodId);
	}
	
	public synchronized String peek() {
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
	public boolean popForException(String methodId, AppJavaClassPath appPath) {
		if(!stack.isEmpty()){
			int popLayer = 0;
			boolean needPop = false;
			
		}
		
		return false;
	}

	public int size() {
		return stack.size();
	}
	
}
