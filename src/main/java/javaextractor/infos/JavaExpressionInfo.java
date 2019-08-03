package main.java.javaextractor.infos;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.*;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
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
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.javaextractor.JavaASTVisitor;
import main.java.javaextractor.JavaExtractor;
import main.java.javaextractor.JavaStatementVisitor;
import main.java.javaextractor.NameResolver;

public abstract class JavaExpressionInfo {
	private static long nodeId = 0;
	private static String expressionType = "";
	
	@SuppressWarnings("unchecked")
	public static long createJavaExpressionNode(BatchInserter inserter, Expression expression, String sourceContent, String methodName, JavaProjectInfo javaProjectInfo)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		
		if(expression!=null)
		{	
			if(expression.resolveConstantExpressionValue() != null)
				map.put(JavaExtractor.CONST_EXPR_VALUE, expression.resolveConstantExpressionValue());
			else
				map.put(JavaExtractor.CONST_EXPR_VALUE, "null");

//			expression.resolveBoxing();
//			expression.resolveUnboxing();
			switch(expression.getNodeType())
			{
				case ASTNode.ARRAY_ACCESS:
				{
					expressionType = "ArrayAccess";
					ArrayAccess arrayAccess = (ArrayAccess)expression;
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					Expression array = arrayAccess.getArray();
					Expression index = arrayAccess.getIndex();
					JavaExpressionInfo.createRelationship(inserter, array, nodeId, JavaExtractor.ARRAY_ACCESS, sourceContent, methodName, javaProjectInfo);
					JavaExpressionInfo.createRelationship(inserter, index, nodeId, JavaExtractor.ARRAY_ACCESS_INDEX, sourceContent, methodName, javaProjectInfo);
				}break;
					
				case ASTNode.ARRAY_CREATION:
				{
					expressionType = "ArrayCreation";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					ArrayCreation arrayCreation = (ArrayCreation)expression;
					ArrayType arrayType = arrayCreation.getType();
					Type elementType = arrayType.getElementType();
					int dimension = arrayType.getDimensions();
					map.put(JavaExtractor.DIMENSION_NUM, dimension);
					map.put(JavaExtractor.ARRAY_TYPE, arrayType.toString());
					map.put(JavaExtractor.ELEMENT_TYPE, elementType.toString());
					nodeId = createNode(inserter, map);
					
					Expression arrayInitializer = arrayCreation.getInitializer();
					List<Expression> dimensions = arrayCreation.dimensions();
					JavaExpressionInfo.createRelationship(inserter, arrayInitializer, nodeId, JavaExtractor.ARRAY_INITIALIZER, sourceContent, methodName, javaProjectInfo);
					for(Expression element : dimensions)
					{
						JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.DIMENSIONS, sourceContent, methodName, javaProjectInfo);
					}
					JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, arrayType.resolveBinding(), JavaExtractor.ARRAY_TYPE_BINDING);
					JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, elementType.resolveBinding(), JavaExtractor.ARRAY_ELEMENT_TYPE_BINDING);
				}break;
				
				case ASTNode.ARRAY_INITIALIZER:
				{
					expressionType = "ArrayInitializer";
					ArrayInitializer arrayInitializer = (ArrayInitializer)expression;
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					List<Expression> initializerExpressions = arrayInitializer.expressions();
					for(Expression element : initializerExpressions)
					{				
						JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.SUB_ARRAY_INITIALIZER, sourceContent, methodName, javaProjectInfo);
					}
				}break;
				
				case ASTNode.ASSIGNMENT:
				{
					expressionType = "Assignment";
					Assignment assignment = (Assignment)expression;
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					Expression leftHandSide = assignment.getLeftHandSide();
					Expression rightHandSide = assignment.getRightHandSide();
					Operator operator = assignment.getOperator();
					long oprtId = JavaExpressionInfo.createJavaAssignmentOperatorNode(inserter, operator);
					if(oprtId!=-1)
						inserter.createRelationship(nodeId, oprtId, JavaExtractor.ASSIGNMENT, new HashMap<>());
					else;
					JavaExpressionInfo.createRelationship(inserter, leftHandSide, nodeId, JavaExtractor.LEFT_OPERAND, sourceContent, methodName, javaProjectInfo);
					JavaExpressionInfo.createRelationship(inserter, rightHandSide, nodeId, JavaExtractor.RIGHT_OPERAND, sourceContent, methodName, javaProjectInfo);
				}break;
				
				case ASTNode.BOOLEAN_LITERAL:
				{
					expressionType = "BooleanLiteral";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
				}break;
				
				case ASTNode.CAST_EXPRESSION:
				{
					expressionType = "CastExpression";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					CastExpression castExpression = (CastExpression)expression;
					JavaExpressionInfo.addDeclaredTypeProperty(map, castExpression.getType());
					ITypeBinding typeCastedToBinding = castExpression.getType().resolveBinding();
					if(typeCastedToBinding != null)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, typeCastedToBinding, JavaExtractor.CASTING_TYPE_BINDING);
					nodeId = createNode(inserter, map);
					
					Expression castedExpression = castExpression.getExpression();
					JavaExpressionInfo.createRelationship(inserter, castedExpression, nodeId, JavaExtractor.CAST, sourceContent, methodName, javaProjectInfo);
				}break;
				
				case ASTNode.CHARACTER_LITERAL:
				{
					expressionType = "CharacterLiteral";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
				}break;
				
				case ASTNode.CLASS_INSTANCE_CREATION:
				{
					expressionType = "ClassInstanceCreation";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation)expression;
					map.put(JavaExtractor.TYPE_NAME, classInstanceCreation.getType().toString());
					JavaExpressionInfo.addDeclaredTypeProperty(map, classInstanceCreation.getType());
					List<ITypeBinding> bindedTypeList = JavaExpressionInfo.addTypeArgProperties(map,classInstanceCreation.typeArguments());
					nodeId = createNode(inserter, map);
					
					List<Expression> argsList = classInstanceCreation.arguments();
					for(Expression element : argsList)
					{
						JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.TYPE_ARG, sourceContent, methodName, javaProjectInfo);
					}
					
					for(ITypeBinding element : bindedTypeList)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element, JavaExtractor.TYPE_ARG_TYPE_BINDING);
					
					AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
					if(anonymousClassDeclaration != null)
					{
						HashMap<String, Object> innerMap = new HashMap<>();
						innerMap.put(JavaExtractor.SUPER_CLASS, anonymousClassDeclaration.resolveBinding().getSuperclass().getQualifiedName());
						ITypeBinding[] typeBindings = anonymousClassDeclaration.resolveBinding().getTypeArguments();
						for(ITypeBinding element : typeBindings)
							JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element, JavaExtractor.TYPE_ARG_TYPE_BINDING);
						innerMap.put(JavaExtractor.METHOD_NAME,methodName);
						innerMap.put(JavaExtractor.CONTENT, sourceContent.substring(expression.getStartPosition(), expression.getStartPosition()+expression.getLength()));
						innerMap.put(JavaExtractor.ROW_NO, sourceContent.substring(0, expression.getStartPosition()).split("\n").length);					
						long anonymousClassId = inserter.createNode(innerMap, JavaExtractor.ANONYMOUS_CLASS);
						inserter.createRelationship(nodeId, anonymousClassId, JavaExtractor.ANONYMOUS_CLASS_DECLARATION, new HashMap<>());
						List<BodyDeclaration> bodyList = anonymousClassDeclaration.bodyDeclarations();
						for(BodyDeclaration element : bodyList)
						{
							if(element.getNodeType() == ASTNode.FIELD_DECLARATION)
							{
								FieldDeclaration fieldDeclaration = (FieldDeclaration)element;
								String type = fieldDeclaration.getType().toString();
						        String fullType = NameResolver.getFullName(fieldDeclaration.getType());
						        String visibility = JavaASTVisitor.getVisibility(fieldDeclaration.getModifiers());
						        boolean isStatic = Modifier.isStatic(element.getModifiers());
						        boolean isFinal = Modifier.isFinal(fieldDeclaration.getModifiers());
						        String comment = fieldDeclaration.getJavadoc() == null ? "" : sourceContent.substring(fieldDeclaration.getJavadoc().getStartPosition(), fieldDeclaration.getJavadoc().getStartPosition() + fieldDeclaration.getJavadoc().getLength());
						        fieldDeclaration.fragments().forEach(n -> {
						            VariableDeclarationFragment fragment = (VariableDeclarationFragment) n;
						            String name = fragment.getName().getFullyQualifiedName();
						            String fullName = methodName + classInstanceCreation.getType().toString() + "." + name;
						            JavaFieldInfo fieldInfo = new JavaFieldInfo(inserter, name, fullName, type, visibility, isStatic, isFinal, comment, methodName, fullType);
						            inserter.createRelationship(anonymousClassId, fieldInfo.getNodeId(), JavaExtractor.HAVE_FIELD, new HashMap<>());
						        });
							}
							else if(element.getNodeType() == ASTNode.METHOD_DECLARATION)
							{
								MethodDeclaration methodDeclaration = (MethodDeclaration)element;
								JavaMethodInfo methodInfo = JavaStatementVisitor.createJavaMethodInfo(methodDeclaration, methodName);
								inserter.createRelationship(anonymousClassId, methodInfo.getNodeId(), JavaExtractor.HAVE_METHOD, new HashMap<>());
								List<Statement> statementList = methodDeclaration.getBody().statements();
								for(Statement statement : statementList)
								{
									long statementId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, sourceContent, statement);
									if(statementId != -1)
										inserter.createRelationship(methodInfo.getNodeId(), statementId, JavaExtractor.HAVE_STATEMENT, new HashMap<>());
									else;
								}
							}
							else if(element.getNodeType() == ASTNode.INITIALIZER)
							{
								Initializer initializer = (Initializer)element;
								long iniId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, sourceContent, initializer.getBody());
								if(iniId != -1)
									inserter.createRelationship(anonymousClassId, iniId, JavaExtractor.INITIALIZER, new HashMap<>());
								else;
							}
							else
								System.out.println(element.toString());
						}
					}
					Expression instanceCreation = classInstanceCreation.getExpression();
					JavaExpressionInfo.createRelationship(inserter, instanceCreation, nodeId, JavaExtractor.CREATED_BY, sourceContent, methodName, javaProjectInfo);
					
					JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, classInstanceCreation.resolveConstructorBinding());
				}break;
				
				case ASTNode.CONDITIONAL_EXPRESSION:
				{
					expressionType = "ConditionalExpression";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					ConditionalExpression conditionalExpression = (ConditionalExpression)expression;
					JavaExpressionInfo.createRelationship(inserter, conditionalExpression.getExpression(), nodeId, JavaExtractor.ENTER_CONDITION, sourceContent, methodName, javaProjectInfo);
					JavaExpressionInfo.createRelationship(inserter, conditionalExpression.getThenExpression(), nodeId, JavaExtractor.THEN, sourceContent, methodName, javaProjectInfo);
					JavaExpressionInfo.createRelationship(inserter, conditionalExpression.getElseExpression(), nodeId, JavaExtractor.ELSE, sourceContent, methodName, javaProjectInfo);
				}break;
				
				case ASTNode.CREATION_REFERENCE:
				{
					expressionType = "CreationReference";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					CreationReference creationReference = (CreationReference)expression;
					JavaExpressionInfo.addDeclaredTypeProperty(map, creationReference.getType());
					List<ITypeBinding> bindedTypeList = JavaExpressionInfo.addTypeArgProperties(map, creationReference.typeArguments());
					nodeId = createNode(inserter, map);
					
					for(ITypeBinding element : bindedTypeList)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element, JavaExtractor.TYPE_ARG_TYPE_BINDING);
					JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, creationReference.resolveMethodBinding());
				}break;
				
				case ASTNode.EXPRESSION_METHOD_REFERENCE:
				{
					expressionType = "ExpressionMethodReference";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					ExpressionMethodReference expressionMethodReference = (ExpressionMethodReference)expression;
					map.put(JavaExtractor.IDENTIFIER, expressionMethodReference.getName().getIdentifier());
					List<ITypeBinding> bindedTypeList = JavaExpressionInfo.addTypeArgProperties(map, expressionMethodReference.typeArguments());
					nodeId = createNode(inserter, map);
					
					Expression ref = expressionMethodReference.getExpression();
					JavaExpressionInfo.createRelationship(inserter, ref, nodeId, JavaExtractor.DOMAIN, sourceContent, methodName, javaProjectInfo);
					
					for(ITypeBinding element : bindedTypeList)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element, JavaExtractor.TYPE_ARG_TYPE_BINDING);
					JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, expressionMethodReference.resolveMethodBinding());
				}break;
				
				case ASTNode.FIELD_ACCESS:
				{
					expressionType = "FieldAccess";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					FieldAccess fieldAccess = (FieldAccess)expression;
					map.put(JavaExtractor.IDENTIFIER, fieldAccess.getName().getIdentifier());
					nodeId = createNode(inserter, map);
					
					Expression access = fieldAccess.getExpression();
					JavaExpressionInfo.createRelationship(inserter, access, nodeId, JavaExtractor.FIELD_ACCESS, sourceContent, methodName, javaProjectInfo);
					
//					IVariableBinding fieldBinding = fieldAccess.resolveFieldBinding();
//					if(fieldBinding != null)
//						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, fieldBinding.getType().getQualifiedName());
				}break;
				
				case ASTNode.INFIX_EXPRESSION:
				{
					expressionType = "InfixExpression";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					InfixExpression prefixExpression = (InfixExpression)expression;
					Expression leftOperand = prefixExpression.getLeftOperand();
					Expression rightOperand = prefixExpression.getRightOperand();
					InfixExpression.Operator operator = prefixExpression.getOperator();
					long oprtId = JavaExpressionInfo.createJavaInfixOperatorNode(inserter, operator);
					inserter.createRelationship(nodeId, oprtId, JavaExtractor.INFIX, new HashMap<>());
					JavaExpressionInfo.createRelationship(inserter, leftOperand, nodeId, JavaExtractor.LEFT_OPERAND, sourceContent, methodName, javaProjectInfo);
					JavaExpressionInfo.createRelationship(inserter, rightOperand, nodeId, JavaExtractor.RIGHT_OPERAND, sourceContent, methodName, javaProjectInfo);
				}break;
				
				case ASTNode.INSTANCEOF_EXPRESSION:
				{
					expressionType = "InstanceofExpression";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					InstanceofExpression instanceofExpression = (InstanceofExpression)expression;
					JavaExpressionInfo.addDeclaredTypeProperty(map,instanceofExpression.getRightOperand());
					map.put(JavaExtractor.TYPE_NAME, instanceofExpression.getRightOperand().toString());
					Type type = instanceofExpression.getRightOperand();
					nodeId = createNode(inserter, map);
					
					Expression left = instanceofExpression.getLeftOperand();
					JavaExpressionInfo.createRelationship(inserter, left, nodeId, JavaExtractor.LEFT_OPERAND, sourceContent, methodName, javaProjectInfo);
					
					JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, type.resolveBinding(), JavaExtractor.INSTANCE_OF_TYPE_BINDING);
				}break;
				
				case ASTNode.LAMBDA_EXPRESSION:
				{
					expressionType = "LambdaExpression";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					LambdaExpression lambdaExpression = (LambdaExpression)expression;
					List<VariableDeclaration> paramList = lambdaExpression.parameters();
					for(VariableDeclaration element : paramList)
					{
						if(element.getNodeType()==ASTNode.VARIABLE_DECLARATION_FRAGMENT)
						{
							long fragmentId = JavaStatementInfo.createVariableDeclarationFragmentNode(inserter, javaProjectInfo, methodName, (VariableDeclarationFragment) element, sourceContent);
							if(fragmentId!=-1)
								inserter.createRelationship(nodeId, fragmentId, JavaExtractor.LAMBDA_PARAMETER, new HashMap<>());
							else;
						}
						else
						{
							long singleVarId = JavaStatementInfo.createSingleVarDeclarationNode(inserter, javaProjectInfo, methodName, sourceContent, (SingleVariableDeclaration) element);
							if(singleVarId!=-1)
								inserter.createRelationship(nodeId, singleVarId, JavaExtractor.LAMBDA_PARAMETER, new HashMap<>());
							else;
						}
					}
					ASTNode body = lambdaExpression.getBody();
					if(body.getNodeType()==ASTNode.BLOCK)
					{
						long blockId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, sourceContent, (Statement) body);
						if(blockId!=-1)
							inserter.createRelationship(nodeId, blockId, JavaExtractor.LAMBDA_BODY, new HashMap<>());
						else;
					}
					else
					{
						long blockId = JavaExpressionInfo.createJavaExpressionNode(inserter, (Expression) body, sourceContent, methodName, javaProjectInfo);
						if(blockId!=-1)
							inserter.createRelationship(nodeId, blockId, JavaExtractor.LAMBDA_BODY, new HashMap<>());
						else;
					}
					
					JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, lambdaExpression.resolveMethodBinding());
				}break;
				
				case ASTNode.MARKER_ANNOTATION:
				{
					expressionType = "MarkerAnnotation";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
				}break;
				
				case ASTNode.METHOD_INVOCATION:
				{
					expressionType = "MethodInvocation";
					MethodInvocation methodInvocation = (MethodInvocation)expression;
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					map.put(JavaExtractor.NAME, methodInvocation.getName().getFullyQualifiedName());
					map.put(JavaExtractor.IS_RESOLVED_TYPE_INFERRED_FROM_EXPECTED_TYPE, methodInvocation.isResolvedTypeInferredFromExpectedType());
					nodeId = createNode(inserter, map);
					
					List<Expression> argsList = methodInvocation.arguments();
					for(Expression element : argsList)
					{
						JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.HAVE_ARG, sourceContent, methodName, javaProjectInfo);
					}
					Expression invok = methodInvocation.getExpression();
					JavaExpressionInfo.createRelationship(inserter, invok, nodeId, JavaExtractor.INVOCATED_BY, sourceContent, methodName, javaProjectInfo);

					JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, methodInvocation.resolveMethodBinding());
				}break;
				
				case ASTNode.METHOD_REF:
				{
					expressionType = "MethodRef";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
				}break;
				
				case ASTNode.NAME_QUALIFIED_TYPE:
				{
					expressionType = "NameQualifiedType";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
				}break;
				
				case ASTNode.NORMAL_ANNOTATION:
				{
					expressionType = "NormalAnnotation";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					NormalAnnotation normalAnnotation = (NormalAnnotation)expression;
					map.put(JavaExtractor.FULLNAME, normalAnnotation.getTypeName().getFullyQualifiedName());
					nodeId = createNode(inserter, map);
					List<Expression> values = normalAnnotation.values();
					for(Expression element : values)
					{
						JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.NORMAL_ANNOTATION_VALUE, sourceContent, methodName, javaProjectInfo);
					}	
				}break;
				
				case ASTNode.NULL_LITERAL:
				{
					expressionType = "NullLiteral";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
				}break;
				
				case ASTNode.PARENTHESIZED_EXPRESSION:
				{
					expressionType = "ParenthesizedExpression";
					ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression)expression;
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					Expression innerExpression = parenthesizedExpression.getExpression();
					JavaExpressionInfo.createRelationship(inserter, innerExpression, nodeId, JavaExtractor.PARENTHESIZE, sourceContent, methodName, javaProjectInfo);
				}break;
				
				case ASTNode.POSTFIX_EXPRESSION:
				{
					expressionType = "PostfixExpression";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					PostfixExpression postfixExpression = (PostfixExpression)expression;
					Expression operand = postfixExpression.getOperand();
					JavaExpressionInfo.createRelationship(inserter, operand, nodeId, JavaExtractor.POSTFIX_OPRD, sourceContent, methodName, javaProjectInfo);

					PostfixExpression.Operator operator = postfixExpression.getOperator();
					long oprtId = JavaExpressionInfo.createJavaPostfixOperatorNode(inserter, operator);
					inserter.createRelationship(nodeId, oprtId, JavaExtractor.POSTFIX, new HashMap<>());
				}break;
				
				case ASTNode.PREFIX_EXPRESSION:
				{
					expressionType = "PrefixExpression";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					PrefixExpression prefixExpression = (PrefixExpression)expression;
					Expression operand = prefixExpression.getOperand();
					JavaExpressionInfo.createRelationship(inserter, operand, nodeId, JavaExtractor.PREFIX_OPRD, sourceContent, methodName, javaProjectInfo);

					PrefixExpression.Operator operator = prefixExpression.getOperator();
					long oprtId = JavaExpressionInfo.createJavaPrefixOperatorNode(inserter, operator);
					inserter.createRelationship(nodeId, oprtId, JavaExtractor.PREFIX, new HashMap<>());
				}break;
				
				case ASTNode.SINGLE_MEMBER_ANNOTATION:
				{
					expressionType = "SingleMemberAnnotation";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation)expression;
					String typeName = singleMemberAnnotation.getTypeName().getFullyQualifiedName();
					map.put(JavaExtractor.FULLNAME, typeName);
					nodeId = createNode(inserter, map);
					
					Expression value = singleMemberAnnotation.getValue();
					JavaExpressionInfo.createRelationship(inserter, value, nodeId, JavaExtractor.SINGLE_MEMBER_ANNOTATION_VALUE, sourceContent, methodName, javaProjectInfo);
				}break;
				
				case ASTNode.STRING_LITERAL:
				{
					expressionType = "StringLiteral";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
				}break;
				
				case ASTNode.SUPER_FIELD_ACCESS:
				{
					expressionType = "SuperFieldAccess";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					SuperFieldAccess superFieldAccess = (SuperFieldAccess)expression;
					Name fieldName = superFieldAccess.getQualifier();
					if(fieldName!=null)
						map.put(JavaExtractor.FULLNAME, fieldName.getFullyQualifiedName());
					else				
						map.put(JavaExtractor.FULLNAME, "null");
					nodeId = createNode(inserter, map);
					
					IVariableBinding fieldBinding = superFieldAccess.resolveFieldBinding();
					if(fieldBinding != null)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, fieldBinding.getType(), JavaExtractor.FIELD_TYPE_BINDING);
				}break;
				
				case ASTNode.SUPER_METHOD_INVOCATION:
				{
					expressionType = "SuperMethodInvocation";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)expression;
					List<ITypeBinding> bindedTypeList = JavaExpressionInfo.addTypeArgProperties(map, superMethodInvocation.typeArguments());
					Name superMethodName = superMethodInvocation.getQualifier();
					if(superMethodName!=null)
						map.put(JavaExtractor.FULLNAME, superMethodName.getFullyQualifiedName());
					else
						map.put(JavaExtractor.FULLNAME, "null");
					nodeId = createNode(inserter, map);
					
					List<Expression> args = superMethodInvocation.arguments();
					for(Expression element : args)
					{
						JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.HAVE_ARG, sourceContent, methodName, javaProjectInfo);
					}
					
					for(ITypeBinding element : bindedTypeList)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element, JavaExtractor.TYPE_ARG_TYPE_BINDING);
					JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, superMethodInvocation.resolveMethodBinding());
				}break;
				
				case ASTNode.THIS_EXPRESSION:
				{
					expressionType = "ThisExpression";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					ThisExpression thisExpression = (ThisExpression)expression;
					Name thisName = thisExpression.getQualifier();
					if(thisName!=null)
						map.put(JavaExtractor.FULLNAME, thisName.getFullyQualifiedName());
					else
						map.put(JavaExtractor.FULLNAME, "null");
					nodeId = createNode(inserter, map);			
				}break;
				
				case ASTNode.TYPE_LITERAL:
				{
					expressionType = "TypeLiteral";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					TypeLiteral typeLiteral = (TypeLiteral)expression;
					JavaExpressionInfo.addDeclaredTypeProperty(map, typeLiteral.getType());
					map.put(JavaExtractor.TYPE_NAME, typeLiteral.getType().toString());
					nodeId = createNode(inserter, map);
					
					JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, typeLiteral.getType().resolveBinding(), JavaExtractor.TYPE_BINDING);
				}break;
				
				case ASTNode.TYPE_METHOD_REFERENCE:
				{
					expressionType = "TypeMethodReference";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					TypeMethodReference typeMethodReference = (TypeMethodReference)expression;
					map.put(JavaExtractor.IDENTIFIER, typeMethodReference.getName().getIdentifier());
					ITypeBinding typeMethodReferenceTypeBinding = typeMethodReference.getType().resolveBinding();
					if(typeMethodReferenceTypeBinding != null)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, typeMethodReferenceTypeBinding, JavaExtractor.TYPE_BINDING);
					map.put(JavaExtractor.METHOD_TYPE, typeMethodReference.getType().toString());
					List<ITypeBinding> bindedTypeNameList = JavaExpressionInfo.addTypeArgProperties(map, typeMethodReference.typeArguments());
					nodeId = createNode(inserter, map);
					
					for(ITypeBinding element : bindedTypeNameList)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element,JavaExtractor.TYPE_ARG_TYPE_BINDING);
					JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, typeMethodReference.resolveMethodBinding());
				}break;
				
				case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
				{
					expressionType = "VariableDeclarationExpression";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)expression;
					JavaExpressionInfo.addModifierProperty(map,variableDeclarationExpression.getModifiers());
					JavaExpressionInfo.addDeclaredTypeProperty(map, variableDeclarationExpression.getType());
					map.put(JavaExtractor.VAR_TYPE_STR, variableDeclarationExpression.getType().toString());
					nodeId = createNode(inserter, map);
					
					List<VariableDeclarationFragment> fragments = variableDeclarationExpression.fragments();
					for(VariableDeclarationFragment element : fragments)
					{
						long id = JavaStatementInfo.createVariableDeclarationFragmentNode(inserter, javaProjectInfo, methodName, element, sourceContent);
						if(id!=-1)
							inserter.createRelationship(nodeId, id, JavaExtractor.VAR_DECLARATION_FRAG, new HashMap<>());
						else;
					}
				}break;
				
				case ASTNode.QUALIFIED_NAME:
				{
					expressionType = "QualifiedName";
					QualifiedName qualifiedName = (QualifiedName)expression;
					map.put(JavaExtractor.IDENTIFIER, qualifiedName.getName().getIdentifier());
					map.put(JavaExtractor.QUALIFIER, qualifiedName.getQualifier().toString());
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
					//IBinding binding = qualifiedName.resolveBinding();
					//if(binding != null)
						//JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, map, binding.getClass().getName());
				}break;
				
				case ASTNode.SIMPLE_NAME:
				{
					expressionType = "SimpleName";
					SimpleName simpleName = (SimpleName)expression;
					String identifier = simpleName.getIdentifier();
					map.put(JavaExtractor.IDENTIFIER, identifier);
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					
//					IBinding binding = simpleName.resolveBinding();
//					if(binding != null)
//						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, binding);			
				}break;
				
				case ASTNode.NUMBER_LITERAL:
				{
					expressionType = "NumberLiteral";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
				}break;
				
				default:
				{
					expressionType = "OtherExpressionType";
					addCommonProperties(map, expression, expressionType, methodName, sourceContent);
					nodeId = createNode(inserter, map);
					System.out.println(expressionType);
				}
			}
			
			if(expression.resolveTypeBinding() != null )
				JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, expression.resolveTypeBinding(),JavaExtractor.TYPE_BINDING);
			return nodeId;
		}
		else
			return -1;
	}
	
	static void createRelationship(BatchInserter inserter, Expression expression, long originalNodeId, RelationshipType relationship, String sourceContent, String methodName, JavaProjectInfo javaProjectInfo) 
	{
		long terminalNodeId = JavaExpressionInfo.createJavaExpressionNode(inserter, expression, sourceContent, methodName, javaProjectInfo);
		if(terminalNodeId != -1)
			inserter.createRelationship(originalNodeId, terminalNodeId, relationship, new HashMap<>());
		else;
	}
	
	static void resolveMethodBinding(BatchInserter inserter, JavaProjectInfo javaProjectInfo, long nodeId, IMethodBinding methodBinding)
	{
		if(javaProjectInfo.containsBindedMethod(methodBinding))
			inserter.createRelationship(nodeId, javaProjectInfo.getBindedMethodId(methodBinding), JavaExtractor.METHOD_BINDING, new HashMap<>());
		else;
	}

	static void resolveTypeBinding(BatchInserter inserter, JavaProjectInfo javaProjectInfo, long nodeId, ITypeBinding typeBinding, RelationshipType relationship) {
		String typeName = typeBinding.getQualifiedName();
		if(javaProjectInfo.containsBindedClass(typeName))
			inserter.createRelationship(nodeId, javaProjectInfo.getBindedTypeId(typeName), relationship, new HashMap<>());
		else 
//			if(!typeBinding.isPrimitive() && !typeBinding.isTopLevel() && !typeBinding.isNullType() 
//				&& !typeBinding.isArray() && !typeBinding.isParameterizedType())
//			System.out.println("non-exist:   "+typeBinding+"\n");
			;
	}

	static List<ITypeBinding> addTypeArgProperties(HashMap<String, Object> map, List<Type> typeArguments) {
		// TODO Auto-generated method stub
		String[] typeArgsDeclaredTypes = new String[typeArguments.size()];
		String[] typeArgsTypes = new String[typeArguments.size()];
		List<ITypeBinding> bindedTypeList = new LinkedList<>();
		for(int i = 0; i<typeArguments.size(); i++)
		{
			Type element = typeArguments.get(i);
			typeArgsTypes[i]=element.toString();
			bindedTypeList.add(element.resolveBinding());
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
		map.put(JavaExtractor.TYPE_ARG_DECLARED_TYPE, typeArgsDeclaredTypes);
		map.put(JavaExtractor.TYPE_ARG_TYPE_NAME, typeArgsTypes);
		return bindedTypeList;
	}

	static void addModifierProperty(HashMap<String, Object> map, int modifier) {
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

	static void addDeclaredTypeProperty(HashMap<String, Object> map, Type type) {
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

	private static long createJavaInfixOperatorNode(BatchInserter inserter,
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

	private static long createJavaPostfixOperatorNode(BatchInserter inserter,
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

	private static long createJavaPrefixOperatorNode(BatchInserter inserter,
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
