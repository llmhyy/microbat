/**
 * 
 */
package mutation.io;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.Node;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.AnnotationDeclaration;
import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.EmptyMemberDeclaration;
import japa.parser.ast.body.EmptyTypeDeclaration;
import japa.parser.ast.body.EnumConstantDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.InitializerDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.MultiTypeParameter;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.comments.BlockComment;
import japa.parser.ast.comments.JavadocComment;
import japa.parser.ast.comments.LineComment;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.InstanceOfExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralMinValueExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.LongLiteralMinValueExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.AssertStmt;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.LabeledStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.SynchronizedStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.stmt.TypeDeclarationStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.type.WildcardType;
import japa.parser.ast.visitor.ModifierVisitorAdapter;

/**
 * @author LLT
 *
 */
public class NodeReplacementVisitor extends ModifierVisitorAdapter<Object>{
	private Node orgNode;
	private Node newNode;
	
	public NodeReplacementVisitor(Node orgNode, Node newNode) {
		this.orgNode = orgNode;
		this.newNode = newNode;
	}
	
	public static void replace(BlockStmt parentNode, Node orgNode, Node newNode) {
		NodeReplacementVisitor visitor = new NodeReplacementVisitor(orgNode, newNode);
		visitor.visit(parentNode, null);
	}

	private boolean matchOrgNode(Node n) {
		return n.getBeginLine() == orgNode.getBeginLine() 
				&& n.getBeginColumn() == orgNode.getBeginColumn()
				&& n.getEndLine() == orgNode.getEndLine()
				&& n.getEndColumn() == orgNode.getEndColumn();
	}

	@Override
	public Node visit(AnnotationDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(AnnotationMemberDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ArrayAccessExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ArrayCreationExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ArrayInitializerExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(AssertStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(AssignExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(BinaryExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(BlockStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(BooleanLiteralExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(BreakStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(CastExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(CatchClause n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(CharLiteralExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ClassExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ClassOrInterfaceDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ClassOrInterfaceType n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(CompilationUnit n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ConditionalExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ConstructorDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ContinueStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(DoStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(DoubleLiteralExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(EmptyMemberDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(EmptyStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(EmptyTypeDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(EnclosedExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(EnumConstantDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(EnumDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ExplicitConstructorInvocationStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ExpressionStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(FieldAccessExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(FieldDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ForeachStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ForStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(IfStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ImportDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(InitializerDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(InstanceOfExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(IntegerLiteralExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(IntegerLiteralMinValueExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(JavadocComment n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(LabeledStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(LongLiteralExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(LongLiteralMinValueExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(MarkerAnnotationExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(MemberValuePair n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(MethodCallExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(MethodDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(NameExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(NormalAnnotationExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(NullLiteralExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ObjectCreationExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(PackageDeclaration n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(Parameter n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(MultiTypeParameter n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(PrimitiveType n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(QualifiedNameExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ReferenceType n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ReturnStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(SingleMemberAnnotationExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(StringLiteralExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(SuperExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(SwitchEntryStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(SwitchStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(SynchronizedStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ThisExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(ThrowStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(TryStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(TypeDeclarationStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(TypeParameter n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(UnaryExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(VariableDeclarationExpr n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(VariableDeclarator n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(VariableDeclaratorId n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(VoidType n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(WhileStmt n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(WildcardType n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(BlockComment n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}

	@Override
	public Node visit(LineComment n, Object arg) {
		if (matchOrgNode(n)) {
			return newNode;
		}
		return super.visit(n, arg);
	}
	
}
