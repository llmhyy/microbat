package microbat.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import microbat.evaluation.junit.TestingMethodChecker;
import sav.strategies.dto.AppJavaClassPath;
import sav.strategies.dto.ClassLocation;

public class JTestUtil {
	public static List<MethodDeclaration> findTestingMethod(CompilationUnit cu) {
		boolean isSubclassOfTestCase = isSubclassOfTestCase(cu);
		
		TestingMethodChecker checker = new TestingMethodChecker(isSubclassOfTestCase);
		cu.accept(checker);
		
		List<MethodDeclaration> testingMethods = checker.getTestingMethods();
		
		return testingMethods;
	}

	public static boolean isSubclassOfTestCase(CompilationUnit cu) {
		if(cu.types().isEmpty()){
			return false;
		}
		
		AbstractTypeDeclaration typeDel = (AbstractTypeDeclaration) cu.types().get(0);
		ITypeBinding binding = typeDel.resolveBinding();
		
		boolean isSubclassOfTestCase = false;
		String parentName = "";
		while(true){
			if(binding == null){
				break;
			}
			
			ITypeBinding superBinding = binding.getSuperclass();
			if(superBinding == null){
				break;
			}
			
			parentName = superBinding.getQualifiedName();
			if(parentName.equals("junit.framework.TestCase")){
				isSubclassOfTestCase = true;
				break;
			}
			
			binding = superBinding;
		}
		
		return isSubclassOfTestCase;
	}

	private static HashMap<String, Boolean> testingClassMap = new HashMap<>();
	
	public static boolean isLocationInTestPackage(ClassLocation location) throws JavaModelException {
		String className = location.getClassCanonicalName();
		Boolean isIn = testingClassMap.get(className);
		if(isIn != null){
			return isIn;
		}
		else{
			IPackageFragmentRoot testRoot = JavaUtil.findTestPackageRootInProject();
			for(IJavaElement ele: testRoot.getChildren()){
				if(ele instanceof IPackageFragment){
					IPackageFragment pack = (IPackageFragment)ele;
					IJavaElement element = find(pack, className);
//					System.currentTimeMillis();
					if(element != null){
						testingClassMap.put(className, true);
						return true;
					}
				}
			}
			
			testingClassMap.put(className, false);
		}
		
		return false;
	}

	private static IJavaElement find(IPackageFragment pack, String className) throws JavaModelException {
		for(IJavaElement ele: pack.getChildren()){
			if(ele instanceof ICompilationUnit){
				ICompilationUnit icu = (ICompilationUnit)ele;
				String name = icu.getElementName();
				name = name.substring(0, name.indexOf(".java"));
				String packName = pack.getElementName();
				
				name = packName + "." + name;
				
				if(name.equals(className)){
					return ele;
				}
			}
			else if(ele instanceof IPackageFragment){
				IJavaElement result = find((IPackageFragment)ele, className);
				if(result != null){
					return result;
				}
			}
		}
		return null;
	}
	
	private static HashSet<String> testClass = new HashSet<>(); 
	
	public static boolean isInTestCase(String className, AppJavaClassPath appPath) {
		if(testClass.contains(className)){
			return true;
		}
		else{
			CompilationUnit cu = JavaUtil.findCompilationUnitInProject(className, appPath);
			List<MethodDeclaration> mdList = JTestUtil.findTestingMethod(cu);
			
			if(!mdList.isEmpty()){
				testClass.add(className);
				return true;
			}
		}
		
		return false;
	}

	public static List<MethodDeclaration> findBeforeAfterMethod(CompilationUnit cu) {
		final List<MethodDeclaration> mdList = new ArrayList<>();
		cu.accept(new ASTVisitor() {
			@SuppressWarnings("rawtypes")
			public boolean visit(MethodDeclaration md){
				ChildListPropertyDescriptor desc = md.getModifiersProperty();
				Object obj = md.getStructuralProperty(desc);
				Field field;
				try {
					field = obj.getClass().getDeclaredField("store");
					field.setAccessible(true);
					ArrayList list = (ArrayList)field.get(obj);
					
					for(Object item: list){
						if(item instanceof MarkerAnnotation){
							MarkerAnnotation annotation = (MarkerAnnotation)item;
							String annName = annotation.getTypeName().getFullyQualifiedName();
							if(annName.equals("Before") || annName.equals("After")){
								mdList.add(md);
							}
						}
					}
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
				
				return false;
			}
		});
		
		return mdList;
	}
}
