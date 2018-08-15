package microbat.tools;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import microbat.instrumentation.cfgcoverage.runtime.CoverageTracer;
import microbat.instrumentation.cfgcoverage.runtime.ICoverageTracer;
import microbat.instrumentation.precheck.TraceMeasurement;
import microbat.instrumentation.runtime.ExecutionTracer;
import microbat.instrumentation.runtime.IExecutionTracer;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.SignatureUtils;

public class CodeGenerator {

	@Test
	public void generateCode_ExecutionTracerIdx() {
		Method[] ms = ExecutionTracer.class.getMethods();
		Method[] ifaceMs = IExecutionTracer.class.getMethods();
		List<String> iMs = new ArrayList<>();
		for (Method m : ifaceMs) {
			iMs.add(m.getName());
		}
		List<Method> methods = CollectionUtils.toArrayList(ms);
		Collections.sort(methods, new Comparator<Method>() {

			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (int i = 0; i < methods.size(); i++) {
			Method method = methods.get(i);
			if (!method.getName().startsWith("_")) {
				continue;
			}
			boolean ifaceMethod = iMs.contains(method.getName());
			
			String className = IExecutionTracer.class.getName();
			if (CollectionUtils.existIn(method.getName(), "_getTracer", "_start")) {
				className = ExecutionTracer.class.getName();
				ifaceMethod = false;
			}
			String format = "%s(%s, \"%s\", \"%s\", \"%s\", %d),";
			String methodName = method.getName();
			char[] charArray = methodName.toCharArray();
			StringBuilder enumType = new StringBuilder();
			for (int j = 1; j < charArray.length; j++) {
				char ch = charArray[j];
				if (Character.isUpperCase(ch)) {
					enumType.append("_").append(ch);
				} else {
					enumType.append(ch);
				}
			}
			String signature = SignatureUtils.getSignature(method);
			System.out.println(String.format(format, 
					enumType.toString().toUpperCase(),
					String.valueOf(ifaceMethod),
					className.replace(".", "/"),
					method.getName(),
					signature,
					method.getParameterTypes().length + 1));
		}
	}
	
	@Test
	public void generateCode_Measurement() {
		Method[] ms = TraceMeasurement.class.getMethods();
		List<Method> methods = CollectionUtils.toArrayList(ms);
		Collections.sort(methods, new Comparator<Method>() {

			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (int i = 0; i < methods.size(); i++) {
			Method method = methods.get(i);
			if (!method.getName().startsWith("_")) {
				continue;
			}
			String format = "%s(\"%s\", \"%s\", \"%s\"),";
			String methodName = method.getName();
			char[] charArray = methodName.toCharArray();
			StringBuilder enumType = new StringBuilder();
			for (int j = 1; j < charArray.length; j++) {
				char ch = charArray[j];
				if (Character.isUpperCase(ch)) {
					enumType.append("_").append(ch);
				} else {
					enumType.append(ch);
				}
			}
			String signature = SignatureUtils.getSignature(method);
			System.out.println(String.format(format, 
					enumType.toString().toUpperCase(),
					TraceMeasurement.class.getName().replace(".", "/"),
					method.getName(),
					signature));
		}
	}
	
	@Test
	public void generateCode_CoverageTracerIdx() {
		Class<?> tracerClass = CoverageTracer.class;
		Class<?> tracerInterface = ICoverageTracer.class;
		Method[] ms = tracerClass.getMethods();
		Method[] ifaceMs = tracerInterface.getMethods();
		List<String> iMs = new ArrayList<>();
		for (Method m : ifaceMs) {
			iMs.add(m.getName());
		}
		List<Method> methods = CollectionUtils.toArrayList(ms);
		Collections.sort(methods, new Comparator<Method>() {

			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (int i = 0; i < methods.size(); i++) {
			Method method = methods.get(i);
			if (!method.getName().startsWith("_")) {
				continue;
			}
			boolean ifaceMethod = iMs.contains(method.getName());
			
			String className = tracerInterface.getName();
			if (Modifier.isStatic(method.getModifiers())) {
				className = tracerClass.getName();
				ifaceMethod = false;
			}
			if (CollectionUtils.existIn(method.getName(), "_getTracer", "_start")) {
			}
			String format = "%s(%s, \"%s\", \"%s\", \"%s\", %d),";
			String methodName = method.getName();
			char[] charArray = methodName.toCharArray();
			StringBuilder enumType = new StringBuilder();
			for (int j = 1; j < charArray.length; j++) {
				char ch = charArray[j];
				if (Character.isUpperCase(ch)) {
					enumType.append("_").append(ch);
				} else {
					enumType.append(ch);
				}
			}
			String signature = SignatureUtils.getSignature(method);
			System.out.println(String.format(format, 
					enumType.toString().toUpperCase(),
					String.valueOf(ifaceMethod),
					className.replace(".", "/"),
					method.getName(),
					signature,
					method.getParameterTypes().length + 1));
		}
	}
	
}
