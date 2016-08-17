package mutation;

import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.PrimitiveType.Primitive;

import java.util.List;

import mutation.mutator.VariableSubstitution;
import mutation.parser.ClassAnalyzer;
import mutation.parser.ClassDescriptor;
import mutation.parser.IJavaParser;
import mutation.parser.JParser;
import mutation.parser.VariableDescriptor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sav.common.core.utils.CollectionUtils;

public class VariableSubstitutionTest {
	private List<ClassDescriptor> descriptors;
	private VariableSubstitution variableSubstitution;
	
	@Before
	public void setup() {
		String sourceFolder = "./src/test/resources";
		String className = "mutation.mutator.VariableSubstitutionClass";
		
		IJavaParser javaParser = new JParser(sourceFolder , CollectionUtils.listOf(className));
		ClassAnalyzer analyzer = new ClassAnalyzer(sourceFolder, javaParser);
		descriptors = analyzer.analyzeCompilationUnit(javaParser.parse(className));
		variableSubstitution = new VariableSubstitution(descriptors.get(0));
	}

	@Test
	public void whenLocalVariableInSameScopeMatch(){
		List<VariableDescriptor> candidates = variableSubstitution
				.findSubstitutions(new PrimitiveType(Primitive.Int), 15,
						Integer.MAX_VALUE);

		Assert.assertTrue(containsVar(candidates, "x1"));
		Assert.assertTrue(containsVar(candidates, "x2"));
	}
	
	@Test
	public void whenlocalVariableInSameScopeNotVisible() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Int), 13, Integer.MAX_VALUE);
		
		Assert.assertFalse(containsVar(candidates, "x2"));
	}
	
	@Test
	public void whenlocalVariableInSameScopeNotMatch() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Int), 13, Integer.MAX_VALUE);
		
		Assert.assertFalse(containsVar(candidates, "x0"));
	}

	@Test
	public void whenLocalVariableInParentScopeMatch() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Double), 25, Integer.MAX_VALUE);
		
		Assert.assertTrue(containsVar(candidates, "y2"));
	}

	@Test
	public void whenLocalVariableInParentScopeNotMatch() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Double), 25, Integer.MAX_VALUE);
		
		Assert.assertFalse(containsVar(candidates, "y1"));
	}

	@Test
	public void whenLocalVariableInChildScopeMatch() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Double), 22, Integer.MAX_VALUE);
		
		Assert.assertTrue(containsVar(candidates, "y2"));
		Assert.assertFalse(containsVar(candidates, "y3"));
	}
	
	@Test
	public void whenLocalVariableInChildScopeButSameLineMatch() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Int), 38, 21);
		
		Assert.assertFalse(containsVar(candidates, "p2"));
	}
	
	@Test
	public void whenClassFieldMatch() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Int), 15, Integer.MAX_VALUE);
		
		Assert.assertTrue(containsVar(candidates, "a"));
		Assert.assertTrue(containsVar(candidates, "c"));
		
	}

	@Test
	public void whenClassFieldNotMatch() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Int), 15, Integer.MAX_VALUE);
		
		Assert.assertFalse(containsVar(candidates, "b"));
		Assert.assertFalse(containsVar(candidates, "d"));
	}

	@Test
	public void whenOuterClassFieldMatch() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Double), 33, Integer.MAX_VALUE);
		
		Assert.assertTrue(containsVar(candidates, "z2"));
		Assert.assertTrue(containsVar(candidates, "z3"));
		Assert.assertTrue(containsVar(candidates, "b"));
	}

	@Test
	public void whenOuterClassFieldNotMatch() {
		List<VariableDescriptor> candidates = variableSubstitution.findSubstitutions(new PrimitiveType(Primitive.Double), 33, Integer.MAX_VALUE);
		
		Assert.assertFalse(containsVar(candidates, "a"));
	}

	private boolean containsVar(List<VariableDescriptor> candidates, String varName){
		for(VariableDescriptor candidate: candidates){
			if(candidate.getName().equals(varName)){
				return true;
			}
		}
		
		return false;
	}
}
