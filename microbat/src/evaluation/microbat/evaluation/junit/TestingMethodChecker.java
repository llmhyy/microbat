package microbat.evaluation.junit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import microbat.util.MicroBatUtil;

public class TestingMethodChecker extends ASTVisitor {
	private boolean isSubclassOfTestCase;
	private ArrayList<MethodDeclaration> testingMethods = new ArrayList<>();
	
	public TestingMethodChecker(boolean isSubclassOfTestCase) {
		super();
		this.isSubclassOfTestCase = isSubclassOfTestCase;
	}


	public boolean visit(MethodDeclaration md){
		
		if(isSubclassOfTestCase){
			String methodName = md.getName().getIdentifier();
			if(methodName.startsWith("test")){
				testingMethods.add(md);
				return false;
			}
		}
		else{
			ChildListPropertyDescriptor descriptor = md.getModifiersProperty();
			Object obj = md.getStructuralProperty(descriptor);
			List<ASTNode> methodModifiers = MicroBatUtil.asT(obj);
			
			for(ASTNode node: methodModifiers){
				if(node instanceof MarkerAnnotation){
					MarkerAnnotation annotation = (MarkerAnnotation)node;
					String name = annotation.getTypeName().getFullyQualifiedName();
					if(name != null && name.equals("Test")){
						testingMethods.add(md);
						return false;
					}
				}
			}
		}
		
		
		return false;
	}


	public ArrayList<MethodDeclaration> getTestingMethods() {
		return testingMethods;
	}
}
