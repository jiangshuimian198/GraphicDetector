package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class WhileStatementInfo extends JavaStatementInfo{
	@Getter
	private long nodeId;
	private HashMap<String, Object> map;

	public WhileStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		super(inserter, belongTo, statementNo, statement);
		// TODO Auto-generated constructor stub
		super.setStatementType("WhileStatement");
		map = new HashMap<String, Object>();
		WhileStatement whileStatement = (WhileStatement)statement;
		nodeId = createNode(inserter);
		Expression loopCondition = whileStatement.getExpression();
		Statement whileBody = whileStatement.getBody();
		long loopConditionId = createExpressionInfo(inserter, loopCondition);
		if(loopConditionId!=-1)
		{
			inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
		}
		long bodyId = createBodyStatement(inserter, belongTo, statementNo, whileBody);
		if(bodyId!=-1)
		{
			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}
	}
	
	private long createExpressionInfo(BatchInserter inserter, Expression conditionExpression) {
		// TODO Auto-generated method stub
		if(conditionExpression!=null)
		{
			JavaExpressionInfo info = new JavaExpressionInfo(inserter, conditionExpression);
			return info.getNodeId();
		}
		else
			return -1;
	}
	
	private long createBodyStatement(BatchInserter inserter, String belongTo, int statementNo,
			Statement whileBody) {
		// TODO Auto-generated method stub
		return JavaStatement.createJavaStatementNode(inserter, belongTo, statementNo, whileBody);
	}
	
	private long createNode(BatchInserter inserter) {
		super.addProperties(map);
        return inserter.createNode(map, JavaExtractor.STATEMENT);
    }
	
}
