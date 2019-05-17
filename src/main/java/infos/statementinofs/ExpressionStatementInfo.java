package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class ExpressionStatementInfo extends JavaStatementInfo{
	
	public ExpressionStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="ExpressionStatement";
		super.addProperties();
		nodeId = createNode(inserter);
		ExpressionStatement expressionStatement = (ExpressionStatement)statement;
		Expression expression = expressionStatement.getExpression();
		long expressionId = JavaExpressionInfo.createJavaExpressionInfo(inserter, expression);
		if(expressionId!=-1)
		{
			inserter.createRelationship(nodeId, expressionId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}
		else;
	}
}
