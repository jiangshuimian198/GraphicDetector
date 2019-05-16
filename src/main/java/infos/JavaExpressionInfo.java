package main.java.infos;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;

public class JavaExpressionInfo {
	@Getter
	private long nodeId;
	@Getter
	protected String expressionType;
	@Getter
	protected HashMap<String, Object> map;
	
	public JavaExpressionInfo(BatchInserter inserter, Expression expression)
	{
		map = new HashMap<String, Object>();
		createJavaExpressionNode(expression);
		nodeId = createNode(inserter);
	}
	
	private void createJavaExpressionNode(Expression expression)
	{
		if(expression.getNodeType()==ASTNode.ANNOTATION_TYPE_DECLARATION)
			expressionType = "ANNOTATION_TYPE_DECLARATION";
		else if(expression.getNodeType()==ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION)
			expressionType = "ANNOTATION_TYPE_MEMBER_DECLARATION";
		else if(expression.getNodeType()==ASTNode.ANONYMOUS_CLASS_DECLARATION)
			expressionType = "ANONYMOUS_CLASS_DECLARATION";
		else if(expression.getNodeType()==ASTNode.ARRAY_ACCESS)
			expressionType = "ARRAY_ACCESS";
		else if(expression.getNodeType()==ASTNode.ARRAY_CREATION)
			expressionType = "ARRAY_CREATION";
		else if(expression.getNodeType()==ASTNode.ARRAY_INITIALIZER)
			expressionType = "ARRAY_INITIALIZER";
		else if(expression.getNodeType()==ASTNode.ASSIGNMENT)
			expressionType = "ASSIGNMENT";
		else if(expression.getNodeType()==ASTNode.BOOLEAN_LITERAL)
			expressionType = "BOOLEAN_LITERAL";
		else if(expression.getNodeType()==ASTNode.CAST_EXPRESSION)
			expressionType = "CAST_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.CHARACTER_LITERAL)
			expressionType = "CHARACTER_LITERAL";
		else if(expression.getNodeType()==ASTNode.CLASS_INSTANCE_CREATION)
			expressionType = "CLASS_INSTANCE_CREATION";
		else if(expression.getNodeType()==ASTNode.CONDITIONAL_EXPRESSION)
			expressionType = "CONDITIONAL_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.CREATION_REFERENCE)
			expressionType = "CREATION_REFERENCE";
		else if(expression.getNodeType()==ASTNode.EXPRESSION_METHOD_REFERENCE)
			expressionType = "EXPRESSION_METHOD_REFERENCE";
		else if(expression.getNodeType()==ASTNode.FIELD_ACCESS)
			expressionType = "FIELD_ACCESS";
		else if(expression.getNodeType()==ASTNode.INFIX_EXPRESSION)
			expressionType = "INFIX_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.INSTANCEOF_EXPRESSION)
			expressionType = "INSTANCEOF_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.LAMBDA_EXPRESSION)
			expressionType = "LAMBDA_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.METHOD_INVOCATION)
			expressionType = "METHOD_INVOCATION";
		else if(expression.getNodeType()==ASTNode.METHOD_REF)
			expressionType = "METHOD_REF";
		else if(expression.getNodeType()==ASTNode.NAME_QUALIFIED_TYPE)
			expressionType = "NAME_QUALIFIED_TYPE";
		else if(expression.getNodeType()==ASTNode.NULL_LITERAL)
			expressionType = "NULL_LITERAL";
		else if(expression.getNodeType()==ASTNode.PARENTHESIZED_EXPRESSION)
			expressionType = "PARENTHESIZED_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.POSTFIX_EXPRESSION)
			expressionType = "POSTFIX_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.PREFIX_EXPRESSION)
			expressionType = "PREFIX_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.STRING_LITERAL)
			expressionType = "STRING_LITERAL";
		else if(expression.getNodeType()==ASTNode.SUPER_FIELD_ACCESS)
			expressionType = "SUPER_FIELD_ACCESS";
		else if(expression.getNodeType()==ASTNode.SUPER_METHOD_INVOCATION)
			expressionType = "SUPER_METHOD_INVOCATION";
		else if(expression.getNodeType()==ASTNode.THIS_EXPRESSION)
			expressionType = "THIS_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.TYPE_LITERAL)
			expressionType = "TYPE_LITERAL";
		else if(expression.getNodeType()==ASTNode.TYPE_METHOD_REFERENCE)
			expressionType = "TYPE_METHOD_REFERENCE";
		else if(expression.getNodeType()==ASTNode.VARIABLE_DECLARATION_EXPRESSION)
			expressionType = "VARIABLE_DECLARATION_EXPRESSION";
		else if(expression.getNodeType()==ASTNode.QUALIFIED_NAME)
			expressionType = "QUALIFIED_NAME";
		else if(expression.getNodeType()==ASTNode.SIMPLE_NAME)
			expressionType = "SIMPLE_NAME";
		else
		{
			expressionType = ""+expression.getNodeType();
			System.out.println(expressionType);
		}
	}
	
	public static long createJavaExpressionInfo(BatchInserter inserter, Expression expression)
	{
		if(expression!=null) {
			JavaExpressionInfo info = new JavaExpressionInfo(inserter, expression);
			return info.getNodeId();
		}
		else
			return -1;
	}

	private long createNode(BatchInserter inserter) {
		// TODO Auto-generated method stub
		map.put(JavaExtractor.EXPRESSION_TYPE, expressionType);
		return inserter.createNode(map, JavaExtractor.EXPREESION);
	}

}
