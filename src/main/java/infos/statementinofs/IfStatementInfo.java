package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class IfStatementInfo extends JavaStatementInfo{
	@Getter
	private int conditionNo;

	private static int conditionNumber = 0;
	
	public IfStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="IfStatement";
		this.conditionNo = conditionNumber;
		super.addProperties();
		map.put(JavaExtractor.IF_CONDITION_NO,conditionNo);
		
		IfStatement ifStatement = (IfStatement)statement;
		Statement thenStatement = ifStatement.getThenStatement();
		Statement elseStatement = ifStatement.getElseStatement();
		Expression conditionalExpression = ifStatement.getExpression();
		nodeId = createNode(inserter);
		
		long conditionalExpressionId = JavaExpressionInfo.createJavaExpressionInfo(inserter, conditionalExpression);
		if(conditionalExpressionId!=-1)
		{
			inserter.createRelationship(nodeId, conditionalExpressionId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
			conditionNumber++;
		}
		else;
		long elseId = JavaStatementInfo.createJavaStatementInfo(inserter, belongTo, statementNo, elseStatement);
		if(elseId!=-1)
		{
			inserter.createRelationship(nodeId, elseId, JavaExtractor.ELSE, new HashMap<>());
		}
		else
			conditionNumber = 0;
		long thenId = JavaStatementInfo.createJavaStatementInfo(inserter, belongTo, statementNo, thenStatement);
		if(thenId!=-1)
		{
			inserter.createRelationship(nodeId, thenId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}
		else;
	}

}
