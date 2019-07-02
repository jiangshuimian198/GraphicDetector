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
import org.eclipse.jdt.core.dom.Assignment.*;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public abstract class JavaExpressionInfo {
	
	public static long createJavaExpressionNode(BatchInserter inserter, Expression expression, String codeContent, String methodName)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		long nodeId = 0;
		String expressionType;
		
		if(expression!=null)
		{	
			if(expression.getNodeType()==ASTNode.ARRAY_ACCESS)
			{
				expressionType = "ArrayAccess";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
				ArrayAccess arrayAccess = (ArrayAccess)expression;
				Expression array = arrayAccess.getArray();
				Expression indexExpression = arrayAccess.getIndex();
				long arrayId = JavaExpressionInfo.createJavaExpressionNode(inserter, array, codeContent, methodName);
				if(arrayId!=-1)
					inserter.createRelationship(nodeId, arrayId, JavaExtractor.ARRAY_ACCESS, new HashMap<>());
				else;
				long indexId = JavaExpressionInfo.createJavaExpressionNode(inserter, indexExpression,codeContent, methodName);
				if(indexId!=-1)
					inserter.createRelationship(nodeId, indexId, JavaExtractor.ARRAY_ACCESS_INDEX, new HashMap<>());
				else;
			}
			else if(expression.getNodeType()==ASTNode.ARRAY_CREATION)
			{
				expressionType = "ArrayCreation";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
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
				
				map.put(JavaExtractor.DIMENSION_NUM, dimension);
				map.put(JavaExtractor.ARRAY_TYPE, arrayTypeLiteral);
				map.put(JavaExtractor.ELEMENT_TYPE, elementTypeLiteral);
				nodeId = createNode(inserter, map);
				
				long initializerId = JavaExpressionInfo.createJavaExpressionNode(inserter, arrayInitializer, codeContent, methodName);
				if(initializerId!=-1)
					inserter.createRelationship(nodeId, initializerId, JavaExtractor.ARRAY_INITIALIZER, new HashMap<>());
				else;
				
				for(int i = 0; i< dimensions.size();i++)
				{
					long dimensionId = JavaExpressionInfo.createJavaExpressionNode(inserter, dimensions.get(i), codeContent, methodName);
					if(dimensionId!=-1)
						inserter.createRelationship(nodeId, dimensionId, JavaExtractor.DIMENSIONS, new HashMap<>());
					else;
				}
			}
			else if(expression.getNodeType()==ASTNode.ARRAY_INITIALIZER)
			{
				expressionType = "ArrayInitializer";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
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
					long initializerExpressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, initializerExpressions.get(i), codeContent, methodName);
					if(initializerExpressionId!=-1)
						inserter.createRelationship(nodeId, initializerExpressionId, JavaExtractor.SUB_ARRAY_INITIALIZER, new HashMap<>());
					else;
				}
			}
			else if(expression.getNodeType()==ASTNode.ASSIGNMENT)
			{
				expressionType = "Assignment";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
				Assignment assignment = (Assignment)expression;
				Expression leftHandSide = assignment.getLeftHandSide();
				Expression rightHandSide = assignment.getRightHandSide();
				Operator operator = assignment.getOperator();
				long oprtId = JavaExpressionInfo.createJavaAssignmentOperatorNode(inserter, operator);
				if(oprtId!=-1)
					inserter.createRelationship(nodeId, oprtId, JavaExtractor.ASSIGNMENT, new HashMap<>());
				else;
				long leftId = JavaExpressionInfo.createJavaExpressionNode(inserter, leftHandSide, codeContent, methodName);
				long rightId = JavaExpressionInfo.createJavaExpressionNode(inserter, rightHandSide, codeContent, methodName);
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
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
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
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				CastExpression castExpression = (CastExpression)expression;
				JavaExpressionInfo.addDeclaredTypeProperty(map, castExpression.getType());
				map.put(JavaExtractor.TYPE_CASTED_TO, castExpression.getType().toString());
				nodeId = createNode(inserter, map);
				Expression castedExpression = castExpression.getExpression();
				long castedExpressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, castedExpression, codeContent, methodName);
				if(castedExpressionId!=-1)
					inserter.createRelationship(nodeId, castedExpressionId, JavaExtractor.CAST, new HashMap<>());
				else;
//				castExpression.resolveBoxing();
//				castExpression.resolveConstantExpressionValue();
//				castExpression.resolveTypeBinding();
//				castExpression.resolveUnboxing();
			}
			else if(expression.getNodeType()==ASTNode.CHARACTER_LITERAL)
			{
				expressionType = "CharaterLiteral";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.CLASS_INSTANCE_CREATION)
			{
				expressionType = "ClassInstanceCreation";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation)expression;
				classInstanceCreation.arguments();
				classInstanceCreation.getAnonymousClassDeclaration();
				classInstanceCreation.getExpression();
				classInstanceCreation.getType();
				classInstanceCreation.typeArguments();
//				classInstanceCreation.resolveBoxing();
//				classInstanceCreation.resolveConstantExpressionValue();
//				classInstanceCreation.resolveConstructorBinding();
//				classInstanceCreation.resolveTypeBinding();
//				classInstanceCreation.resolveUnboxing();
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.CONDITIONAL_EXPRESSION)
			{
				expressionType = "ConditionalExpression";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				ConditionalExpression conditionalExpression = (ConditionalExpression)expression;
				conditionalExpression.getElseExpression();
				conditionalExpression.getExpression();
				conditionalExpression.getThenExpression();
				conditionalExpression.resolveBoxing();
				conditionalExpression.resolveConstantExpressionValue();
				conditionalExpression.resolveTypeBinding();
				conditionalExpression.resolveUnboxing();
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.CREATION_REFERENCE)
			{
				expressionType = "CreationReference";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				CreationReference creationReference = (CreationReference)expression;
				creationReference.getType();
				creationReference.getTypeArgumentsProperty();
				creationReference.typeArguments();
//				creationReference.resolveBoxing();
//				creationReference.resolveConstantExpressionValue();
//				creationReference.resolveMethodBinding();
//				creationReference.resolveTypeBinding();
//				creationReference.resolveUnboxing();
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.EXPRESSION_METHOD_REFERENCE)
			{
				expressionType = "ExpressionMethodReference";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				ExpressionMethodReference expressionMethodReference = (ExpressionMethodReference)expression;
				expressionMethodReference.getExpression();
				expressionMethodReference.getName();
				expressionMethodReference.typeArguments();
//				expressionMethodReference.resolveBoxing();
//				expressionMethodReference.resolveConstantExpressionValue();
//				expressionMethodReference.resolveMethodBinding();
//				expressionMethodReference.resolveTypeBinding();
//				expressionMethodReference.resolveUnboxing();
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.FIELD_ACCESS)
			{
				expressionType = "FieldAccess";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				FieldAccess fieldAccess = (FieldAccess)expression;
				fieldAccess.getExpression();
				fieldAccess.getName();
//				fieldAccess.resolveBoxing();
//				fieldAccess.resolveConstantExpressionValue();
//				fieldAccess.resolveFieldBinding();
//				fieldAccess.resolveTypeBinding();
//				fieldAccess.resolveUnboxing();
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.INFIX_EXPRESSION)
			{
				expressionType = "InfixExpression";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
				InfixExpression prefixExpression = (InfixExpression)expression;
				Expression leftOperand = prefixExpression.getLeftOperand();
				Expression rightOperand = prefixExpression.getRightOperand();
				InfixExpression.Operator operator = prefixExpression.getOperator();
				long oprtId = JavaExpressionInfo.createJavaInfixOperatorNode(inserter, operator);
				inserter.createRelationship(nodeId, oprtId, JavaExtractor.INFIX, new HashMap<>());
				long leftOprdId = JavaExpressionInfo.createJavaExpressionNode(inserter, leftOperand, codeContent, methodName);
				long rightOprdId = JavaExpressionInfo.createJavaExpressionNode(inserter, rightOperand, codeContent, methodName);
				inserter.createRelationship(oprtId, leftOprdId, JavaExtractor.LEFT_OPERAND, new HashMap<>());
				inserter.createRelationship(oprtId, rightOprdId, JavaExtractor.RIGHT_OPERAND, new HashMap<>());
//				prefixExpression.resolveBoxing();
//				prefixExpression.resolveConstantExpressionValue();
//				prefixExpression.resolveTypeBinding();
//				prefixExpression.resolveUnboxing();
			}
			else if(expression.getNodeType()==ASTNode.INSTANCEOF_EXPRESSION)
			{
				expressionType = "InstanceofExpression";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				InstanceofExpression instanceofExpression = (InstanceofExpression)expression;
				Type type = instanceofExpression.getRightOperand();
				JavaExpressionInfo.addDeclaredTypeProperty(map,type);
				map.put(JavaExtractor.INSTANCE_TYPE, type.toString());
				nodeId = createNode(inserter, map);
				Expression left = instanceofExpression.getLeftOperand();
				long leftId = JavaExpressionInfo.createJavaExpressionNode(inserter, left, codeContent, methodName);
				if(leftId!=-1)
					inserter.createRelationship(nodeId, leftId, JavaExtractor.LEFT_OPERAND, new HashMap<>());
				else;
			}
			else if(expression.getNodeType()==ASTNode.LAMBDA_EXPRESSION)
			{
				expressionType = "LambdaExpression";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
				
				LambdaExpression lambdaExpression = (LambdaExpression)expression;
				@SuppressWarnings("unchecked")
				List<VariableDeclaration> paramList = lambdaExpression.parameters();
				for(VariableDeclaration element : paramList)
				{
					if(element.getNodeType()==ASTNode.VARIABLE_DECLARATION_FRAGMENT)
					{
						long fragmentId = JavaStatementInfo.createVariableDeclarationFragmentNode(inserter, methodName, (VariableDeclarationFragment) element, codeContent);
						if(fragmentId!=-1)
							inserter.createRelationship(nodeId, fragmentId, JavaExtractor.LAMBDA_PARAMETER, new HashMap<>());
						else;
					}
					else
					{
						long singleVarId = JavaStatementInfo.createSingleVarDeclarationNode(inserter, methodName, codeContent, (SingleVariableDeclaration) element);
						if(singleVarId!=-1)
							inserter.createRelationship(nodeId, singleVarId, JavaExtractor.LAMBDA_PARAMETER, new HashMap<>());
						else;
					}
				}
//				lambdaExpression.resolveConstantExpressionValue();
//				lambdaExpression.resolveBoxing();
//				lambdaExpression.resolveMethodBinding();
//				lambdaExpression.resolveTypeBinding();
//				lambdaExpression.resolveUnboxing();
				ASTNode body = lambdaExpression.getBody();
				if(body.getNodeType()==ASTNode.BLOCK)
				{
					long blockId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, codeContent, (Statement) body);
					if(blockId!=-1)
						inserter.createRelationship(nodeId, blockId, JavaExtractor.LAMBDA_BODY, new HashMap<>());
					else;
				}
				else
				{
					long blockId = JavaExpressionInfo.createJavaExpressionNode(inserter, (Expression) body, codeContent, methodName);
					if(blockId!=-1)
						inserter.createRelationship(nodeId, blockId, JavaExtractor.LAMBDA_BODY, new HashMap<>());
					else;
				}
			}
			else if(expression.getNodeType()==ASTNode.MARKER_ANNOTATION)
			{
				expressionType = "MarkerAnnotation";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
				//ArrayAccess arrayAccess = (ArrayAccess)expression;
			}
			else if(expression.getNodeType()==ASTNode.METHOD_INVOCATION)
			{
				expressionType = "MethodInvocation";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.METHOD_REF)
			{
				expressionType = "MethodRef";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.NAME_QUALIFIED_TYPE)
			{
				expressionType = "NameQualifiedType";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.NORMAL_ANNOTATION)
			{
				expressionType = "NormalAnnotation";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				NormalAnnotation normalAnnotation = (NormalAnnotation)expression;
				String typeName = normalAnnotation.getTypeName().getFullyQualifiedName();
				map.put(JavaExtractor.FULLNAME, typeName);
				nodeId = createNode(inserter, map);
				@SuppressWarnings("unchecked")
				List<Expression> values = normalAnnotation.values();
				for(Expression value:values)
				{
					long id = JavaExpressionInfo.createJavaExpressionNode(inserter, value, codeContent, methodName);
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.NORMAL_ANNOTATION_VALUE, new HashMap<>());
					else;
				}
			}
			else if(expression.getNodeType()==ASTNode.NULL_LITERAL)
			{
				expressionType = "NullLiteral";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.PARENTHESIZED_EXPRESSION)
			{
				expressionType = "ParenthesizedExpression";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.POSTFIX_EXPRESSION)
			{
				expressionType = "PostfixExpression";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
				PostfixExpression postfixExpression = (PostfixExpression)expression;
				Expression operand = postfixExpression.getOperand();
				PostfixExpression.Operator operator = postfixExpression.getOperator();
				long oprdId = JavaExpressionInfo.createJavaExpressionNode(inserter, operand, codeContent, methodName);
				long oprtId = JavaExpressionInfo.createJavaPostfixOperatorNode(inserter, operator);
				inserter.createRelationship(nodeId, oprtId, JavaExtractor.POSTFIX, new HashMap<>());
				inserter.createRelationship(oprtId, oprdId, JavaExtractor.POSTFIX_OPRD, new HashMap<>());
//				postfixExpression.resolveBoxing();
//				postfixExpression.resolveConstantExpressionValue();
//				postfixExpression.resolveTypeBinding();
//				postfixExpression.resolveUnboxing();
			}
			else if(expression.getNodeType()==ASTNode.PREFIX_EXPRESSION)
			{
				expressionType = "PrefixExpression";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
				PrefixExpression prefixExpression = (PrefixExpression)expression;
				Expression operand = prefixExpression.getOperand();
				PrefixExpression.Operator operator = prefixExpression.getOperator();
				long oprdId = JavaExpressionInfo.createJavaExpressionNode(inserter, operand, codeContent, methodName);
				long oprtId = JavaExpressionInfo.createJavaPrefixOperatorNode(inserter, operator);
				inserter.createRelationship(nodeId, oprtId, JavaExtractor.PREFIX, new HashMap<>());
				inserter.createRelationship(oprtId, oprdId, JavaExtractor.PREFIX_OPRD, new HashMap<>());
//				prefixExpression.resolveBoxing();
//				prefixExpression.resolveConstantExpressionValue();
//				prefixExpression.resolveTypeBinding();
//				prefixExpression.resolveUnboxing();
			}
			else if(expression.getNodeType()==ASTNode.SINGLE_MEMBER_ANNOTATION)
			{
				expressionType = "SingleMemberAnnotation";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation)expression;
				String typeName = singleMemberAnnotation.getTypeName().getFullyQualifiedName();
				map.put(JavaExtractor.FULLNAME, typeName);
				nodeId = createNode(inserter, map);
				Expression value = singleMemberAnnotation.getValue();
				long valueId = JavaExpressionInfo.createJavaExpressionNode(inserter, value, codeContent, methodName);
				if(valueId!=-1)
					inserter.createRelationship(nodeId, valueId, JavaExtractor.SINGLE_MEMBER_ANNOTATION_VALUE, new HashMap<>());
				else;
			}
			else if(expression.getNodeType()==ASTNode.STRING_LITERAL)
			{
				expressionType = "StringLiteral";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.SUPER_FIELD_ACCESS)
			{
				expressionType = "SuperFieldAccess";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				SuperFieldAccess superFieldAccess = (SuperFieldAccess)expression;
				Name fieldName = superFieldAccess.getQualifier();
				if(fieldName!=null)
				map.put(JavaExtractor.FULLNAME, fieldName.getFullyQualifiedName());
				else				
					map.put(JavaExtractor.FULLNAME, "null");
//				superFieldAccess.resolveBoxing();
//				superFieldAccess.resolveConstantExpressionValue();
//				superFieldAccess.resolveFieldBinding();
//				superFieldAccess.resolveTypeBinding();
//				superFieldAccess.resolveUnboxing();
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.SUPER_METHOD_INVOCATION)
			{
				expressionType = "SuperMethodInvocation";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)expression;
				@SuppressWarnings("unchecked")
				List<Expression> args = superMethodInvocation.arguments();
				@SuppressWarnings("unchecked")
				List<Type> typeArgs = superMethodInvocation.typeArguments();
				String[] typeArgsLiteral = new String[typeArgs.size()];
				for(int i =0;i<typeArgs.size();i++)
				{
					Type type = typeArgs.get(i);
					typeArgsLiteral[i]=type.toString();
				}
				map.put(JavaExtractor.TYPE_ARG_TYPE_STR, typeArgsLiteral);
				Name superMethodName = superMethodInvocation.getQualifier();
				if(superMethodName!=null)
					map.put(JavaExtractor.FULLNAME, superMethodName.getFullyQualifiedName());
				else
					map.put(JavaExtractor.FULLNAME, "null");
//				superMethodInvocation.resolveBoxing();
//				superMethodInvocation.resolveConstantExpressionValue();
//				superMethodInvocation.resolveMethodBinding();
//				superMethodInvocation.resolveTypeBinding();
//				superMethodInvocation.resolveUnboxing();
				nodeId = createNode(inserter, map);
				for(Expression arg:args)
				{
					long id = JavaExpressionInfo.createJavaExpressionNode(inserter, arg, codeContent, methodName);
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.HAVE_PARAM, new HashMap<>());
					else;
				}
			}
			else if(expression.getNodeType()==ASTNode.THIS_EXPRESSION)
			{
				expressionType = "ThisExpression";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				ThisExpression thisExpression = (ThisExpression)expression;
				Name thisName = thisExpression.getQualifier();
				if(thisName!=null)
					map.put(JavaExtractor.FULLNAME, thisName.getFullyQualifiedName());
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
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.TYPE_METHOD_REFERENCE)
			{
				expressionType = "TypeMethodReference";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
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
					if(element.isAnnotatable())
						typeArgsDeclaredTypes[i]="Annotatable";
					else if(element.isArrayType())
						typeArgsDeclaredTypes[i]="ArrayType";
					else if(element.isIntersectionType())
						typeArgsDeclaredTypes[i]="IntersectionType";
					else if(element.isNameQualifiedType())
						typeArgsDeclaredTypes[i]="NameQualifiedType";
					else if(element.isParameterizedType())
						typeArgsDeclaredTypes[i]="ParameterizedType";
					else if(element.isPrimitiveType())
						typeArgsDeclaredTypes[i]="PrimitiveType";
					else if(element.isQualifiedType())
						typeArgsDeclaredTypes[i]="QualifiedType";
					else if(element.isSimpleType())
						typeArgsDeclaredTypes[i]="SimpleType";
					else if(element.isUnionType())
						typeArgsDeclaredTypes[i]="UnionType";
					else if(element.isVar())
						typeArgsDeclaredTypes[i]="Varialbe";
					else if(element.isWildcardType())
						typeArgsDeclaredTypes[i]="WildcardType";
				}
				map.put(JavaExtractor.DECLARED_TYPE, typeArgsDeclaredTypes);
				map.put(JavaExtractor.TYPE_ARG_TYPE_STR, typeArgsTypes);
				nodeId = createNode(inserter, map);
			}
			else if(expression.getNodeType()==ASTNode.VARIABLE_DECLARATION_EXPRESSION)
			{
				expressionType = "VariableDeclarationExpression";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)expression;
				int modifier = variableDeclarationExpression.getModifiers();
				JavaExpressionInfo.addModifierProperty(map,modifier);
				
				variableDeclarationExpression.fragments();
				Type type = variableDeclarationExpression.getType();
				JavaExpressionInfo.addDeclaredTypeProperty(map, type);
				map.put(JavaExtractor.VAR_TYPE_STR, type.toString());
				nodeId = createNode(inserter, map);
				
				@SuppressWarnings("unchecked")
				List<VariableDeclarationFragment> fragments = variableDeclarationExpression.fragments();
				for(VariableDeclarationFragment element : fragments)
				{
					long id = JavaStatementInfo.createVariableDeclarationFragmentNode(inserter, methodName, element, codeContent);
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
				map.put(JavaExtractor.IDENTIFIER, name);
				map.put(JavaExtractor.QUALIFIER, qualifier);
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
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
				map.put(JavaExtractor.IDENTIFIER, identifier);
				if(simpleName.isDeclaration())
					map.put(JavaExtractor.SIMPLENAME_TYPE, "Declaration");
				else if(simpleName.isVar())
					map.put(JavaExtractor.SIMPLENAME_TYPE, "Var");
				else if(simpleName.isSimpleName())
					map.put(JavaExtractor.SIMPLENAME_TYPE, "SimpleName");
				else if(simpleName.isQualifiedName())
					map.put(JavaExtractor.SIMPLENAME_TYPE, "QualifiedName");
				else;
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
//				simpleName.resolveBinding();
//				simpleName.resolveBoxing();
//				simpleName.resolveConstantExpressionValue();
//				simpleName.resolveTypeBinding();
//				simpleName.resolveUnboxing();
			}
			else if(expression.getNodeType()==ASTNode.NUMBER_LITERAL)
			{
				expressionType = "NumberLiteral";
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
			}
			else
			{
				expressionType = ""+expression.getNodeType();
				addCommonProperties(map, expression, expressionType, methodName, codeContent);
				nodeId = createNode(inserter, map);
				System.out.println(expressionType);
			}
			return nodeId;
		}
		else
			return -1;
	}

	protected static void addModifierProperty(HashMap<String, Object> map, int modifier) {
		// TODO Auto-generated method stub
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
	}

	protected static void addDeclaredTypeProperty(HashMap<String, Object> map, Type type) {
		// TODO Auto-generated method stub
		if(type.isAnnotatable())
		map.put(JavaExtractor.DECLARED_TYPE, "Annotatable");
		else if(type.isArrayType())
		map.put(JavaExtractor.DECLARED_TYPE, "ArrayType");
		else if(type.isIntersectionType())
		map.put(JavaExtractor.DECLARED_TYPE, "IntersectionType");
		else if(type.isNameQualifiedType())
		map.put(JavaExtractor.DECLARED_TYPE, "NameQualifiedType");
		else if(type.isParameterizedType())
		map.put(JavaExtractor.DECLARED_TYPE, "ParameterizedType");
		else if(type.isPrimitiveType())
		map.put(JavaExtractor.DECLARED_TYPE, "PrimitiveType");
		else if(type.isQualifiedType())
		map.put(JavaExtractor.DECLARED_TYPE, "QualifiedType");
		else if(type.isSimpleType())
		map.put(JavaExtractor.DECLARED_TYPE, "SimpleType");
		else if(type.isUnionType())
		map.put(JavaExtractor.DECLARED_TYPE, "UnionType");
		else if(type.isVar())
		map.put(JavaExtractor.DECLARED_TYPE, "Varialbe");
		else if(type.isWildcardType())
		map.put(JavaExtractor.DECLARED_TYPE, "WildcardType");
	}

	protected static long createJavaInfixOperatorNode(BatchInserter inserter,
			org.eclipse.jdt.core.dom.InfixExpression.Operator operator) {
		if(operator!=null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put(JavaExtractor.OPERATOR_TYPE, "InfixOperator");
			map.put(JavaExtractor.OPERATOR_LITERAL,operator.toString());
			return inserter.createNode(map, JavaExtractor.OPERATOR);
		}
		else
			return -1;
	}

	protected static long createJavaPostfixOperatorNode(BatchInserter inserter,
			org.eclipse.jdt.core.dom.PostfixExpression.Operator operator) {
		if(operator!=null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put(JavaExtractor.OPERATOR_TYPE, "PostfixOperator");
			map.put(JavaExtractor.OPERATOR_LITERAL,operator.toString());
			return inserter.createNode(map, JavaExtractor.OPERATOR);
		}
		else
			return -1;
	}

	protected static long createJavaPrefixOperatorNode(BatchInserter inserter,
			org.eclipse.jdt.core.dom.PrefixExpression.Operator operator) {
		if(operator!=null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put(JavaExtractor.OPERATOR_TYPE, "PrefixOperator");
			map.put(JavaExtractor.OPERATOR_LITERAL,operator.toString());
			return inserter.createNode(map, JavaExtractor.OPERATOR);
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

	private static void addCommonProperties(HashMap<String, Object> map, Expression expression, String expressionType, String methodName, String codeContent) {
		// TODO Auto-generated method stub
		map.put(JavaExtractor.EXPRESSION_TYPE, expressionType);
		map.put(JavaExtractor.METHOD_NAME,methodName);
		map.put(JavaExtractor.CONTENT, codeContent.substring(expression.getStartPosition(), expression.getStartPosition()+expression.getLength()));
		map.put(JavaExtractor.ROW_NO, codeContent.substring(0, expression.getStartPosition()).split("\n").length);
	}
	
	private static long createNode(BatchInserter inserter, HashMap<String, Object> map)
	{
		return inserter.createNode(map, JavaExtractor.EXPREESION);
	}

}
