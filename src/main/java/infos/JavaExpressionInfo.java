package main.java.infos;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public abstract class JavaExpressionInfo {
	
	private static String createJavaExpressionNode(Expression expression)
	{
		String expressionType;
		if(expression.getNodeType()==ASTNode.ANNOTATION_TYPE_DECLARATION)
			expressionType = "AnnotationTypeDeclaration";
		else if(expression.getNodeType()==ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION)
			expressionType = "AnnotationTypeMemberDeclaration";
		else if(expression.getNodeType()==ASTNode.ANONYMOUS_CLASS_DECLARATION)
			expressionType = "AnnotationClassDeclaration";
		else if(expression.getNodeType()==ASTNode.ARRAY_ACCESS)
			expressionType = "ArrayAccess";
		else if(expression.getNodeType()==ASTNode.ARRAY_CREATION)
			expressionType = "ArrayCreation";
		else if(expression.getNodeType()==ASTNode.ARRAY_INITIALIZER)
			expressionType = "ArrayInitializer";
		else if(expression.getNodeType()==ASTNode.ASSIGNMENT)
			expressionType = "Assignment";
		else if(expression.getNodeType()==ASTNode.BOOLEAN_LITERAL)
			expressionType = "BooleanLiteral";
		else if(expression.getNodeType()==ASTNode.CAST_EXPRESSION)
			expressionType = "CastExpression";
		else if(expression.getNodeType()==ASTNode.CHARACTER_LITERAL)
			expressionType = "CharaterLiteral";
		else if(expression.getNodeType()==ASTNode.CLASS_INSTANCE_CREATION)
			expressionType = "ClassInstanceCreation";
		else if(expression.getNodeType()==ASTNode.CONDITIONAL_EXPRESSION)
			expressionType = "ConditionalExpression";
		else if(expression.getNodeType()==ASTNode.CREATION_REFERENCE)
			expressionType = "CreationReference";
		else if(expression.getNodeType()==ASTNode.EXPRESSION_METHOD_REFERENCE)
			expressionType = "ExpressionMethodReference";
		else if(expression.getNodeType()==ASTNode.FIELD_ACCESS)
			expressionType = "FieldAccess";
		else if(expression.getNodeType()==ASTNode.INFIX_EXPRESSION)
			expressionType = "InfixExpression";
		else if(expression.getNodeType()==ASTNode.INSTANCEOF_EXPRESSION)
			expressionType = "InstanceofExpression";
		else if(expression.getNodeType()==ASTNode.LAMBDA_EXPRESSION)
			expressionType = "LambdaExpression";
		else if(expression.getNodeType()==ASTNode.METHOD_INVOCATION)
			expressionType = "MethodInvocation";
		else if(expression.getNodeType()==ASTNode.METHOD_REF)
			expressionType = "MethodRef";
		else if(expression.getNodeType()==ASTNode.NAME_QUALIFIED_TYPE)
			expressionType = "NameQualifiedType";
		else if(expression.getNodeType()==ASTNode.NULL_LITERAL)
			expressionType = "NullLiteral";
		else if(expression.getNodeType()==ASTNode.PARENTHESIZED_EXPRESSION)
			expressionType = "ParenthesizedExpression";
		else if(expression.getNodeType()==ASTNode.POSTFIX_EXPRESSION)
			expressionType = "PostfixExpression";
		else if(expression.getNodeType()==ASTNode.PREFIX_EXPRESSION)
			expressionType = "PrefixExpression";
		else if(expression.getNodeType()==ASTNode.STRING_LITERAL)
			expressionType = "StringLiteral";
		else if(expression.getNodeType()==ASTNode.SUPER_FIELD_ACCESS)
			expressionType = "SuperFieldAccess";
		else if(expression.getNodeType()==ASTNode.SUPER_METHOD_INVOCATION)
			expressionType = "SuperMethodInvocation";
		else if(expression.getNodeType()==ASTNode.THIS_EXPRESSION)
			expressionType = "ThisExpression";
		else if(expression.getNodeType()==ASTNode.TYPE_LITERAL)
			expressionType = "TypeLiteral";
		else if(expression.getNodeType()==ASTNode.TYPE_METHOD_REFERENCE)
			expressionType = "TypeMethodReference";
		else if(expression.getNodeType()==ASTNode.VARIABLE_DECLARATION_EXPRESSION)
			expressionType = "VariableDeclarationExpression";
		else if(expression.getNodeType()==ASTNode.QUALIFIED_NAME)
			expressionType = "QualifiedName";
		else if(expression.getNodeType()==ASTNode.SIMPLE_NAME)
			expressionType = "SimpleName";
		else if(expression.getNodeType()==ASTNode.NUMBER_LITERAL)
			expressionType = "NumberLiteral";
		else
		{
			expressionType = ""+expression.getNodeType();
			System.out.println(expressionType);
		}
		return expressionType;
	}
	
	public static long createJavaExpressionNode(BatchInserter inserter, Expression expression, String sourceContent, String methodName)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		String expressionType = null;
		if(expression!=null) {
			expressionType = createJavaExpressionNode(expression);
			long nodeId = createNode(inserter, map, expression, expressionType, methodName, sourceContent);
			return nodeId;
		}
		else
			return -1;
	}

	private static long createNode(BatchInserter inserter, HashMap<String, Object> map, Expression expression, String expressionType, String methodName, String sourceContent) {
		// TODO Auto-generated method stub
		map.put(JavaExtractor.EXPRESSION_TYPE, expressionType);
		map.put(JavaExtractor.METHOD_NAME,methodName);
		String content = sourceContent.substring(expression.getStartPosition(), expression.getStartPosition()+expression.getLength());
		map.put(JavaExtractor.CONTENT, content);
		return inserter.createNode(map, JavaExtractor.EXPREESION);
	}

}
