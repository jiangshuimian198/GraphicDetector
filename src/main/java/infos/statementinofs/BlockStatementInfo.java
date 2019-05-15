package main.java.infos.statementinofs;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;

public class BlockStatementInfo extends JavaStatementInfo{
	@Getter
    private long nodeId;
	private HashMap<String, Object> map;
	
	public BlockStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement)
	{
		super(inserter, belongTo, statementNo, statement);
		super.setStatementType("Block");
		map = new HashMap<String, Object>();
		nodeId = createNode(inserter);
		
		Block block = (Block)statement;
		@SuppressWarnings("unchecked")
		List<Statement> statements = block.statements();
		for(int i = 0; i<statements.size();i++)
		{
			long id = createBlockStatement(inserter, belongTo, i, statements.get(i));
			inserter.createRelationship(nodeId, id, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}
	}
	
	private long createBlockStatement(BatchInserter inserter, String belongTo, int statementNo,
			Statement blockStatement) {
		// TODO Auto-generated method stub
		return JavaStatement.createJavaStatementNode(inserter, belongTo, statementNo, blockStatement);
	}
	
	private long createNode(BatchInserter inserter) {
		super.addProperties(map);
        return inserter.createNode(map, JavaExtractor.STATEMENT);
    }

}
