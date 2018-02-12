package mutation.mutator;

import japa.parser.ast.type.Type;

import java.util.ArrayList;
import java.util.List;

import mutation.parser.ClassDescriptor;
import mutation.parser.LocalVariable;
import mutation.parser.MethodDescriptor;
import mutation.parser.VariableDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sav.common.core.utils.Assert;

public class VariableSubstitution {
	private static Logger log = LoggerFactory.getLogger(VariableSubstitution.class);
	private ClassDescriptor descriptor;
	
	public VariableSubstitution(ClassDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	public Type getType(String varName, int lineNumber, int col){
		Type type = searchFieldType(varName);
		if (type != null) {
			return type;
		}
		type = searchVarTypeInMethods(lineNumber, varName, descriptor.getMethods());
		if(type != null){
			return type;
		}
		
		for(ClassDescriptor innerClass: descriptor.getInnerClasses()){
			type = searchVarTypeInMethods(lineNumber, varName, innerClass.getMethods());
			if(type != null){
				return type;
			}
		}
		return null;
	}
	
	private Type searchFieldType(String fieldName) {
		for (VariableDescriptor var : descriptor.getFields()) {
			if (var.getName().equals(fieldName)) {
				return var.getType();
			}
		}
		return null;
	}

	private Type searchVarTypeInMethods(int lineNumber, String varName,
			List<MethodDescriptor> methods) {
		for(MethodDescriptor method: methods){
			if(method.containsLine(lineNumber)){
				Type type = searchVarTypeInScopes(lineNumber, varName, method.getLocalVars());
				if(type != null){
					return type;
				}
			}
		}
		
		return null;
	}

	private Type searchVarTypeInScopes(int lineNumber, String varName,
			List<LocalVariable> scopes) {
		for(LocalVariable scope: scopes){
			if(scope.containsLine(lineNumber)){
				for(VariableDescriptor localVarInMethod: scope.getVars().values()){
					if(localVarInMethod.getName().equals(varName)){
						return localVarInMethod.getType();
					}
				}
			}
		}
		
		return null;
	}

	public List<VariableDescriptor> findSubstitutions(String varName, int line, int col) {
		Type type = getType(varName, line, col);
		List<VariableDescriptor> result = new ArrayList<VariableDescriptor>();
		/* we cannot get type if varName is not a valid variable, or 
		 * there's something wrong with getType function.
		 */
		if (type == null) {
			log.warn("Cannot specify type for variable with name", varName,
					"in", descriptor.getName());
			return result;
		}
		result.addAll(findVariables(descriptor, type, line, col));
		for(ClassDescriptor innerClass: descriptor.getInnerClasses()){
			result.addAll(findVariables(innerClass, type, line, col));
		}
		return result;
	}
	
	public List<VariableDescriptor> findSubstitutions(Type type, int line, int col) {
		Assert.assertTrue(type != null, "Cannot find substitution, type = null");
		List<VariableDescriptor> result = findVariables(descriptor, type, line, col);
		for(ClassDescriptor innerClass: descriptor.getInnerClasses()){
			result.addAll(findVariables(innerClass, type, line, col));
		}
		return result;
	}
	
	private static List<VariableDescriptor> findVariables(ClassDescriptor classDescriptor,
			Type type, int line, int col) {
		
		List<VariableDescriptor> result = new ArrayList<VariableDescriptor>();
		
		for(VariableDescriptor field: classDescriptor.getFields()){
			if(field.getType().equals(type)){
				result.add(field);
			}
		}
		
		for(MethodDescriptor method: classDescriptor.getMethods()){
			if(method.containsLine(line)){
				result.addAll(findVariables(method.getLocalVars(), type, line, col));
			}
		}
		return result;
	}

	private static List<VariableDescriptor> findVariables(List<LocalVariable> scopes,
			Type type, int lineNumber, int column) {
		List<VariableDescriptor> result = new ArrayList<VariableDescriptor>();
		for (LocalVariable scope : scopes) {
			if (scope.containsLine(lineNumber)) {
				for (VariableDescriptor localVarInMethod : scope.getVars()
						.values()) {
					if (isVisibleAndMatchType(localVarInMethod, lineNumber,
							column, type)) {
						result.add(localVarInMethod);
					}
				}
			}
		}
		return result;
	}

	private static boolean isVisibleAndMatchType(VariableDescriptor localVarInMethod, int lineNumber, int column, Type type){
		return localVarInMethod.getPosition().declaredBefore(lineNumber, column) && localVarInMethod.getType().equals(type);
	}
}
