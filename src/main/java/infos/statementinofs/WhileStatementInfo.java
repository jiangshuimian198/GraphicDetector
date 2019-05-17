package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class WhileStatementInfo extends JavaStatementInfo{

	public WhileStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="WhileStatement";
		super.addProperties();
		nodeId = createNode(inserter);
		
		WhileStatement whileStatement = (WhileStatement)statement;
		Expression loopCondition = whileStatement.getExpression();
		Statement whileBody = whileStatement.getBody();
		long loopConditionId = JavaExpressionInfo.createJavaExpressionInfo(inserter, loopCondition);
		if(loopConditionId!=-1)
		{
			inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
		}else;
		long bodyId = JavaStatementInfo.createJavaStatementInfo(inserter, belongTo, statementNo, whileBody);
		if(bodyId!=-1)
		{
			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}else;
	}
	
}
