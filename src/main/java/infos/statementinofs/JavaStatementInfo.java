package main.java.infos.statementinofs;

import com.google.common.base.Preconditions;
import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;

public class JavaStatementInfo {
	
	@Getter
    protected String statementType;
	@Getter
	protected String belongTo;
	@Getter
	protected int statementNo;
	@Getter
    private long nodeId;
	
	private HashMap<String, Object> map;
	
	public JavaStatementInfo() {}
		
	public JavaStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement)
	{
		Preconditions.checkArgument(belongTo != null);
        this.belongTo = belongTo;
        this.statementNo = statementNo;
        createJavaStatementNode(inserter, statement);
	}
	
	protected void createJavaStatementNode(BatchInserter inserter, Statement statement)
	{
		if(statement.getNodeType() == ASTNode.ASSERT_STATEMENT)
        {
        	this.statementType = "AssertStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.DO_STATEMENT)
        {
        	this.statementType = "DoStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT)
        {
        	this.statementType = "EnhancedForStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
        {
        	this.statementType = "ExpressionStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.RETURN_STATEMENT)
        {
        	this.statementType = "ReturnStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.SWITCH_STATEMENT)
        {
        	this.statementType = "SwitchStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.THROW_STATEMENT)
        {
        	this.statementType = "ThrowStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.TRY_STATEMENT)
        {
        	this.statementType = "TryStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT)
        {
        	this.statementType = "VariableDeclarationStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
        {
        	this.statementType = "SuperConstructorInvocation";
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
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.LABELED_STATEMENT)
        {
        	this.statementType = "LabeledStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.EXPRESSION_METHOD_REFERENCE)
        {
        	this.statementType = "EXPRESSION_METHOD_REFERENCE";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.EMPTY_STATEMENT)
        {
        	this.statementType = "EmptyStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.CONTINUE_STATEMENT)
        {
        	this.statementType = "ContinueStatement";
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.BREAK_STATEMENT)
        {
        	this.statementType = "BreakStatement";
        	nodeId = createNode(inserter);
        }
        else
    	{	
        	this.statementType = ""+statement.getNodeType();
        	System.out.println(statement.getNodeType());
        	//nodeId = createNode(inserter);
    	}
	}
	
	private long createNode(BatchInserter inserter) {
		map = new HashMap<String, Object>();
		addProperties(map);
		long id = inserter.createNode(map, JavaExtractor.STATEMENT);
        return id;
    }
	
	protected void addProperties(HashMap<String, Object> map) {
        map.put(JavaExtractor.STATEMENT_TYPE, statementType);
        map.put(JavaExtractor.BELONG_TO, belongTo);
        map.put(JavaExtractor.STATEMENT_NO, statementNo);
    }
	
	public void setStatementType(String s)
	{
		this.statementType = s;
	}
	
	public static long createJavaStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement)
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
			else if(statement.getNodeType()==ASTNode.FOR_STATEMENT) 
			{
				ForStatementInfo info = new ForStatementInfo(inserter, belongTo, statementNo, statement);
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

