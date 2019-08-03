package main.java.javaextractor.infos;

import com.google.common.base.Preconditions;
import lombok.Getter;
import main.java.javaextractor.JavaExtractor;

import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;

public class OtherStatementInfo {
	
    protected String statementType;
	protected String belongTo;
	@Getter
    protected long nodeId;
	
	protected HashMap<String, Object> map;
	
	public OtherStatementInfo() {
		map = new HashMap<String, Object>();
	}
		
	public OtherStatementInfo(BatchInserter inserter, String belongTo, Statement statement)
	{
		Preconditions.checkArgument(belongTo != null);
        this.belongTo = belongTo;
        map = new HashMap<String, Object>();
        createJavaStatementNode(inserter, statement);
	}
	
	private void createJavaStatementNode(BatchInserter inserter, Statement statement)
	{
        this.statementType = ""+statement.getNodeType();
        System.out.println(statement.getNodeType());
	}
	
	protected long createNode(BatchInserter inserter) {
		long id = inserter.createNode(map, JavaExtractor.STATEMENT);
        return id;
    }
	
	protected void addProperties() {
        map.put(JavaExtractor.STATEMENT_TYPE, statementType);
        map.put(JavaExtractor.METHOD_NAME, belongTo);
    }
	
}

