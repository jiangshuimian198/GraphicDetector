package main.java.infos.statementinofs;

import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public class BreakStatementInfo extends JavaStatementInfo{

	public BreakStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) 
	{
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="BreakStatement";
		super.addProperties();
		
		
		BreakStatement breakStatement = (BreakStatement)statement;
		SimpleName identifier = breakStatement.getLabel();
		if(identifier!=null)
			map.put(JavaExtractor.LABEL,identifier.getIdentifier());
		else
			map.put(JavaExtractor.LABEL,"null");
		nodeId = createNode(inserter);
	}
}
