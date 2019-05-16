package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class ForStatementInfo extends JavaStatementInfo{
	@Getter
	private long nodeId;
	private HashMap<String, Object> map;

	public ForStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.setStatementType("ForStatement");
		map = new HashMap<String, Object>();
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
	
	private long createNode(BatchInserter inserter) {
		super.addProperties(map);
        return inserter.createNode(map, JavaExtractor.STATEMENT);
    }

}
