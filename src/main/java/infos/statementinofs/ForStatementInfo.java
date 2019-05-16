package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class ForStatementInfo extends JavaStatementInfo{
	
	public ForStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		super();
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="ForStatement";
		super.addProperties();
		nodeId = createNode(inserter);
		
		ForStatement forStatement = (ForStatement)statement;
		Expression loopCondition = forStatement.getExpression();
		Statement forBody = forStatement.getBody();
		long loopId = JavaExpressionInfo.createJavaExpressionInfo(inserter, loopCondition);
		if(loopId!=-1)
		{
			inserter.createRelationship(nodeId, loopId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
		}
		else;
		long bodyId = JavaStatementInfo.createJavaStatementInfo(inserter, belongTo, statementNo, forBody);
		if(bodyId!=-1)
		{
			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}
		else;
	}

}
