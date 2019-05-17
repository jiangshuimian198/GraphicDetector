package main.java.infos;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public abstract class JavaStatementInfo {
	private static int conditionNo = 0;
	
	public static long createJavaStatementNode(BatchInserter inserter, String methodName, int statementNo, Statement statement)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		long nodeId;
		
		if(statement!=null) 
		{
			if(statement.getNodeType() == ASTNode.ASSERT_STATEMENT)
	        {
	        	String statementType="AssertStatement";
				addProperties(map, statementType, methodName, statementNo);
				nodeId = createNode(inserter, map);
				AssertStatement assertStatement = (AssertStatement)statement;
				Expression assertExpression = assertStatement.getExpression();
				long assertId = JavaExpressionInfo.createJavaExpressionNode(inserter, assertExpression);
	    		if(assertId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, assertId, JavaExtractor.ASSERT, new HashMap<>());
	    		}else;
				return nodeId;
	        }
			else if(statement.getNodeType()==ASTNode.BLOCK) 
			{
				String statementType="Block";
				addProperties(map, statementType, methodName, statementNo);
				nodeId = createNode(inserter, map);
				
				Block block = (Block)statement;
				@SuppressWarnings("unchecked")
				List<Statement> statements = block.statements();
				for(int i = 0; i<statements.size();i++)
				{
					long id = JavaStatementInfo.createJavaStatementNode(inserter, methodName, i, statements.get(i));
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.STATEMENT_BODY, new HashMap<>());
					else;
				}
				return nodeId;
			}
			else if(statement.getNodeType() == ASTNode.BREAK_STATEMENT)
	        {
				String statementType="BreakStatement";
				addProperties(map, statementType, methodName, statementNo);
				BreakStatement breakStatement = (BreakStatement)statement;
				SimpleName identifier = breakStatement.getLabel();
				if(identifier!=null)
					map.put(JavaExtractor.LABEL,identifier.getIdentifier());
				else
					map.put(JavaExtractor.LABEL,"null");
				nodeId = createNode(inserter, map);
				return nodeId;
	        }
			else if(statement.getNodeType() == ASTNode.CONTINUE_STATEMENT)
	        {
	        	String statementType="ContinueStatement";
	        	addProperties(map, statementType, methodName, statementNo);
	        	ContinueStatement continueStatement = (ContinueStatement)statement;
	    		SimpleName identifier = continueStatement.getLabel();
	    		if(identifier!=null)
	    			map.put(JavaExtractor.LABEL,identifier.getIdentifier());
	    		else
	    			map.put(JavaExtractor.LABEL,"null");
	    		nodeId = createNode(inserter, map);
	    		return nodeId;
	        }
	        else if(statement.getNodeType() == ASTNode.DO_STATEMENT)
	        {
	        	String statementType="DoStatement";
	        	addProperties(map, statementType, methodName, statementNo);
	        	nodeId = createNode(inserter, map);
	        	DoStatement doStatement = (DoStatement)statement;
	    		Statement doBody = doStatement.getBody();
	    		Expression loopCondition = doStatement.getExpression();
	    		
	    		long loopConditionId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition);
	    		if(loopConditionId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
	    		}else;
	    		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, statementNo, doBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}else;
	    		return nodeId;
	        }
	        else if(statement.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT)
	        {
	        	String statementType="EnhancedForStatement";
	        	addProperties(map, statementType, methodName, statementNo);
	        	nodeId = createNode(inserter, map);
	        	EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
	    		Expression loopCondition = enhancedForStatement.getExpression();
	    		Statement forBody = enhancedForStatement.getBody();
	    		long loopId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition);
	    		if(loopId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, loopId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
	    		}
	    		else;
	    		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, statementNo, forBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	    		return nodeId;
	        }
	        else if(statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
	        {
	        	String statementType="ExpressionStatement";
	        	addProperties(map, statementType, methodName, statementNo);
	        	nodeId = createNode(inserter, map);
	        	ExpressionStatement expressionStatement = (ExpressionStatement)statement;
	    		Expression expression = expressionStatement.getExpression();
	    		long expressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, expression);
	    		if(expressionId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, expressionId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	    		return nodeId;
	        }
	        else if(statement.getNodeType()==ASTNode.FOR_STATEMENT) 
			{
	        	String statementType="ForStatement";
	        	addProperties(map, statementType, methodName, statementNo);
	        	nodeId = createNode(inserter, map);
	    		
	    		ForStatement forStatement = (ForStatement)statement;
	    		@SuppressWarnings("unchecked")
				List<Expression> initializers = forStatement.initializers();
	    		Expression loopCondition = forStatement.getExpression();
	    		Statement forBody = forStatement.getBody();
	    		@SuppressWarnings("unchecked")
				List<Expression> updaters = forStatement.updaters();
	    		for(int i =0;i<initializers.size();i++)
	    		{
	    			long initId = JavaExpressionInfo.createJavaExpressionNode(inserter, initializers.get(i));
	    			if(initId!=-1)
	    			{
	    				inserter.createRelationship(nodeId, initId, JavaExtractor.INITIALIZER, new HashMap<>());
	    			}
	    			else;
	    		}
	    		long loopId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition);
	    		if(loopId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, loopId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
	    		}
	    		else;
	    		for(int i =0;i<updaters.size();i++)
	    		{
	    			long initId = JavaExpressionInfo.createJavaExpressionNode(inserter, updaters.get(i));
	    			if(initId!=-1)
	    			{
	    				inserter.createRelationship(nodeId, initId, JavaExtractor.UPDATERS, new HashMap<>());
	    			}
	    			else;
	    		}
	    		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, statementNo, forBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	    		return nodeId;
			}
	        else if(statement.getNodeType()==ASTNode.IF_STATEMENT) 
			{
	
	        	String statementType="IfStatement";
	        	addProperties(map, statementType, methodName, statementNo);
	        	map.put(JavaExtractor.IF_CONDITION_NO,conditionNo);
	    		
	    		IfStatement ifStatement = (IfStatement)statement;
	    		Statement thenStatement = ifStatement.getThenStatement();
	    		Statement elseStatement = ifStatement.getElseStatement();
	    		Expression conditionalExpression = ifStatement.getExpression();
	    		nodeId = createNode(inserter, map);
	    		
	    		long conditionalExpressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, conditionalExpression);
	    		if(conditionalExpressionId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, conditionalExpressionId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
	    			conditionNo++;
	    		}
	    		else;
	    		long elseId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, statementNo, elseStatement);
	    		if(elseId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, elseId, JavaExtractor.ELSE, new HashMap<>());
	    		}
	    		else
	    			conditionNo = 0;
	    		long thenId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, statementNo, thenStatement);
	    		if(thenId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, thenId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	    		return nodeId;
			}
	        else if(statement.getNodeType() == ASTNode.RETURN_STATEMENT)
	        {
	        	String statementType="ReturnStatement";
				addProperties(map, statementType, methodName, statementNo);
				nodeId = createNode(inserter, map);
				ReturnStatement returnStatement = (ReturnStatement)statement;
				Expression returnExpression = returnStatement.getExpression();
				long returnId = JavaExpressionInfo.createJavaExpressionNode(inserter, returnExpression);
				if(returnId!=-1)
				{
					inserter.createRelationship(nodeId, returnId, JavaExtractor.RETURN, new HashMap<>());
				}
				else;
				return nodeId;
	        }
	        else if(statement.getNodeType() == ASTNode.SWITCH_CASE)
	        {
	        	String statementType="SwitchCase";
	        	addProperties(map, statementType, methodName, statementNo);
	        	SwitchCase switchCase = (SwitchCase)statement;
	        	Boolean isDefault = switchCase.isDefault();
				map.put(JavaExtractor.IS_DEFAULT, isDefault);
				nodeId = createNode(inserter, map);
				Expression caseExpression = switchCase.getExpression();
				long caseId = JavaExpressionInfo.createJavaExpressionNode(inserter, caseExpression);
				if(caseId!=-1)
				{
					inserter.createRelationship(nodeId, caseId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
				}
				return nodeId;
	        }
	        else if(statement.getNodeType() == ASTNode.SWITCH_STATEMENT)
	        {
	        	String statementType="SwitchStatement";
				addProperties(map, statementType, methodName, statementNo);
				nodeId = createNode(inserter, map);
				SwitchStatement switchStatement = (SwitchStatement)statement;
				Expression enterCondition = switchStatement.getExpression();
				long conditionId = JavaExpressionInfo.createJavaExpressionNode(inserter, enterCondition);
				if(conditionId!=-1)
				{
					inserter.createRelationship(nodeId, conditionId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
				}
				@SuppressWarnings("unchecked")
				List<Statement> statements=switchStatement.statements();
				for(int i = 0; i<statements.size();i++)
				{
					long id = JavaStatementInfo.createJavaStatementNode(inserter, methodName, i, statements.get(i));
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.STATEMENT_BODY, new HashMap<>());
					else;
				}
				return nodeId;
	        }
	        else if(statement.getNodeType()==ASTNode.WHILE_STATEMENT) 
			{
	        	String statementType="WhileStatement";
				addProperties(map, statementType, methodName, statementNo);
				nodeId = createNode(inserter, map);
				WhileStatement whileStatement = (WhileStatement)statement;
				Expression loopCondition = whileStatement.getExpression();
				Statement whileBody = whileStatement.getBody();
				long loopConditionId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition);
				if(loopConditionId!=-1)
				{
					inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
				}else;
				long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, statementNo, whileBody);
				if(bodyId!=-1)
				{
					inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
				}else;
				return nodeId;
			}
	        else
			{
				TempStatementInfo info = new TempStatementInfo(inserter, methodName, statementNo, statement);
				return info.getNodeId();
			}
		}
		else
			return -1;	
	}
	
	private static long createNode(BatchInserter inserter, HashMap<String, Object> map) {
		long id = inserter.createNode(map, JavaExtractor.STATEMENT);
        return id;
    }
	
	private static void addProperties(HashMap<String, Object> map, String statementType, String belongTo, int statementNo) {
        map.put(JavaExtractor.STATEMENT_TYPE, statementType);
        map.put(JavaExtractor.METHOD_NAME, belongTo);
        map.put(JavaExtractor.STATEMENT_NO, statementNo);
    }
}
