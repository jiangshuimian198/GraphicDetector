package main.java.infos;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public abstract class JavaStatementInfo {
	private static int conditionNo = 0;
	private static long nodeId = 0;
	private static String statementType = "";
	
	@SuppressWarnings("unchecked")
	public static long createJavaStatementNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String methodName, String sourceContent, Statement statement)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		if(statement!=null) 
		{
			switch(statement.getNodeType())
			{
				case ASTNode.ASSERT_STATEMENT:
				{
		        	statementType="AssertStatement";
					addProperties(statement, sourceContent, map, statementType, methodName);
					AssertStatement assertStatement = (AssertStatement)statement;
					nodeId = createNode(inserter, map);
					
					Expression assertExpression = assertStatement.getExpression();
		    		JavaExpressionInfo.createRelationship(inserter, assertExpression, nodeId, JavaExtractor.ASSERT, sourceContent, methodName, javaProjectInfo);
		        }break;
		        
				case ASTNode.BLOCK:
				{
					statementType="Block";
					addProperties(statement, sourceContent, map, statementType, methodName);
					nodeId = createNode(inserter, map);
					
					Block block = (Block)statement;
					List<Statement> statements = block.statements();
					for(Statement element : statements)
					{
						JavaStatementInfo.createRelationship(inserter, element, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
					}
				}break;
				
				case ASTNode.BREAK_STATEMENT:
				{
					statementType="BreakStatement";
					addProperties(statement, sourceContent, map, statementType, methodName);
					BreakStatement breakStatement = (BreakStatement)statement;
					SimpleName identifier = breakStatement.getLabel();
					if(identifier!=null)
						map.put(JavaExtractor.LABEL,identifier.getIdentifier());
					else
						map.put(JavaExtractor.LABEL,"null");
					nodeId = createNode(inserter, map);
		        }break;
		        
				case ASTNode.CONSTRUCTOR_INVOCATION:
				{
					statementType="ConstructorInvocation";
					addProperties(statement, sourceContent, map, statementType, methodName);
					ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
					List<ITypeBinding> bindedTypeList = JavaExpressionInfo.addTypeArgProperties(map,constructorInvocation.typeArguments());
					nodeId = createNode(inserter, map);
					
					List<Expression> constructorArgs = constructorInvocation.arguments();
					for(Expression element : constructorArgs)
					{
			    		JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.HAVE_ARG, sourceContent, methodName, javaProjectInfo);
					}
					
					for(ITypeBinding element : bindedTypeList)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element, JavaExtractor.TYPE_ARG_TYPE_BINDING);
					IMethodBinding constructorBinding = constructorInvocation.resolveConstructorBinding();
					if(constructorBinding != null)
						JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, constructorBinding);
		        }break;
		        
				case ASTNode.CONTINUE_STATEMENT:
				{
		        	statementType="ContinueStatement";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	ContinueStatement continueStatement = (ContinueStatement)statement;
		    		SimpleName identifier = continueStatement.getLabel();
		    		if(identifier!=null)
		    			map.put(JavaExtractor.LABEL,identifier.getIdentifier());
		    		else
		    			map.put(JavaExtractor.LABEL,"null");
		    		nodeId = createNode(inserter, map);
		        }break;
		        
				case ASTNode.DO_STATEMENT:
				{
		        	statementType="DoStatement";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	nodeId = createNode(inserter, map);
		        	
		        	DoStatement doStatement = (DoStatement)statement;
		    		Statement doBody = doStatement.getBody();
		    		Expression loopCondition = doStatement.getExpression();
		    		JavaExpressionInfo.createRelationship(inserter, loopCondition, nodeId, JavaExtractor.LOOP_CONDITION, sourceContent, methodName, javaProjectInfo);
		    		JavaStatementInfo.createRelationship(inserter, doBody, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
		        }break;
		        
				case ASTNode.EMPTY_STATEMENT:
				{
		        	statementType = "EmptyStatement";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	nodeId = createNode(inserter, map);
		        }break;
		        
				case ASTNode.ENHANCED_FOR_STATEMENT:
				{
		        	statementType="EnhancedForStatement";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	nodeId = createNode(inserter, map);
		        	
		        	EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
		    		Expression loopCondition = enhancedForStatement.getExpression();
		    		Statement forBody = enhancedForStatement.getBody();
		    		JavaExpressionInfo.createRelationship(inserter, loopCondition, nodeId, JavaExtractor.LOOP_CONDITION, sourceContent, methodName, javaProjectInfo);
		    		JavaStatementInfo.createRelationship(inserter, forBody, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
		        }break;
		        
				case ASTNode.EXPRESSION_STATEMENT:
				{
		        	statementType="ExpressionStatement";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	nodeId = createNode(inserter, map);
		        	
		        	ExpressionStatement expressionStatement = (ExpressionStatement)statement;
		    		Expression expression = expressionStatement.getExpression();
		    		JavaExpressionInfo.createRelationship(inserter, expression, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
		        }break;
		        
				case ASTNode.FOR_STATEMENT:
				{
		        	statementType="ForStatement";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	nodeId = createNode(inserter, map);
		    		
		    		ForStatement forStatement = (ForStatement)statement;
		    		List<Expression> initializers = forStatement.initializers();
		    		Expression loopCondition = forStatement.getExpression();
		    		Statement forBody = forStatement.getBody();
		    		List<Expression> updaters = forStatement.updaters();
		    		for(Expression element : initializers)
		    		{
			    		JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.INITIALIZER, sourceContent, methodName, javaProjectInfo);
		    		}
		    		JavaExpressionInfo.createRelationship(inserter, loopCondition, nodeId, JavaExtractor.LOOP_CONDITION, sourceContent, methodName, javaProjectInfo);
		    		for(Expression element : updaters)
		    		{
			    		JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.UPDATER, sourceContent, methodName, javaProjectInfo);
		    		}
		    		JavaStatementInfo.createRelationship(inserter, forBody, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
				}break;
				
				case ASTNode.IF_STATEMENT:
				{
					
		        	statementType="IfStatement";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	map.put(JavaExtractor.IF_CONDITION_NO,conditionNo);
		        	nodeId = createNode(inserter, map);
		    		
		    		IfStatement ifStatement = (IfStatement)statement;
		    		Statement thenStatement = ifStatement.getThenStatement();
		    		Statement elseStatement = ifStatement.getElseStatement();
		    		Expression conditionalExpression = ifStatement.getExpression();
		    		JavaExpressionInfo.createRelationship(inserter, conditionalExpression, nodeId, JavaExtractor.ENTER_CONDITION, sourceContent, methodName, javaProjectInfo);
		    		JavaStatementInfo.createRelationship(inserter, elseStatement, nodeId, JavaExtractor.ELSE, sourceContent, methodName, javaProjectInfo);
		    		JavaStatementInfo.createRelationship(inserter, thenStatement, nodeId, JavaExtractor.THEN, sourceContent, methodName, javaProjectInfo);
				}break;
				
				case ASTNode.LABELED_STATEMENT:
				{
		        	statementType = "LabeledStatement";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	LabeledStatement labeledStatement = (LabeledStatement)statement;
		        	Statement labeledBody = labeledStatement.getBody();
		        	map.put(JavaExtractor.LABEL, labeledStatement.getLabel().getIdentifier());
					nodeId = createNode(inserter, map);
					
		    		JavaStatementInfo.createRelationship(inserter, labeledBody, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
		        }break;
		        
				case ASTNode.RETURN_STATEMENT:
				{
		        	statementType="ReturnStatement";
					addProperties(statement, sourceContent, map, statementType, methodName);
					nodeId = createNode(inserter, map);
					
					ReturnStatement returnStatement = (ReturnStatement)statement;
					Expression returnExpression = returnStatement.getExpression();
		    		JavaExpressionInfo.createRelationship(inserter, returnExpression, nodeId, JavaExtractor.RETURN, sourceContent, methodName, javaProjectInfo);
		        }break;
		        
				case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
				{
		        	statementType = "SuperConstructorInvocation";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	nodeId = createNode(inserter, map);
		        	SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;

		        	List<ITypeBinding> bindedTypeList = JavaExpressionInfo.addTypeArgProperties(map,superConstructorInvocation.typeArguments());
		        	List<Expression> constructorArgs = superConstructorInvocation.arguments();
					for(Expression element : constructorArgs)
					{
			    		JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.HAVE_ARG, sourceContent, methodName, javaProjectInfo);
					}
					Expression expression = superConstructorInvocation.getExpression();
		    		JavaExpressionInfo.createRelationship(inserter, expression, nodeId, JavaExtractor.INVOCATED_BY, sourceContent, methodName, javaProjectInfo);

		    		for(ITypeBinding element : bindedTypeList)
						JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element, JavaExtractor.TYPE_ARG_TYPE_BINDING);
		    		IMethodBinding constructorBinding = superConstructorInvocation.resolveConstructorBinding();
					if(constructorBinding != null)
						JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, constructorBinding);
		        }break;
		        
				case ASTNode.SWITCH_CASE:
				{
		        	statementType="SwitchCase";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	SwitchCase switchCase = (SwitchCase)statement;
		        	Boolean isDefault = switchCase.isDefault();
					map.put(JavaExtractor.IS_DEFAULT, isDefault);
					nodeId = createNode(inserter, map);
					
					Expression caseExpression = switchCase.getExpression();
		    		JavaExpressionInfo.createRelationship(inserter, caseExpression, nodeId, JavaExtractor.ENTER_CONDITION, sourceContent, methodName, javaProjectInfo);
		        }break;
		        
				case ASTNode.SWITCH_STATEMENT:
				{
		        	statementType="SwitchStatement";
					addProperties(statement, sourceContent, map, statementType, methodName);
					nodeId = createNode(inserter, map);
					
					SwitchStatement switchStatement = (SwitchStatement)statement;
					Expression enterCondition = switchStatement.getExpression();
		    		JavaExpressionInfo.createRelationship(inserter, enterCondition, nodeId, JavaExtractor.SWITCH, sourceContent, methodName, javaProjectInfo);

					List<Statement> statements = switchStatement.statements();
					for(Statement element : statements)
					{
			    		JavaStatementInfo.createRelationship(inserter, element, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
					}
		        }break;
		        
				case ASTNode.SYNCHRONIZED_STATEMENT:
				{
		        	statementType = "SynchronizedStatement";
		        	addProperties(statement, sourceContent, map, statementType, methodName);
		        	nodeId = createNode(inserter, map);
		        	
		        	SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
		        	Statement synchronizedBody = synchronizedStatement.getBody();
		        	Expression synchronizedExpression = synchronizedStatement.getExpression();
		    		JavaStatementInfo.createRelationship(inserter, synchronizedBody, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
		    		JavaExpressionInfo.createRelationship(inserter, synchronizedExpression, nodeId, JavaExtractor.SYNCHRONIZED, sourceContent, methodName, javaProjectInfo);
		        }break;
		        
				case ASTNode.THROW_STATEMENT:
				{
		        	statementType="ThrowStatement";
					addProperties(statement, sourceContent, map, statementType, methodName);
					nodeId = createNode(inserter, map);
					
					ThrowStatement throwStatement = (ThrowStatement)statement;
					Expression throwExpression = throwStatement.getExpression();
		    		JavaExpressionInfo.createRelationship(inserter, throwExpression, nodeId, JavaExtractor.THROW, sourceContent, methodName, javaProjectInfo);
				}break;
				
				case ASTNode.TRY_STATEMENT:
				{
		        	statementType="TryStatement";
					addProperties(statement, sourceContent, map, statementType, methodName);
					nodeId = createNode(inserter, map);
					
					TryStatement tryStatement = (TryStatement)statement;
					Statement tryBody = tryStatement.getBody();
					Statement tryFinally = tryStatement.getFinally();
					List<Expression> resources = tryStatement.resources();
					List<CatchClause> catchClauses = tryStatement.catchClauses();
		    		JavaStatementInfo.createRelationship(inserter, tryBody, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
		    		JavaStatementInfo.createRelationship(inserter, tryFinally, nodeId, JavaExtractor.FINALLY, sourceContent, methodName, javaProjectInfo);

					for(CatchClause element : catchClauses)
					{
						long catchId = JavaStatementInfo.createCatchClauseNode(inserter, javaProjectInfo, methodName, sourceContent, element);
						inserter.createRelationship(nodeId, catchId, JavaExtractor.CATCH, new HashMap<>());
					}
					for(Expression element : resources)
					{
			    		JavaExpressionInfo.createRelationship(inserter, element, nodeId, JavaExtractor.TRY_RESOURCE, sourceContent, methodName, javaProjectInfo);
					}
		        }break;
		        
				case ASTNode.VARIABLE_DECLARATION_STATEMENT:
				{
		        	statementType="VariableDeclarationStatement";
					addProperties(statement, sourceContent, map, statementType, methodName);
					VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
					map.put(JavaExtractor.VAR_TYPE_STR, variableDeclarationStatement.getType().toString());
					JavaExpressionInfo.addModifierProperty(map, variableDeclarationStatement.getModifiers());
					JavaExpressionInfo.addDeclaredTypeProperty(map, variableDeclarationStatement.getType());
					nodeId = createNode(inserter, map);
					
					List<VariableDeclarationFragment> fragments = variableDeclarationStatement.fragments();
					for(VariableDeclarationFragment element : fragments)
					{
						long id = JavaStatementInfo.createVariableDeclarationFragmentNode(inserter, javaProjectInfo, methodName, element, sourceContent);
						if(id!=-1)
							inserter.createRelationship(nodeId, id, JavaExtractor.VAR_DECLARATION_FRAG, new HashMap<>());
						else;
					}
	            }break;
	            
				case ASTNode.WHILE_STATEMENT:
				{
		        	statementType="WhileStatement";
					addProperties(statement, sourceContent, map, statementType, methodName);
					nodeId = createNode(inserter, map);
					
					WhileStatement whileStatement = (WhileStatement)statement;
					Expression loopCondition = whileStatement.getExpression();
					Statement whileBody = whileStatement.getBody();
		    		JavaExpressionInfo.createRelationship(inserter, loopCondition, nodeId, JavaExtractor.LOOP_CONDITION, sourceContent, methodName, javaProjectInfo);
		    		JavaStatementInfo.createRelationship(inserter, whileBody, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
				}break;
				
				default:
				{
					OtherStatementInfo info = new OtherStatementInfo(inserter, methodName, statement);
					nodeId = info.getNodeId();
				}
			}
			return nodeId;
		}
		else
			return -1;	
	}
	
	static void createRelationship(BatchInserter inserter, Statement statement, long originalNodeId,
			RelationshipType relationship, String sourceContent, String methodName, JavaProjectInfo javaProjectInfo) {
		long terminalNodeId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, sourceContent, statement);
		if(terminalNodeId != -1)
		{
			inserter.createRelationship(originalNodeId, terminalNodeId, relationship, new HashMap<>());
			if(statementType.equals("IfStatement"))
				conditionNo++;
		}
		else
			if(statementType.equals("IfStatement"))
				conditionNo = 0;
	}

	static long createVariableDeclarationFragmentNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String methodName,
			VariableDeclarationFragment variableDeclarationFragment, String sourceContent) {
		HashMap<String, Object> map = new  HashMap<>();
		map.put(JavaExtractor.METHOD_NAME, methodName);
		map.put(JavaExtractor.CONTENT, sourceContent.substring(variableDeclarationFragment.getStartPosition(),variableDeclarationFragment.getStartPosition()+variableDeclarationFragment.getLength()));
		map.put(JavaExtractor.ROW_NO, sourceContent.substring(0, variableDeclarationFragment.getStartPosition()).split("\n").length);
		Expression initializer = variableDeclarationFragment.getInitializer();
		map.put(JavaExtractor.EXTRA_DIMENSION_NUM, variableDeclarationFragment.getExtraDimensions());
		map.put(JavaExtractor.IDENTIFIER, variableDeclarationFragment.getName().getIdentifier());
		JavaExpressionInfo.createRelationship(inserter, initializer, nodeId, JavaExtractor.LOOP_CONDITION, sourceContent, methodName, javaProjectInfo);
		return nodeId;
	}

	private static long createCatchClauseNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String methodName, String sourceContent,
			CatchClause catchClause) {
		HashMap<String, Object> map = new  HashMap<>();
		map.put(JavaExtractor.METHOD_NAME, methodName);
		map.put(JavaExtractor.CONTENT, sourceContent.substring(catchClause.getStartPosition(),catchClause.getStartPosition()+catchClause.getLength()));
		map.put(JavaExtractor.ROW_NO, sourceContent.substring(0, catchClause.getStartPosition()).split("\n").length);
		long nodeId = inserter.createNode(map, JavaExtractor.CATCH_CLAUSE);
		Statement catchBody = catchClause.getBody();
		SingleVariableDeclaration exception = catchClause.getException();
		JavaStatementInfo.createRelationship(inserter, catchBody, nodeId, JavaExtractor.STATEMENT_BODY, sourceContent, methodName, javaProjectInfo);
		long exceptionId = JavaStatementInfo.createSingleVarDeclarationNode(inserter, javaProjectInfo, methodName, sourceContent, exception);
		if(exceptionId!=-1)
		{
			inserter.createRelationship(nodeId, exceptionId, JavaExtractor.EXCEPTION_CAUGHT, new HashMap<>());
		}else;
		return nodeId;
	}

	static long createSingleVarDeclarationNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String methodName, String sourceContent, SingleVariableDeclaration singleVarDec) {
		HashMap<String, Object> map = new HashMap<>();
		map.put(JavaExtractor.METHOD_NAME, methodName);
		map.put(JavaExtractor.CONTENT, sourceContent.substring(singleVarDec.getStartPosition(),singleVarDec.getStartPosition()+singleVarDec.getLength()));
		map.put(JavaExtractor.ROW_NO, sourceContent.substring(0, singleVarDec.getStartPosition()).split("\n").length);
		map.put(JavaExtractor.IDENTIFIER, singleVarDec.getName().getIdentifier());
		JavaExpressionInfo.addModifierProperty(map, singleVarDec.getModifiers());
		map.put(JavaExtractor.IS_VARIABLE_ARITY_METHOD_ARG, Boolean.toString(singleVarDec.isVarargs()));
		long nodeId = inserter.createNode(map, JavaExtractor.SINGLE_VARIABLE_DECLARATION);
		Expression initializer = singleVarDec.getInitializer();
		JavaExpressionInfo.createRelationship(inserter, initializer, nodeId, JavaExtractor.INITIALIZER, sourceContent, methodName, javaProjectInfo);

		@SuppressWarnings("unchecked")
		List<Dimension> list = singleVarDec.extraDimensions();
		if(list != null)
			for(Dimension element : list)
			{
				HashMap<String, Object> dmap = new HashMap<>();
				dmap.put(JavaExtractor.METHOD_NAME, methodName);
		    	dmap.put(JavaExtractor.CONTENT, sourceContent.substring(element.getStartPosition(),element.getStartPosition()+element.getLength()));
				dmap.put(JavaExtractor.ROW_NO, sourceContent.substring(0, element.getStartPosition()).split("\n").length);
				long dimensionId = inserter.createNode(dmap, JavaExtractor.DIMENSION);
				inserter.createRelationship(nodeId, dimensionId, JavaExtractor.EXTRA_DIMENSION, new HashMap<>());
				@SuppressWarnings("unchecked")
				List<Annotation> annoList = element.annotations();
				for(Annotation anno : annoList) 
				{
					long annoId = JavaExpressionInfo.createJavaExpressionNode(inserter, anno, sourceContent, methodName, javaProjectInfo);
					if(annoId!=-1)
						inserter.createRelationship(nodeId, annoId, JavaExtractor.HAVE_ANNOTATION, new HashMap<>());
					else;
				}
			}
		else;
		return nodeId;	
	}

	private static long createNode(BatchInserter inserter, HashMap<String, Object> map) {
		long id = inserter.createNode(map, JavaExtractor.STATEMENT);
        return id;
    }
	
	private static void addProperties(ASTNode statement, String codeContent, HashMap<String, Object> map, String statementType, String belongTo) {
        map.put(JavaExtractor.STATEMENT_TYPE, statementType);
        map.put(JavaExtractor.METHOD_NAME, belongTo);
		map.put(JavaExtractor.CONTENT, codeContent.substring(statement.getStartPosition(),statement.getStartPosition()+statement.getLength()));
		map.put(JavaExtractor.ROW_NO, codeContent.substring(0, statement.getStartPosition()).split("\n").length);
    }
}
