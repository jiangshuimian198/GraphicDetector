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
    protected long nodeId;
	
	protected HashMap<String, Object> map;
	
	public JavaStatementInfo() {
		map = new HashMap<String, Object>();
	}
		
	public JavaStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement)
	{
		Preconditions.checkArgument(belongTo != null);
        this.belongTo = belongTo;
        this.statementNo = statementNo;
        map = new HashMap<String, Object>();
        createJavaStatementNode(inserter, statement);
	}
	
	protected void createJavaStatementNode(BatchInserter inserter, Statement statement)
	{
		if(statement.getNodeType() == ASTNode.ASSERT_STATEMENT)
        {
        	this.statementType = "AssertStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT)
        {
        	this.statementType = "EnhancedForStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
        {
        	this.statementType = "ExpressionStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.RETURN_STATEMENT)
        {
        	this.statementType = "ReturnStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.SWITCH_STATEMENT)
        {
        	this.statementType = "SwitchStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.THROW_STATEMENT)
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
        else if(statement.getNodeType() == ASTNode.CONTINUE_STATEMENT)
        {
        	this.statementType = "ContinueStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else if(statement.getNodeType() == ASTNode.BREAK_STATEMENT)
        {
        	this.statementType = "BreakStatement";
        	addProperties();
        	nodeId = createNode(inserter);
        }
        else
    	{	
        	this.statementType = ""+statement.getNodeType();
        	System.out.println(statement.getNodeType());
        	//nodeId = createNode(inserter);
    	}
	}
	
	protected long createNode(BatchInserter inserter) {
		long id = inserter.createNode(map, JavaExtractor.STATEMENT);
        return id;
    }
	
	protected void addProperties() {
        map.put(JavaExtractor.STATEMENT_TYPE, statementType);
        map.put(JavaExtractor.BELONG_TO, belongTo);
        map.put(JavaExtractor.STATEMENT_NO, statementNo);
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
			else if(statement.getNodeType() == ASTNode.DO_STATEMENT)
	        {
				DoStatementInfo info = new DoStatementInfo(inserter, belongTo, statementNo, statement);
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

