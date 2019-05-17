package main.java.infos;

import com.google.common.base.Preconditions;
import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;

public class TempStatementInfo {
	
    protected String statementType;
	protected String belongTo;
	protected int statementNo;
	@Getter
    protected long nodeId;
	
	protected HashMap<String, Object> map;
	
	public TempStatementInfo() {
		map = new HashMap<String, Object>();
	}
		
	public TempStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement)
	{
		Preconditions.checkArgument(belongTo != null);
        this.belongTo = belongTo;
        this.statementNo = statementNo;
        map = new HashMap<String, Object>();
        createJavaStatementNode(inserter, statement);
	}
	
	private void createJavaStatementNode(BatchInserter inserter, Statement statement)
	{
		if(statement.getNodeType() == ASTNode.THROW_STATEMENT)
        {
        	this.statementType = "ThrowStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.TRY_STATEMENT)
        {
        	this.statementType = "TryStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT)
        {
        	this.statementType = "VariableDeclarationStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
        {
        	this.statementType = "SuperConstructorInvocation";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION)
        {
        	this.statementType = "ConstructorInvocation";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.SYNCHRONIZED_STATEMENT)
        {
        	this.statementType = "SynchronizedStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.LABELED_STATEMENT)
        {
        	this.statementType = "LabeledStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.EXPRESSION_METHOD_REFERENCE)
        {
        	this.statementType = "EXPRESSION_METHOD_REFERENCE";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.EMPTY_STATEMENT)
        {
        	this.statementType = "EmptyStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else
    	{	
        	this.statementType = ""+statement.getNodeType();
        	System.out.println(statement.getNodeType());
    	}
	}
	
	protected long createNode(BatchInserter inserter) {
		long id = inserter.createNode(map, JavaExtractor.STATEMENT);
        return id;
    }
	
	protected void addProperties() {
        map.put(JavaExtractor.STATEMENT_TYPE, statementType);
        map.put(JavaExtractor.METHOD_NAME, belongTo);
        map.put(JavaExtractor.STATEMENT_NO, statementNo);
    }
	
}

