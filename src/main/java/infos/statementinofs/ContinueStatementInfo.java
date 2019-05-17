package main.java.infos.statementinofs;

import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public class ContinueStatementInfo extends JavaStatementInfo{
	
	public ContinueStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) 
	{
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="ReturnStatement";
		super.addProperties();
		nodeId = createNode(inserter);
		
		ContinueStatement continueStatement = (ContinueStatement)statement;
		SimpleName identifier = continueStatement.getLabel();
		
		if(identifier!=null)
			map.put(JavaExtractor.LABEL,identifier.getIdentifier());
		else
			map.put(JavaExtractor.LABEL,"null");
		nodeId = createNode(inserter);
	}
}
