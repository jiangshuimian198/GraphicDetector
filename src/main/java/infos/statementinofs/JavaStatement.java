package main.java.infos.statementinofs;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

public interface JavaStatement {
	public static long createJavaStatementNode(BatchInserter inserter, String belongTo, int statementNo, Statement statement)
	{
		if(statement!=null)
		{
			if(statement.getNodeType()==ASTNode.BLOCK) 
			{
				BlockStatementInfo info = new BlockStatementInfo(inserter, belongTo, statementNo, statement);
				return info.getNodeId();
			}
			else if(statement.getNodeType()==ASTNode.IF_STATEMENT) 
			{
				IfStatementInfo info = new IfStatementInfo(inserter, belongTo, statementNo, statement);
				return info.getNodeId();
			}
			else if(statement.getNodeType()==ASTNode.WHILE_STATEMENT) 
			{
				WhileStatementInfo info = new WhileStatementInfo(inserter, belongTo, statementNo, statement);
				return info.getNodeId();
			}
			else
			{
				JavaStatementInfo info = new JavaStatementInfo(inserter, belongTo, statementNo, statement);
				return info.getNodeId();
			}
		}
		else
			return -1;
		
	}
}
