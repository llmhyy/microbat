package mutation.mutator;

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
import japa.parser.ast.visitor.VoidVisitorAdapter;

/**
 * Created by hoangtung on 4/3/15.
 */
public class AbstractMutationVisitor extends VoidVisitorAdapter<Boolean> {

	protected boolean beforeVisit(Node node) {
		return true;
	}

	protected boolean beforeMutate(Node n) {
		return true;
	}

	public boolean mutate(AnnotationDeclaration n) {
		return true;
	}

	public boolean mutate(AnnotationMemberDeclaration n) {
		return true;
	}

	public boolean mutate(ArrayAccessExpr n) {
		return true;
	}

	public boolean mutate(ArrayCreationExpr n) {
		return true;
	}

	public boolean mutate(ArrayInitializerExpr n) {
		return true;
	}

	public boolean mutate(AssertStmt n) {
		return true;
	}

	public boolean mutate(AssignExpr n) {
		return true;
	}

	public boolean mutate(BinaryExpr n) {
		return true;
	}

	public boolean mutate(BlockComment n) {
		return true;
	}

	public boolean mutate(BlockStmt n) {
		return true;
	}

	public boolean mutate(BooleanLiteralExpr n) {
		return true;
	}

	public boolean mutate(BreakStmt n) {
		return true;
	}

	public boolean mutate(CastExpr n) {
		return true;
	}

	public boolean mutate(CatchClause n) {
		return true;
	}

	public boolean mutate(CharLiteralExpr n) {
		return true;
	}

	public boolean mutate(ClassExpr n) {
		return true;
	}

	public boolean mutate(ClassOrInterfaceDeclaration n) {
		return true;
	}

	public boolean mutate(ClassOrInterfaceType n) {
		return true;
	}

	public boolean mutate(CompilationUnit n) {
		return true;
	}

	public boolean mutate(ConditionalExpr n) {
		return true;
	}

	public boolean mutate(ConstructorDeclaration n) {
		return true;
	}

	public boolean mutate(ContinueStmt n) {
		return true;
	}

	public boolean mutate(DoStmt n) {
		return true;
	}

	public boolean mutate(DoubleLiteralExpr n) {
		return true;
	}

	public boolean mutate(EmptyMemberDeclaration n) {
		return true;
	}

	public boolean mutate(EmptyStmt n) {
		return true;
	}

	public boolean mutate(EmptyTypeDeclaration n) {
		return true;
	}

	public boolean mutate(EnclosedExpr n) {
		return true;
	}

	public boolean mutate(EnumConstantDeclaration n) {
		return true;
	}

	public boolean mutate(EnumDeclaration n) {
		return true;
	}

	public boolean mutate(ExplicitConstructorInvocationStmt n) {
		return true;
	}

	public boolean mutate(ExpressionStmt n) {
		return true;
	}

	public boolean mutate(FieldAccessExpr n) {
		return true;
	}

	public boolean mutate(FieldDeclaration n) {
		return true;
	}

	public boolean mutate(ForeachStmt n) {
		return true;
	}

	public boolean mutate(ForStmt n) {
		return true;
	}

	public boolean mutate(IfStmt n) {
		return true;
	}

	public boolean mutate(ImportDeclaration n) {
		return true;
	}

	public boolean mutate(InitializerDeclaration n) {
		return true;
	}

	public boolean mutate(InstanceOfExpr n) {
		return true;
	}

	public boolean mutate(IntegerLiteralExpr n) {
		return true;
	}

	public boolean mutate(IntegerLiteralMinValueExpr n) {
		return true;
	}

	public boolean mutate(JavadocComment n) {
		return true;
	}

	public boolean mutate(LabeledStmt n) {
		return true;
	}

	public boolean mutate(LineComment n) {
		return true;
	}

	public boolean mutate(LongLiteralExpr n) {
		return true;
	}

	public boolean mutate(LongLiteralMinValueExpr n) {
		return true;
	}

	public boolean mutate(MarkerAnnotationExpr n) {
		return true;
	}

	public boolean mutate(MemberValuePair n) {
		return true;
	}

	public boolean mutate(MethodCallExpr n) {
		return true;
	}

	public boolean mutate(MethodDeclaration n) {
		return true;
	}

	public boolean mutate(NameExpr n) {
		return true;
	}

	public boolean mutate(NormalAnnotationExpr n) {
		return true;
	}

	public boolean mutate(NullLiteralExpr n) {
		return true;
	}

	public boolean mutate(ObjectCreationExpr n) {
		return true;
	}

	public boolean mutate(PackageDeclaration n) {
		return true;
	}

	public boolean mutate(Parameter n) {
		return true;
	}

	public boolean mutate(MultiTypeParameter n) {
		return true;
	}

	public boolean mutate(PrimitiveType n) {
		return true;
	}

	public boolean mutate(QualifiedNameExpr n) {
		return true;
	}

	public boolean mutate(ReferenceType n) {
		return true;
	}

	public boolean mutate(ReturnStmt n) {
		return true;
	}

	public boolean mutate(SingleMemberAnnotationExpr n) {
		return true;
	}

	public boolean mutate(StringLiteralExpr n) {
		return true;
	}

	public boolean mutate(SuperExpr n) {
		return true;
	}

	public boolean mutate(SwitchEntryStmt n) {
		return true;
	}

	public boolean mutate(SwitchStmt n) {
		return true;
	}

	public boolean mutate(SynchronizedStmt n) {
		return true;
	}

	public boolean mutate(ThisExpr n) {
		return true;
	}

	public boolean mutate(ThrowStmt n) {
		return true;
	}

	public boolean mutate(TryStmt n) {
		return true;
	}

	public boolean mutate(TypeDeclarationStmt n) {
		return true;
	}

	public boolean mutate(TypeParameter n) {
		return true;
	}

	public boolean mutate(UnaryExpr n) {
		return true;
	}

	public boolean mutate(VariableDeclarationExpr n) {
		return true;
	}

	public boolean mutate(VariableDeclarator n) {
		return true;
	}

	public boolean mutate(VariableDeclaratorId n) {
		return true;
	}

	public boolean mutate(VoidType n) {
		return true;
	}

	public boolean mutate(WhileStmt n) {
		return true;
	}

	public boolean mutate(WildcardType n) {
		return true;
	}

	@Override
	public void visit(AnnotationDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(AnnotationMemberDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ArrayAccessExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ArrayCreationExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ArrayInitializerExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(AssertStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(AssignExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(BinaryExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(BlockComment n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(BlockStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(BooleanLiteralExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(BreakStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(CastExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(CatchClause n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(CharLiteralExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ClassExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ClassOrInterfaceType n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(CompilationUnit n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ConditionalExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ConstructorDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ContinueStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(DoStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(DoubleLiteralExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(EmptyMemberDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(EmptyStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(EmptyTypeDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(EnclosedExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(EnumConstantDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(EnumDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ExpressionStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(FieldAccessExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(FieldDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ForeachStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ForStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(IfStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ImportDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(InitializerDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(InstanceOfExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(IntegerLiteralExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(IntegerLiteralMinValueExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(JavadocComment n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(LabeledStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(LineComment n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(LongLiteralExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(LongLiteralMinValueExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(MarkerAnnotationExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(MemberValuePair n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(MethodCallExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(MethodDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(NameExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(NormalAnnotationExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(NullLiteralExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ObjectCreationExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(PackageDeclaration n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(Parameter n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(MultiTypeParameter n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(PrimitiveType n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(QualifiedNameExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ReferenceType n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ReturnStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(SingleMemberAnnotationExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(StringLiteralExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(SuperExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(SwitchEntryStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(SwitchStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(SynchronizedStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ThisExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(ThrowStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(TryStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(TypeDeclarationStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(TypeParameter n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(UnaryExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(VariableDeclarationExpr n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(VariableDeclarator n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(VariableDeclaratorId n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(VoidType n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(WhileStmt n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

	@Override
	public void visit(WildcardType n, Boolean arg) {
		if (beforeMutate(n)) {
			if (!mutate(n)) {
				return;
			}
		}
		if (beforeVisit(n)) {
			super.visit(n, arg);
		}
	}

}
