package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class DoStatementInfo extends JavaStatementInfo{

	public DoStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		// TODO Auto-generated constructor stub
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="DoStatement";
		super.addProperties();
		nodeId = createNode(inserter);
		
		DoStatement doStatement = (DoStatement)statement;
		Statement doBody = doStatement.getBody();
		Expression loopCondition = doStatement.getExpression();
		
		long loopConditionId = JavaExpressionInfo.createJavaExpressionInfo(inserter, loopCondition);
		if(loopConditionId!=-1)
		{
			inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
		}else;
		long bodyId = JavaStatementInfo.createJavaStatementInfo(inserter, belongTo, statementNo, doBody);
		if(bodyId!=-1)
		{
			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}else;
	}

}
