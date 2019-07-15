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
import org.eclipse.jdt.core.dom.IBinding;
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
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public abstract class JavaStatementInfo {
	private static int conditionNo = 0;
	
	@SuppressWarnings("unchecked")
	public static long createJavaStatementNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String methodName, String codeContent, Statement statement)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		long nodeId;
		String statementType;
		
		if(statement!=null) 
		{
			if(statement.getNodeType() == ASTNode.ASSERT_STATEMENT)
	        {
	        	statementType="AssertStatement";
				addProperties(statement, codeContent, map, statementType, methodName);
				AssertStatement assertStatement = (AssertStatement)statement;
				nodeId = createNode(inserter, map);
				
				Expression assertExpression = assertStatement.getExpression();
				long assertId = JavaExpressionInfo.createJavaExpressionNode(inserter, assertExpression, codeContent, methodName, javaProjectInfo);
	    		if(assertId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, assertId, JavaExtractor.ASSERT, new HashMap<>());
	    		}else;
	        }
			else if(statement.getNodeType()==ASTNode.BLOCK) 
			{
				statementType="Block";
				addProperties(statement, codeContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				
				Block block = (Block)statement;
				List<Statement> statements = block.statements();
				for(int i = 0; i<statements.size();i++)
				{
					long id = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, statements.get(i));
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.STATEMENT_BODY, new HashMap<>());
					else;
				}
			}
			else if(statement.getNodeType() == ASTNode.BREAK_STATEMENT)
	        {
				statementType="BreakStatement";
				addProperties(statement, codeContent, map, statementType, methodName);
				BreakStatement breakStatement = (BreakStatement)statement;
				SimpleName identifier = breakStatement.getLabel();
				if(identifier!=null)
					map.put(JavaExtractor.LABEL,identifier.getIdentifier());
				else
					map.put(JavaExtractor.LABEL,"null");
				nodeId = createNode(inserter, map);
	        }
			else if(statement.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION)
	        {
				statementType="ConstructorInvocation";
				addProperties(statement, codeContent, map, statementType, methodName);
				ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
				//constructorInvocation.resolveConstructorBinding();
				JavaExpressionInfo.addTypeArgProperties(map,constructorInvocation.typeArguments());
				nodeId = createNode(inserter, map);
				
				List<Expression> constructorArgs = constructorInvocation.arguments();
				for(Expression element : constructorArgs)
				{
					long argId = JavaExpressionInfo.createJavaExpressionNode(inserter, element, codeContent, methodName, javaProjectInfo);
		    		if(argId!=-1)
		    		{
		    			inserter.createRelationship(nodeId, argId, JavaExtractor.HAVE_PARAM, new HashMap<>());
		    		}else;
				}
	        }
			else if(statement.getNodeType() == ASTNode.CONTINUE_STATEMENT)
	        {
	        	statementType="ContinueStatement";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	ContinueStatement continueStatement = (ContinueStatement)statement;
	    		SimpleName identifier = continueStatement.getLabel();
	    		if(identifier!=null)
	    			map.put(JavaExtractor.LABEL,identifier.getIdentifier());
	    		else
	    			map.put(JavaExtractor.LABEL,"null");
	    		nodeId = createNode(inserter, map);
	        }
	        else if(statement.getNodeType() == ASTNode.DO_STATEMENT)
	        {
	        	statementType="DoStatement";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        	
	        	DoStatement doStatement = (DoStatement)statement;
	    		Statement doBody = doStatement.getBody();
	    		Expression loopCondition = doStatement.getExpression();
	    		long loopConditionId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition, codeContent, methodName, javaProjectInfo);
	    		if(loopConditionId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
	    		}else;
	    		
	    		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, doBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}else;
	        }
	        else if(statement.getNodeType() == ASTNode.EMPTY_STATEMENT)
	        {
	        	statementType = "EmptyStatement";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        }
	        else if(statement.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT)
	        {
	        	statementType="EnhancedForStatement";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        	
	        	EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
	    		Expression loopCondition = enhancedForStatement.getExpression();
	    		Statement forBody = enhancedForStatement.getBody();
	    		long loopId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition, codeContent, methodName, javaProjectInfo);
	    		if(loopId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, loopId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
	    		}
	    		else;
	    		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, forBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	        }
	        else if(statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
	        {
	        	statementType="ExpressionStatement";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        	
	        	ExpressionStatement expressionStatement = (ExpressionStatement)statement;
	    		Expression expression = expressionStatement.getExpression();
	    		long expressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, expression, codeContent, methodName, javaProjectInfo);
	    		if(expressionId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, expressionId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	        }
	        else if(statement.getNodeType()==ASTNode.FOR_STATEMENT) 
			{
	        	statementType="ForStatement";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	    		
	    		ForStatement forStatement = (ForStatement)statement;
	    		List<Expression> initializers = forStatement.initializers();
	    		Expression loopCondition = forStatement.getExpression();
	    		Statement forBody = forStatement.getBody();
	    		List<Expression> updaters = forStatement.updaters();
	    		for(Expression element : initializers)
	    		{
	    			long initId = JavaExpressionInfo.createJavaExpressionNode(inserter, element, codeContent, methodName, javaProjectInfo);
	    			if(initId!=-1)
	    			{
	    				inserter.createRelationship(nodeId, initId, JavaExtractor.INITIALIZER, new HashMap<>());
	    			}
	    			else;
	    		}
	    		long loopId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition, codeContent, methodName, javaProjectInfo);
	    		if(loopId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, loopId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
	    		}
	    		else;
	    		for(Expression element : updaters)
	    		{
	    			long initId = JavaExpressionInfo.createJavaExpressionNode(inserter, element, codeContent, methodName, javaProjectInfo);
	    			if(initId!=-1)
	    			{
	    				inserter.createRelationship(nodeId, initId, JavaExtractor.UPDATER, new HashMap<>());
	    			}
	    			else;
	    		}
	    		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, forBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
			}
	        else if(statement.getNodeType()==ASTNode.IF_STATEMENT) 
			{
	
	        	statementType="IfStatement";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	map.put(JavaExtractor.IF_CONDITION_NO,conditionNo);
	        	nodeId = createNode(inserter, map);
	    		
	    		IfStatement ifStatement = (IfStatement)statement;
	    		Statement thenStatement = ifStatement.getThenStatement();
	    		Statement elseStatement = ifStatement.getElseStatement();
	    		Expression conditionalExpression = ifStatement.getExpression();
	    		long conditionalExpressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, conditionalExpression, codeContent, methodName, javaProjectInfo);
	    		if(conditionalExpressionId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, conditionalExpressionId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
	    			conditionNo++;
	    		}
	    		else;
	    		long elseId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, elseStatement);
	    		if(elseId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, elseId, JavaExtractor.ELSE, new HashMap<>());
	    		}
	    		else
	    			conditionNo = 0;
	    		long thenId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, thenStatement);
	    		if(thenId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, thenId, JavaExtractor.THEN, new HashMap<>());
	    		}
	    		else;
			}
	        else if(statement.getNodeType() == ASTNode.LABELED_STATEMENT)
	        {
	        	statementType = "LabeledStatement";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	LabeledStatement labeledStatement = (LabeledStatement)statement;
	        	Statement labeledBody = labeledStatement.getBody();
	        	map.put(JavaExtractor.LABEL, labeledStatement.getLabel().getIdentifier());
				nodeId = createNode(inserter, map);
				
				long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, labeledBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	        }
	        else if(statement.getNodeType() == ASTNode.RETURN_STATEMENT)
	        {
	        	statementType="ReturnStatement";
				addProperties(statement, codeContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				
				ReturnStatement returnStatement = (ReturnStatement)statement;
				Expression returnExpression = returnStatement.getExpression();
				long returnId = JavaExpressionInfo.createJavaExpressionNode(inserter, returnExpression, codeContent, methodName, javaProjectInfo);
				if(returnId!=-1)
				{
					inserter.createRelationship(nodeId, returnId, JavaExtractor.RETURN, new HashMap<>());
				}
				else;
	        }
	        else if(statement.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
	        {
	        	statementType = "SuperConstructorInvocation";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        	SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;

				JavaExpressionInfo.addTypeArgProperties(map,superConstructorInvocation.typeArguments());
	        	List<Expression> constructorArgs = superConstructorInvocation.arguments();
				for(Expression element : constructorArgs)
				{
					long argId = JavaExpressionInfo.createJavaExpressionNode(inserter, element, codeContent, methodName, javaProjectInfo);
		    		if(argId!=-1)
		    		{
		    			inserter.createRelationship(nodeId, argId, JavaExtractor.HAVE_PARAM, new HashMap<>());
		    		}else;
				}
				Expression expression = superConstructorInvocation.getExpression();
				long expressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, expression, codeContent, methodName, javaProjectInfo);
	    		if(expressionId!=-1)
	    		{
	    			inserter.createRelationship(expressionId, nodeId, JavaExtractor.INVOCATION, new HashMap<>());
	    		}else;
				//superConstructorInvocation.resolveConstructorBinding();
	        }
	        else if(statement.getNodeType() == ASTNode.SWITCH_CASE)
	        {
	        	statementType="SwitchCase";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	SwitchCase switchCase = (SwitchCase)statement;
	        	Boolean isDefault = switchCase.isDefault();
				map.put(JavaExtractor.IS_DEFAULT, isDefault);
				nodeId = createNode(inserter, map);
				
				Expression caseExpression = switchCase.getExpression();
				long caseId = JavaExpressionInfo.createJavaExpressionNode(inserter, caseExpression, codeContent, methodName, javaProjectInfo);
				if(caseId!=-1)
				{
					inserter.createRelationship(nodeId, caseId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
				}
	        }
	        else if(statement.getNodeType() == ASTNode.SWITCH_STATEMENT)
	        {
	        	statementType="SwitchStatement";
				addProperties(statement, codeContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				
				SwitchStatement switchStatement = (SwitchStatement)statement;
				Expression enterCondition = switchStatement.getExpression();
				long conditionId = JavaExpressionInfo.createJavaExpressionNode(inserter, enterCondition, codeContent, methodName, javaProjectInfo);
				if(conditionId!=-1)
				{
					inserter.createRelationship(nodeId, conditionId, JavaExtractor.SWITCH, new HashMap<>());
				}
				List<Statement> statements=switchStatement.statements();
				for(int i = 0; i<statements.size();i++)
				{
					long id = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, statements.get(i));
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.STATEMENT_BODY, new HashMap<>());
					else;
				}
	        }
	        else if(statement.getNodeType() == ASTNode.SYNCHRONIZED_STATEMENT)
	        {
	        	statementType = "SynchronizedStatement";
	        	addProperties(statement, codeContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        	
	        	SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
	        	Statement synchronizedBody = synchronizedStatement.getBody();
	        	Expression synchronizedExpression = synchronizedStatement.getExpression();
	        	long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, synchronizedBody);
				if(bodyId!=-1)
					inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
				else;
				long syncExpressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, synchronizedExpression, codeContent, methodName, javaProjectInfo);
				if(syncExpressionId!=-1)
				{
					inserter.createRelationship(nodeId, syncExpressionId, JavaExtractor.SYNCHRONIZED, new HashMap<>());
				}else;
	        }
	        else if(statement.getNodeType() == ASTNode.THROW_STATEMENT)
	        {
	        	statementType="ThrowStatement";
				addProperties(statement, codeContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				
				ThrowStatement throwStatement = (ThrowStatement)statement;
				Expression throwExpression = throwStatement.getExpression();
				long throwId = JavaExpressionInfo.createJavaExpressionNode(inserter, throwExpression, codeContent, methodName, javaProjectInfo);
				if(throwId!=-1)
				{
					inserter.createRelationship(nodeId, throwId, JavaExtractor.THROW, new HashMap<>());
				}else;
			}
	        else if(statement.getNodeType() == ASTNode.TRY_STATEMENT)
	        {
	        	statementType="TryStatement";
				addProperties(statement, codeContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				
				TryStatement tryStatement = (TryStatement)statement;
				Statement tryBody = tryStatement.getBody();
				Statement tryFinally = tryStatement.getFinally();
				List<Expression> resources = tryStatement.resources();
				List<CatchClause> catchClauses = tryStatement.catchClauses();
				long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, tryBody);
				if(bodyId!=-1)
				{
					inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
				}else;
				long finallyId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, tryFinally);
				if(finallyId!=-1)
				{
					inserter.createRelationship(nodeId, finallyId, JavaExtractor.FINALLY, new HashMap<>());
				}else;
				for(CatchClause element : catchClauses)
				{
					long catchId = JavaStatementInfo.createCatchClauseNode(inserter, javaProjectInfo, methodName, codeContent, element);
					inserter.createRelationship(nodeId, catchId, JavaExtractor.CATCH, new HashMap<>());
				}
				for(Expression resource:resources)
				{
					long resourceId = JavaExpressionInfo.createJavaExpressionNode(inserter, resource, codeContent, methodName, javaProjectInfo);
					inserter.createRelationship(nodeId, resourceId, JavaExtractor.TRY_RESOURCE, new HashMap<>());
				}
	        }
	        else if(statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT)
            {
	        	statementType="VariableDeclarationStatement";
				addProperties(statement, codeContent, map, statementType, methodName);
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
				map.put(JavaExtractor.VAR_TYPE_STR, variableDeclarationStatement.getType().toString());
				JavaExpressionInfo.addModifierProperty(map, variableDeclarationStatement.getModifiers());
				JavaExpressionInfo.addDeclaredTypeProperty(map, variableDeclarationStatement.getType());
				nodeId = createNode(inserter, map);
				
				List<VariableDeclarationFragment> fragments = variableDeclarationStatement.fragments();
				for(VariableDeclarationFragment element : fragments)
				{
					long id = JavaStatementInfo.createVariableDeclarationFragmentNode(inserter, javaProjectInfo, methodName, element, codeContent);
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.VAR_DECLARATION_FRAG, new HashMap<>());
					else;
				}
            }
	        else if(statement.getNodeType()==ASTNode.WHILE_STATEMENT) 
			{
	        	statementType="WhileStatement";
				addProperties(statement, codeContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				
				WhileStatement whileStatement = (WhileStatement)statement;
				Expression loopCondition = whileStatement.getExpression();
				Statement whileBody = whileStatement.getBody();
				long loopConditionId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition, codeContent, methodName, javaProjectInfo);
				if(loopConditionId!=-1)
				{
					inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
				}else;
				long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, whileBody);
				if(bodyId!=-1)
				{
					inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
				}else;
			}
	        else
			{
				OtherStatementInfo info = new OtherStatementInfo(inserter, methodName, statement);
				nodeId = info.getNodeId();
			}
			return nodeId;
		}
		else
			return -1;	
	}
	
	static long createVariableDeclarationFragmentNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String methodName,
			VariableDeclarationFragment variableDeclarationFragment, String codeContent) {
		HashMap<String, Object> map = new  HashMap<>();
		map.put(JavaExtractor.METHOD_NAME, methodName);
		map.put(JavaExtractor.CONTENT, codeContent.substring(variableDeclarationFragment.getStartPosition(),variableDeclarationFragment.getStartPosition()+variableDeclarationFragment.getLength()));
		map.put(JavaExtractor.ROW_NO, codeContent.substring(0, variableDeclarationFragment.getStartPosition()).split("\n").length);
		Expression initializer = variableDeclarationFragment.getInitializer();
		map.put(JavaExtractor.EXTRA_DIMENSION_NUM, variableDeclarationFragment.getExtraDimensions());
		map.put(JavaExtractor.IDENTIFIER, variableDeclarationFragment.getName().getIdentifier());
		long nodeId = inserter.createNode(map, JavaExtractor.VARIABLE_DECLARATION_FRAGMENT);
		long initializerId = JavaExpressionInfo.createJavaExpressionNode(inserter, initializer, codeContent, methodName, javaProjectInfo);
		if(initializerId!=-1)
			inserter.createRelationship(nodeId, initializerId, JavaExtractor.INITIALIZER, new HashMap<>());
		else;
		return nodeId;
	}

	private static long createCatchClauseNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String methodName, String codeContent,
			CatchClause catchClause) {
		HashMap<String, Object> map = new  HashMap<>();
		map.put(JavaExtractor.METHOD_NAME, methodName);
		map.put(JavaExtractor.CONTENT, codeContent.substring(catchClause.getStartPosition(),catchClause.getStartPosition()+catchClause.getLength()));
		map.put(JavaExtractor.ROW_NO, codeContent.substring(0, catchClause.getStartPosition()).split("\n").length);
		long nodeId = inserter.createNode(map, JavaExtractor.CATCH_CLAUSE);
		Statement catchBody = catchClause.getBody();
		SingleVariableDeclaration exception = catchClause.getException();
		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, javaProjectInfo, methodName, codeContent, catchBody);
		if(bodyId!=-1)
		{
			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}else;
		long exceptionId = JavaStatementInfo.createSingleVarDeclarationNode(inserter, javaProjectInfo, methodName, codeContent, exception);
		if(exceptionId!=-1)
		{
			inserter.createRelationship(nodeId, exceptionId, JavaExtractor.EXCEPTION_CAUGHT, new HashMap<>());
		}else;
		return nodeId;
	}

	static long createSingleVarDeclarationNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String methodName, String codeContent, SingleVariableDeclaration singleVarDec) {
		HashMap<String, Object> map = new HashMap<>();
		map.put(JavaExtractor.METHOD_NAME, methodName);
		map.put(JavaExtractor.CONTENT, codeContent.substring(singleVarDec.getStartPosition(),singleVarDec.getStartPosition()+singleVarDec.getLength()));
		map.put(JavaExtractor.ROW_NO, codeContent.substring(0, singleVarDec.getStartPosition()).split("\n").length);
		map.put(JavaExtractor.IDENTIFIER, singleVarDec.getName().getIdentifier());
		JavaExpressionInfo.addModifierProperty(map, singleVarDec.getModifiers());
		map.put(JavaExtractor.IS_VARIABLE_ARITY_METHOD_ARG, Boolean.toString(singleVarDec.isVarargs()));
		long nodeId = inserter.createNode(map, JavaExtractor.SINGLE_VARIABLE_DECLARATION);
		Expression initializer = singleVarDec.getInitializer();
		long initId = JavaExpressionInfo.createJavaExpressionNode(inserter, initializer, codeContent, methodName, javaProjectInfo);
		if(initId != -1)
			inserter.createRelationship(nodeId, initId, JavaExtractor.INITIALIZER, new HashMap<>());
		else;
		@SuppressWarnings("unchecked")
		List<Dimension> list = singleVarDec.extraDimensions();
		if(list != null)
			for(Dimension element : list)
			{
				HashMap<String, Object> dmap = new HashMap<>();
				dmap.put(JavaExtractor.METHOD_NAME, methodName);
		    	dmap.put(JavaExtractor.CONTENT, codeContent.substring(element.getStartPosition(),element.getStartPosition()+element.getLength()));
				dmap.put(JavaExtractor.ROW_NO, codeContent.substring(0, element.getStartPosition()).split("\n").length);
				long dimensionId = inserter.createNode(dmap, JavaExtractor.DIMENSION);
				inserter.createRelationship(nodeId, dimensionId, JavaExtractor.EXTRA_DIMENSION, new HashMap<>());
				@SuppressWarnings("unchecked")
				List<Annotation> annoList = element.annotations();
				for(Annotation anno : annoList) 
				{
					long annoId = JavaExpressionInfo.createJavaExpressionNode(inserter, anno, codeContent, methodName, javaProjectInfo);
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
