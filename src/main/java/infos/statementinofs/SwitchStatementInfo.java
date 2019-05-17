package main.java.infos.statementinofs;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class SwitchStatementInfo extends JavaStatementInfo{
	
	public SwitchStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="SwitchStatement";
		super.addProperties();
		nodeId = createNode(inserter);
		
		SwitchStatement switchStatement = (SwitchStatement)statement;
		Expression enterCondition = switchStatement.getExpression();
		long conditionId = JavaExpressionInfo.createJavaExpressionInfo(inserter, enterCondition);
		if(conditionId!=-1)
		{
			inserter.createRelationship(nodeId, conditionId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
		}
		@SuppressWarnings("unchecked")
		List<Statement> statements=switchStatement.statements();
		for(int i = 0; i<statements.size();i++)
		{
			long id = JavaStatementInfo.createJavaStatementInfo(inserter, belongTo, i, statements.get(i));
			if(id!=-1)
				inserter.createRelationship(nodeId, id, JavaExtractor.STATEMENT_BODY, new HashMap<>());
			else;
		}
	}
}
