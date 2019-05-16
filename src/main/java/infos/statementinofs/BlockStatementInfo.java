package main.java.infos.statementinofs;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public class BlockStatementInfo extends JavaStatementInfo{
	
	public BlockStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement)
	{
		super();
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.statementType="Block";
		super.addProperties();
		nodeId = createNode(inserter);
		
		Block block = (Block)statement;
		@SuppressWarnings("unchecked")
		List<Statement> statements = block.statements();
		for(int i = 0; i<statements.size();i++)
		{
			long id = JavaStatementInfo.createJavaStatementInfo(inserter, belongTo, i, statements.get(i));
			if(id!=-1)
				inserter.createRelationship(nodeId, id, JavaExtractor.STATEMENT_BODY, new HashMap<>());
			else;
		}
	}
	
	

}
