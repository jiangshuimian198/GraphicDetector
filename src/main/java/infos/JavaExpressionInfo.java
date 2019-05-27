package main.java.infos;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public abstract class JavaExpressionInfo {
	
	public static long createJavaExpressionNode(BatchInserter inserter, Expression expression, String sourceContent, String methodName)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		long nodeId = 0;
		String expressionType;
		
		if(expression!=null)
		{	
			if(expression.getNodeType()==ASTNode.ARRAY_ACCESS)
			{
				expressionType = "ArrayAccess";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
				ArrayAccess arrayAccess = (ArrayAccess)expression;
				Expression array = arrayAccess.getArray();
				Expression indexExpression = arrayAccess.getIndex();
				long arrayId = JavaExpressionInfo.createJavaExpressionNode(inserter, array, sourceContent, methodName);
				if(arrayId!=-1)
					inserter.createRelationship(nodeId, arrayId, JavaExtractor.ARRAY_ACCESS, new HashMap<>());
				else;
				long indexId = JavaExpressionInfo.createJavaExpressionNode(inserter, indexExpression,sourceContent, methodName);
				if(indexId!=-1)
					inserter.createRelationship(nodeId, indexId, JavaExtractor.ARRAY_ACCESS_INDEX, new HashMap<>());
				else;
			}
			else if(expression.getNodeType()==ASTNode.ARRAY_CREATION)
			{
				expressionType = "ArrayCreation";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				ArrayCreation arrayCreation = (ArrayCreation)expression;
				Expression arrayInitializer = arrayCreation.getInitializer();
				@SuppressWarnings("unchecked")
				List<Expression> dimensions = arrayCreation.dimensions();
//				arrayCreation.resolveBoxing();
//				arrayCreation.resolveConstantExpressionValue();
//				arrayCreation.resolveTypeBinding();
//				arrayCreation.resolveUnboxing();
				ArrayType arrayType = arrayCreation.getType();
				Type elementType = arrayType.getElementType();
				int dimension = arrayType.getDimensions();
				String arrayTypeLiteral = arrayType.toString();
				String elementTypeLiteral = elementType.toString();
				
				map.put(JavaExtractor.DIMENSION, dimension);
				map.put(JavaExtractor.ARRAY_TYPE, arrayTypeLiteral);
				map.put(JavaExtractor.ELEMENT_TYPE, elementTypeLiteral);
				nodeId = createNode(inserter, map);
				
				long initializerId = JavaExpressionInfo.createJavaExpressionNode(inserter, arrayInitializer, sourceContent, methodName);
				if(initializerId!=-1)
					inserter.createRelationship(nodeId, initializerId, JavaExtractor.ARRAY_INITIALIZER, new HashMap<>());
				else;
				
				for(int i = 0; i< dimensions.size();i++)
				{
					long dimensionId = JavaExpressionInfo.createJavaExpressionNode(inserter, dimensions.get(i), sourceContent, methodName);
					if(dimensionId!=-1)
						inserter.createRelationship(nodeId, dimensionId, JavaExtractor.DIMENSIONS, new HashMap<>());
					else;
				}
			}
			else if(expression.getNodeType()==ASTNode.ARRAY_INITIALIZER)
			{
				expressionType = "ArrayInitializer";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
				ArrayInitializer arrayInitializer = (ArrayInitializer)expression;
				@SuppressWarnings("unchecked")
				List<Expression> initializerExpressions = arrayInitializer.expressions();
//				arrayInitializer.resolveBoxing();
//				arrayInitializer.resolveConstantExpressionValue();
//				arrayInitializer.resolveTypeBinding();
//				arrayInitializer.resolveUnboxing();
				for(int i = 0; i< initializerExpressions.size();i++)
				{
					long initializerExpressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, initializerExpressions.get(i), sourceContent, methodName);
					if(initializerExpressionId!=-1)
						inserter.createRelationship(nodeId, initializerExpressionId, JavaExtractor.SUB_ARRAY_INITIALIZER, new HashMap<>());
					else;
				}
			}
			else if(expression.getNodeType()==ASTNode.ASSIGNMENT)
			{
				expressionType = "Assignment";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
				Assignment assignment = (Assignment)expression;
				Expression leftHandSide = assignment.getLeftHandSide();
				Expression rightHandSide = assignment.getRightHandSide();
				Operator operator = assignment.getOperator();
				long oprtId = JavaExpressionInfo.createJavaAssignmentOperatorNode(inserter, operator);
				if(oprtId!=-1)
					inserter.createRelationship(nodeId, oprtId, JavaExtractor.ASSIGNMENT, new HashMap<>());
				else;
				long leftId = JavaExpressionInfo.createJavaExpressionNode(inserter, leftHandSide, sourceContent, methodName);
				long rightId = JavaExpressionInfo.createJavaExpressionNode(inserter, rightHandSide, sourceContent, methodName);
				if(leftId!=-1)
					inserter.createRelationship(oprtId, leftId, JavaExtractor.LEFT_OPERAND, new HashMap<>());
				else;
				if(rightId!=-1)
					inserter.createRelationship(oprtId, rightId, JavaExtractor.RIGHT_OPERAND, new HashMap<>());
				else;
//				assignment.resolveBoxing();
//				assignment.resolveConstantExpressionValue();
//				assignment.resolveTypeBinding();
//				assignment.resolveUnboxing();
			}
			else if(expression.getNodeType()==ASTNode.BOOLEAN_LITERAL)
			{
				expressionType = "BooleanLiteral";
				addProperties(map, expression, expressionType, methodName, sourceContent);
//				BooleanLiteral booleanLiteral = (BooleanLiteral)expression;
//				booleanLiteral.resolveBoxing();
//				booleanLiteral.resolveConstantExpressionValue();
//				booleanLiteral.resolveTypeBinding();
//				booleanLiteral.resolveUnboxing();
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.CAST_EXPRESSION)
			{
				expressionType = "CastExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.CHARACTER_LITERAL)
			{
				expressionType = "CharaterLiteral";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.CLASS_INSTANCE_CREATION)
			{
				expressionType = "ClassInstanceCreation";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.CONDITIONAL_EXPRESSION)
			{
				expressionType = "ConditionalExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.CREATION_REFERENCE)
			{
				expressionType = "CreationReference";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.EXPRESSION_METHOD_REFERENCE)
			{
				expressionType = "ExpressionMethodReference";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.FIELD_ACCESS)
			{
				expressionType = "FieldAccess";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.INFIX_EXPRESSION)
			{
				expressionType = "InfixExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.INSTANCEOF_EXPRESSION)
			{
				expressionType = "InstanceofExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.LAMBDA_EXPRESSION)
			{
				expressionType = "LambdaExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.MARKER_ANNOTATION)
			{
				expressionType = "MarkerAnnotation";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
				//ArrayAccess arrayAccess = (ArrayAccess)expression;
			}
			else if(expression.getNodeType()==ASTNode.METHOD_INVOCATION)
			{
				expressionType = "MethodInvocation";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.METHOD_REF)
			{
				expressionType = "MethodRef";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.NAME_QUALIFIED_TYPE)
			{
				expressionType = "NameQualifiedType";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.NORMAL_ANNOTATION)
			{
				expressionType = "NormalAnnotation";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.NULL_LITERAL)
			{
				expressionType = "NullLiteral";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.PARENTHESIZED_EXPRESSION)
			{
				expressionType = "ParenthesizedExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.POSTFIX_EXPRESSION)
			{
				expressionType = "PostfixExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.PREFIX_EXPRESSION)
			{
				expressionType = "PrefixExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.SINGLE_MEMBER_ANNOTATION)
			{
				expressionType = "SingleMemberAnnotation";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.STRING_LITERAL)
			{
				expressionType = "StringLiteral";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.SUPER_FIELD_ACCESS)
			{
				expressionType = "SuperFieldAccess";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.SUPER_METHOD_INVOCATION)
			{
				expressionType = "SuperMethodInvocation";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.THIS_EXPRESSION)
			{
				expressionType = "ThisExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				ThisExpression thisExpression = (ThisExpression)expression;
				Name name = thisExpression.getQualifier();
				if(name!=null)
					map.put(JavaExtractor.FULLNAME, name.toString());
				else
					map.put(JavaExtractor.FULLNAME, "null");
//				thisExpression.resolveBoxing();
//				thisExpression.resolveConstantExpressionValue();
//				thisExpression.resolveTypeBinding();
//				thisExpression.resolveUnboxing();
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.TYPE_LITERAL)
			{
				expressionType = "TypeLiteral";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.TYPE_METHOD_REFERENCE)
			{
				expressionType = "TypeMethodReference";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				TypeMethodReference typeMethodReference = (TypeMethodReference)expression;
				String name = typeMethodReference.getName().getIdentifier();
				map.put(JavaExtractor.NAME, name);
				Type type = typeMethodReference.getType();
				//ITypeBinding typeBinding = type.resolveBinding();
				map.put(JavaExtractor.METHOD_TYPE, type.toString());
//				typeMethodReference.resolveConstantExpressionValue();
//				typeMethodReference.resolveBoxing();
//				typeMethodReference.resolveMethodBinding();
//				typeMethodReference.resolveTypeBinding();
//				typeMethodReference.resolveUnboxing();
				@SuppressWarnings("unchecked")
				List<Type> typeArgsTypeList = typeMethodReference.typeArguments();
				String[] typeArgsDeclaredTypes = new String[typeArgsTypeList.size()];
				String[] typeArgsTypes = new String[typeArgsTypeList.size()];
				for(int i = 0; i<typeArgsTypeList.size(); i++)
				{
					Type element = typeArgsTypeList.get(i);
					typeArgsTypes[i]=element.toString();
					//type.resolveBinding();
					boolean isAnnotatable = element.isAnnotatable();
					boolean isArrayType = element.isArrayType();
					boolean isIntersectionType = element.isIntersectionType();
					boolean isNameQualifiedType = element.isNameQualifiedType();
					boolean isParameterizedType = element.isParameterizedType();
					boolean isPrimitiveType = element.isPrimitiveType();
					boolean isQualifiedType = element.isQualifiedType();
					boolean isSimpleType = element.isSimpleType();
					boolean isUnionType = element.isUnionType();
					boolean isVar = element.isVar();
					boolean isWildcardType = element.isWildcardType();
					if(isAnnotatable)
						typeArgsDeclaredTypes[i]="Annotatable";
					else if(isArrayType)
						typeArgsDeclaredTypes[i]="ArrayType";
					else if(isIntersectionType)
						typeArgsDeclaredTypes[i]="IntersectionType";
					else if(isNameQualifiedType)
						typeArgsDeclaredTypes[i]="NameQualifiedType";
					else if(isParameterizedType)
						typeArgsDeclaredTypes[i]="ParameterizedType";
					else if(isPrimitiveType)
						typeArgsDeclaredTypes[i]="PrimitiveType";
					else if(isQualifiedType)
						typeArgsDeclaredTypes[i]="QualifiedType";
					else if(isSimpleType)
						typeArgsDeclaredTypes[i]="SimpleType";
					else if(isUnionType)
						typeArgsDeclaredTypes[i]="UnionType";
					else if(isVar)
						typeArgsDeclaredTypes[i]="Varialbe";
					else if(isWildcardType)
						typeArgsDeclaredTypes[i]="WildcardType";
				}
				map.put(JavaExtractor.DECLARED_TYPE, typeArgsDeclaredTypes);
				map.put(JavaExtractor.TYPE_ARG_TYPE_STR, typeArgsDeclaredTypes);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.VARIABLE_DECLARATION_EXPRESSION)
			{
				expressionType = "VariableDeclarationExpression";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)expression;
				int modifier = variableDeclarationExpression.getModifiers();
				if(Modifier.isFinal(modifier))
					map.put(JavaExtractor.IS_FINAL, true);
				else
					map.put(JavaExtractor.IS_FINAL, false);
				if(Modifier.isStatic(modifier))
					map.put(JavaExtractor.IS_STATIC, true);
				else
					map.put(JavaExtractor.IS_STATIC, false);
				if(Modifier.isVolatile(modifier))
					map.put(JavaExtractor.IS_VOLATILE, true);
				else
					map.put(JavaExtractor.IS_VOLATILE, false);
				if(Modifier.isTransient(modifier))
					map.put(JavaExtractor.IS_TRANSIENT, true);
				else
					map.put(JavaExtractor.IS_TRANSIENT, false);
				variableDeclarationExpression.fragments();
				Type type = variableDeclarationExpression.getType();
				String typeLiteral = type.toString();
				map.put(JavaExtractor.VAR_TYPE_STR, typeLiteral);
				boolean isAnnotatable = type.isAnnotatable();
				boolean isArrayType = type.isArrayType();
				boolean isIntersectionType = type.isIntersectionType();
				boolean isNameQualifiedType = type.isNameQualifiedType();
				boolean isParameterizedType = type.isParameterizedType();
				boolean isPrimitiveType = type.isPrimitiveType();
				boolean isQualifiedType = type.isQualifiedType();
				boolean isSimpleType = type.isSimpleType();
				boolean isUnionType = type.isUnionType();
				boolean isVar = type.isVar();
				boolean isWildcardType = type.isWildcardType();
				if(isAnnotatable)
				map.put(JavaExtractor.DECLARED_TYPE, "Annotatable");
				else if(isArrayType)
				map.put(JavaExtractor.DECLARED_TYPE, "ArrayType");
				else if(isIntersectionType)
				map.put(JavaExtractor.DECLARED_TYPE, "IntersectionType");
				else if(isNameQualifiedType)
				map.put(JavaExtractor.DECLARED_TYPE, "NameQualifiedType");
				else if(isParameterizedType)
				map.put(JavaExtractor.DECLARED_TYPE, "ParameterizedType");
				else if(isPrimitiveType)
				map.put(JavaExtractor.DECLARED_TYPE, "PrimitiveType");
				else if(isQualifiedType)
				map.put(JavaExtractor.DECLARED_TYPE, "QualifiedType");
				else if(isSimpleType)
				map.put(JavaExtractor.DECLARED_TYPE, "SimpleType");
				else if(isUnionType)
				map.put(JavaExtractor.DECLARED_TYPE, "UnionType");
				else if(isVar)
				map.put(JavaExtractor.DECLARED_TYPE, "Varialbe");
				else if(isWildcardType)
				map.put(JavaExtractor.DECLARED_TYPE, "WildcardType");
				nodeId = createNode(inserter, map);
				
				@SuppressWarnings("unchecked")
				List<VariableDeclarationFragment> fragments = variableDeclarationExpression.fragments();
				for(int i = 0; i<fragments.size(); i++)
				{
					long id = JavaStatementInfo.createVariableDeclarationFragmentNode(inserter, map, methodName, i, fragments.get(i), sourceContent);
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.VAR_DECLARATION_FRAG, new HashMap<>());
					else;
				}
			}
			else if(expression.getNodeType()==ASTNode.QUALIFIED_NAME)
			{
				expressionType = "QualifiedName";
				QualifiedName qualifiedName = (QualifiedName)expression;
				String name = qualifiedName.getName().getIdentifier();
				String qualifier = qualifiedName.getQualifier().toString();
				map.put(JavaExtractor.NAME, name);
				map.put(JavaExtractor.QUALIFIER, qualifier);
				addProperties(map, expression, expressionType, methodName, sourceContent);
//				qualifiedName.resolveBinding();
//				qualifiedName.resolveBoxing();
//				qualifiedName.resolveConstantExpressionValue();
//				qualifiedName.resolveTypeBinding();
//				qualifiedName.resolveUnboxing();
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.SIMPLE_NAME)
			{
				expressionType = "SimpleName";
				SimpleName simpleName = (SimpleName)expression;
				String identifier = simpleName.getIdentifier();
				map.put(JavaExtractor.NAME, identifier);
				boolean isDeclaration = simpleName.isDeclaration();
				boolean isVar = simpleName.isVar();
//				simpleName.resolveBinding();
//				simpleName.resolveBoxing();
//				simpleName.resolveConstantExpressionValue();
//				simpleName.resolveTypeBinding();
//				simpleName.resolveUnboxing();
				if(isDeclaration)
					map.put(JavaExtractor.SIMPLENAME_TYPE, "Declaration");
				else if(isVar)
					map.put(JavaExtractor.SIMPLENAME_TYPE, "Var");
				else
					map.put(JavaExtractor.SIMPLENAME_TYPE, "Else");
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.NUMBER_LITERAL)
			{
				expressionType = "NumberLiteral";
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
			}
			else
			{
				expressionType = ""+expression.getNodeType();
				addProperties(map, expression, expressionType, methodName, sourceContent);
				nodeId = createNode(inserter, map);
				System.out.println(expressionType);
			}
			return nodeId;
		}
		else
			return -1;
	}

	private static long createJavaAssignmentOperatorNode(BatchInserter inserter, Operator operator) {
		if(operator!=null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put(JavaExtractor.OPERATOR_TYPE, "AssignOperator");
			map.put(JavaExtractor.OPERATOR_LITERAL,operator.toString());
			return inserter.createNode(map, JavaExtractor.OPERATOR);
		}
		else
			return -1;
	}

	private static void addProperties(HashMap<String, Object> map, Expression expression, String expressionType, String methodName, String sourceContent) {
		// TODO Auto-generated method stub
		map.put(JavaExtractor.EXPRESSION_TYPE, expressionType);
		map.put(JavaExtractor.METHOD_NAME,methodName);
		String content = sourceContent.substring(expression.getStartPosition(), expression.getStartPosition()+expression.getLength());
		map.put(JavaExtractor.CONTENT, content);
		int rowNo = sourceContent.substring(0, expression.getStartPosition()).split("\n").length;
		map.put(JavaExtractor.ROW_NO, rowNo);
	}
	
	private static long createNode(BatchInserter inserter, HashMap<String, Object> map)
	{
		return inserter.createNode(map, JavaExtractor.EXPREESION);
	}

}
