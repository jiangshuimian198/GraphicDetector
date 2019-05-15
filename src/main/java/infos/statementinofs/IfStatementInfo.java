package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class IfStatementInfo extends JavaStatementInfo{
	@Getter
	private long nodeId;
	@Getter
	private int conditionNo;
	private HashMap<String, Object> map;
	private boolean isBlockElse = false;

	private static int conditionNumber = 0;
	
	public IfStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		
		// TODO Auto-generated constructor stub
		super(inserter, belongTo, statementNo, statement);
		super.setStatementType("IfStatement");
		map = new HashMap<String, Object>();
		this.conditionNo = conditionNumber;
		if(statement.getNodeType()==ASTNode.IF_STATEMENT)
		{
			IfStatement ifStatement = (IfStatement)statement;
			Statement thenStatement = ifStatement.getThenStatement();
			Statement elseStatement = ifStatement.getElseStatement();
			Expression conditionalExpression = ifStatement.getExpression();
			nodeId = createNode(inserter);
			long conditionalExpressionId = createExpressionInfo(inserter, conditionalExpression);
			if(conditionalExpressionId!=-1)
			{
				inserter.createRelationship(nodeId, conditionalExpressionId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
			}
			else;
			long elseId = createElseStatement(inserter, belongTo, statementNo, elseStatement);
			if(elseId!=-1)
			{
				inserter.createRelationship(nodeId, elseId, JavaExtractor.ELSE, new HashMap<>());
			}
			else;
			long thenId = createThenStatement(inserter, belongTo, statementNo, thenStatement);
			if(thenId!=-1)
			{
				inserter.createRelationship(nodeId, thenId, JavaExtractor.THEN, new HashMap<>());
			}
			else;
		}
		else
		{
			isBlockElse = true;
			nodeId = createNode(inserter);
		}
	}
	
	private long createThenStatement(BatchInserter inserter, String belongTo, int statementNo,
			Statement thenStatement) {
		// TODO Auto-generated method stub
		return JavaStatement.createJavaStatementNode(inserter, belongTo, statementNo, thenStatement);
	}

	private long createExpressionInfo(BatchInserter inserter, Expression conditionExpression) {
		// TODO Auto-generated method stub
		if(conditionExpression!=null)
		{
			conditionNumber++;
			JavaExpressionInfo info = new JavaExpressionInfo(inserter, conditionExpression);
			return info.getNodeId();
		}
		else
			return -1;
	}

	private long createElseStatement(BatchInserter inserter, String belongTo, int statementNo, Statement elseStatement) {
		// TODO Auto-generated method stub
		if(elseStatement!=null)
		{
			IfStatementInfo info = new IfStatementInfo(inserter, belongTo, statementNo, elseStatement);
			return info.getNodeId();
		}
		else {
			conditionNumber = 0;
			return -1;
		}
	}
	
	private long createNode(BatchInserter inserter) {
		super.addProperties(map);
		map.put(JavaExtractor.IF_CONDITION_NO,conditionNo);
		map.put(JavaExtractor.IS_BLOCK_ELSE,isBlockElse);
        return inserter.createNode(map, JavaExtractor.STATEMENT);
    }

}
