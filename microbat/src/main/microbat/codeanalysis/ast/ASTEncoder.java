package microbat.codeanalysis.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

public class ASTEncoder {
	public static List<String> abstractASTNodeList = new ArrayList<>();
	public static Map<String, Integer> abstractASTMap = new HashMap<>();
	
	public static int baseASTNodeNumber = 92;
	
	public static int getDimensions(){
		return baseASTNodeNumber + abstractASTNodeList.size();
	}
	
	public static String getAbstractASTType(int index){
		return abstractASTNodeList.get(index-baseASTNodeNumber);
	}
	
	static{
		abstractASTNodeList.add(BodyDeclaration.class.getName());
		abstractASTNodeList.add(AbstractTypeDeclaration.class.getName());
		abstractASTNodeList.add(Comment.class.getName());
		abstractASTNodeList.add(Expression.class.getName());
		abstractASTNodeList.add(Annotation.class.getName());
		abstractASTNodeList.add(MethodReference.class.getName());
		abstractASTNodeList.add(Name.class.getName());
		abstractASTNodeList.add(Statement.class.getName());
		abstractASTNodeList.add(Type.class.getName());
		abstractASTNodeList.add(AnnotatableType.class.getName());
		abstractASTNodeList.add(VariableDeclaration.class.getName());
		
		for(int i=0; i<abstractASTNodeList.size(); i++){
			int number = baseASTNodeNumber + i;
			String className = abstractASTNodeList.get(i);
			abstractASTMap.put(className, number);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static String getASTNodeType(int[] code){
		for(int i=0; i<baseASTNodeNumber; i++){
			if(code[i]==1){
				int type = code[i] + 1;
				Class clazz = ASTNode.nodeClassForType(type);
				return clazz.getName();
			}
		}
		
		return "unknown";
	}
	
	@SuppressWarnings("rawtypes")
	public static int[] encode(ASTNode node){
		//there are 92 AST node type in all.
		int[] code = new int[baseASTNodeNumber+abstractASTNodeList.size()];
		for(int i=0; i<code.length; i++){
			code[i] = 0;
		}
		
		int nodeType = node.getNodeType();
		code[nodeType-1] = 1;
		
		//find shared AST parent based on JDT hierarchy
		Class clazz = ASTNode.nodeClassForType(nodeType);
		
		while(clazz!=null){
			clazz = clazz.getSuperclass();
			if(clazz.equals(ASTNode.class)){
				break;
			}
			int index = abstractASTMap.get(clazz.getName());
			code[index] = 1;
		}
		
		
		return code;
		
	}
}
