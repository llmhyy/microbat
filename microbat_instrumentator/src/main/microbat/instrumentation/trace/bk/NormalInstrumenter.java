package microbat.instrumentation.trace.bk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.CodeConverter;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.MethodInfo;
import javassist.compiler.Javac;
import javassist.expr.ExprEditor;
import microbat.instrumentation.trace.model.AccessVariableInfo;
import microbat.model.variable.Variable;
import sav.common.core.utils.StringUtils;

public class NormalInstrumenter implements ITraceInstrumenter {
	private IAccessVariableCollector accVarCollector = new AccessVariableCollector();
	
	@Override
	public void visitClass(String className) {
		accVarCollector = new BcelAccessVariableCollector(className);
	}
	
	public void instrument(CtBehavior method) throws CannotCompileException {
		MethodInfo methodInfo = method.getMethodInfo();
		System.out.println(methodInfo.getName());
		CodeAttribute codeAttr = methodInfo.getCodeAttribute();
		CodeIterator iterator = codeAttr.iterator();
		Map<Integer, AccessVariableInfo> lineInfos = new LinkedHashMap<>();
		while (iterator.hasNext()) {
			try {
				int pos = iterator.next();
				int lineNo = methodInfo.getLineNumber(pos);
				AccessVariableInfo lineInfo = lineInfos.get(lineNo);
				if (lineInfo == null) {
					lineInfo = new AccessVariableInfo(lineNo);
					lineInfos.put(lineNo, lineInfo);
				}
				accVarCollector.collectVariable(iterator, pos, methodInfo.getConstPool(), lineInfo);
			} catch (BadBytecode e) {
				throw new CannotCompileException(e);
			}
		}
		/* instrument */
		TraceCodeConverter codeConverter = new TraceCodeConverter(lineInfos.values());
		method.instrument(codeConverter);
		method.instrument(new ExprEditor() {
			
		});
	}
	
	private static class TraceCodeConverter extends CodeConverter {
		private Collection<AccessVariableInfo> lineInfos;
		
		public TraceCodeConverter(Collection<AccessVariableInfo> lineInfos) {
			this.lineInfos = lineInfos;
		}
		
		@Override
		protected void doit(CtClass clazz, MethodInfo methodInfo, ConstPool cp) throws CannotCompileException {
			CodeAttribute ca = methodInfo.getCodeAttribute();
	        if (ca == null)
	            throw new CannotCompileException("no method body");

	        LineNumberAttribute ainfo
	            = (LineNumberAttribute)ca.getAttribute(LineNumberAttribute.tag);
	        if (ainfo == null)
	            throw new CannotCompileException("no line number info");
	        
	        CtClass cc = clazz;
	        CodeIterator iterator = ca.iterator();
			for (AccessVariableInfo lineInfo : lineInfos) {
				try {
					int index = lineInfo.getInsertPc();
					Javac jv = new Javac(cc);
					jv.recordLocalVariables(ca, index);
					jv.recordParams(getParameterTypes(methodInfo, clazz), Modifier.isStatic(getModifiers(methodInfo)));
					jv.setMaxLocals(ca.getMaxLocals());
					jv.compileStmnt(getTraceInstrCode(lineInfo));
					Bytecode b = jv.getBytecode();
					int locals = b.getMaxLocals();
					int stack = b.getMaxStack();
					ca.setMaxLocals(locals);

					/*
					 * We assume that there is no values in the operand stack at
					 * the position where the bytecode is inserted.
					 */
					if (stack > ca.getMaxStack())
						ca.setMaxStack(stack);

					index = iterator.insertAt(index, b.get());
					iterator.insert(b.getExceptionTable(), index);
					methodInfo.rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private String getTraceInstrCode(AccessVariableInfo accVarInfo) {
			StringBuilder sb = new StringBuilder();
 			sb.append("microbat.instrumentation.trace.data.RuntimeDataStore.getStore().store(")
 				.append(accVarInfo.getLineNo()).append(", ")
				.append("\"").append(accVarInfo.getReadString()).append("\", ")
				.append("\"").append(accVarInfo.getWrittenString()).append("\", ");
 			List<String> allVarNames = new ArrayList<String>(accVarInfo.getAllVarNames().keySet());
			if (allVarNames.isEmpty()) {
 				sb.append("null, null");
 			} else {
 				sb.append("new java.lang.String[]{").append(StringUtils.toJoinStrCode(allVarNames)).append("}, ")
// 				.append("new Object[]{").append(StringUtils.join(accVarInfo.getAllVarNames(), ", ")).append("}");
 				.append("new Object[]{$1}");
 			}
 			sb.append(");");
			return sb.toString();
		}
		
		private int getModifiers(MethodInfo methodInfo) {
			return AccessFlag.toModifier(methodInfo.getAccessFlags());
		}

		private CtClass[] getParameterTypes(MethodInfo methodInfo, CtClass clazz) throws NotFoundException {
			return Descriptor.getParameterTypes(methodInfo.getDescriptor(), clazz.getClassPool());
		}
	}
	
}
