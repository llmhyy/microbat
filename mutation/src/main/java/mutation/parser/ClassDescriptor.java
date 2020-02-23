package mutation.parser;

import japa.parser.ast.expr.NameExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hoangtung on 3/31/15.
 */
public class ClassDescriptor {
	private String packageName;
	private String name;
	private int modifier;
	private String superClass;
	private ClassDescriptor outterClass;
	private List<String> implementedInterfaces;
	private List<ClassDescriptor> innerClasses;
	private List<MethodDescriptor> methods;
	
	private List<VariableDescriptor> fields;

	public ClassDescriptor() {
		implementedInterfaces = new ArrayList<String>();
		innerClasses = new ArrayList<ClassDescriptor>();
		methods = new ArrayList<MethodDescriptor>();
		fields = new ArrayList<VariableDescriptor>();
	}

	public String getQuantifiedName() {
		if (outterClass == null)
			return (packageName == null ? "" : packageName + ".") + name;
		else
			return outterClass.getQuantifiedName() + "$" + name;
	}

	public void addMethod(MethodDescriptor md) {
		this.methods.add(md);
		md.setClassDescriptor(this);
	}

	public void register(ClassManager cm) {
		cm.acceptClass(this);

		if (innerClasses != null) {
			for (ClassDescriptor iCl : innerClasses) {
				iCl.register(cm);
			}
		}
	}

	public VariableDescriptor getVarFromName(String quantifiedName,
			int beginLine, int endLine) {
		//TODO LLT;
		return null;
	}

	public VariableDescriptor getVarFromName(NameExpr name) {
		//TODO LLT;
		return null;
	}

	public void addImplementedInterfaces(String name) {
		this.implementedInterfaces.add(name);
	}

	public void addFields(List<VariableDescriptor> vars) {
		this.fields.addAll(vars);
	}

	public void addField(VariableDescriptor var) {
		this.fields.add(var);
	}

	public void addInnerClass(ClassDescriptor inner) {
		this.innerClasses.add(inner);
		inner.setOutterClass(this);
	}
	
	public List<VariableDescriptor> getFields() {
		return fields;
	}

	public void setFields(List<VariableDescriptor> fields) {
		this.fields = fields;
	}
	
	public VariableDescriptor getFieldByName(String name) {
		for (VariableDescriptor field : fields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getModifier() {
		return modifier;
	}

	public void setModifier(int modifier) {
		this.modifier = modifier;
	}

	public String getSuperClass() {
		return superClass;
	}

	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}

	public ClassDescriptor getOutterClass() {
		return outterClass;
	}

	public void setOutterClass(ClassDescriptor outterClass) {
		this.outterClass = outterClass;
	}

	public List<String> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	public void setImplementedInterfaces(List<String> implementedInterfaces) {
		this.implementedInterfaces = implementedInterfaces;
	}

	public List<ClassDescriptor> getInnerClasses() {
		return innerClasses;
	}

	public void setInnerClasses(List<ClassDescriptor> innerClasses) {
		this.innerClasses = innerClasses;
	}

	public List<MethodDescriptor> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodDescriptor> methods) {
		this.methods = methods;
	}

	public List<VariableDescriptor> getVisibleVars(int lineNum) {
		return new ArrayList<VariableDescriptor>();
	}

	public VariableDescriptor getOneVisibleVar(int lineNum) {
		return null;
	}
}
