package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class EnhancedForStatementInfo extends JavaStatementInfo{
	
	public EnhancedForStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="EnhancedForStatement";
		super.addProperties();
		nodeId = createNode(inserter);
		
		EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
		Expression loopCondition = enhancedForStatement.getExpression();
		Statement forBody = enhancedForStatement.getBody();
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
