package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class ReturnStatementInfo extends JavaStatementInfo{
	
	public ReturnStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) 
	{
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="ReturnStatement";
		super.addProperties();
		nodeId = createNode(inserter);
		
		ReturnStatement returnStatement = (ReturnStatement)statement;
		Expression returnExpression = returnStatement.getExpression();
		
		long returnId = JavaExpressionInfo.createJavaExpressionInfo(inserter, returnExpression);
		if(returnId!=-1)
		{
			inserter.createRelationship(nodeId, returnId, JavaExtractor.RETURN, new HashMap<>());
		}
		else;
	}

}
