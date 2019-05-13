package main.java.infos;

import com.google.common.base.Preconditions;
import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;
import java.util.Map;

public class JavaStatementInfo {
	
	@Getter
    private String statementType;
	@Getter
    private String belongTo;
	@Getter
    private int statementNo;
	@Getter
    private long nodeId;
	
	public JavaStatementInfo(BatchInserter inserter, int type, String belongTo, int statementNo)
	{
        if(type == ASTNode.BLOCK)
        	this.statementType = "Block";
        else if(type == ASTNode.ASSERT_STATEMENT)
        	this.statementType = "AssertStatement";
        else if(type == ASTNode.DO_STATEMENT)
        	this.statementType = "DoStatement";
        else if(type == ASTNode.ENHANCED_FOR_STATEMENT)
        	this.statementType = "EnhancedForStatement";
        else if(type == ASTNode.EXPRESSION_STATEMENT)
        	this.statementType = "ExpressionStatement";
        else if(type == ASTNode.FOR_STATEMENT)
        	this.statementType = "ForStatement";
        else if(type == ASTNode.IF_STATEMENT)
        	this.statementType = "IfStatemnt";
        else if(type == ASTNode.RETURN_STATEMENT)
        	this.statementType = "ReturnStatement";
        else if(type == ASTNode.SWITCH_STATEMENT)
        	this.statementType = "SwitchStatement";
        else if(type == ASTNode.THROW_STATEMENT)
        	this.statementType = "ThrowStatement";
        else if(type == ASTNode.TRY_STATEMENT)
        	this.statementType = "TryStatement";
        else if(type == ASTNode.VARIABLE_DECLARATION_STATEMENT)
        	this.statementType = "VariableDeclarationStatement";
        else if(type == ASTNode.WHILE_STATEMENT)
        	this.statementType = "WhileStatement";
        else if(type == ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
        	this.statementType = "SuperConstructorInvocation";
        else if(type == ASTNode.CONSTRUCTOR_INVOCATION)
        	this.statementType = "ConstructorInvocation";
        else if(type == ASTNode.SYNCHRONIZED_STATEMENT)
        	this.statementType = "SynchronizedStatement";
        else if(type == ASTNode.LABELED_STATEMENT)
        	this.statementType = "LabeledStatement";
        else if(type == ASTNode.EXPRESSION_METHOD_REFERENCE)
        	this.statementType = "EXPRESSION_METHOD_REFERENCE";
        else if(type == ASTNode.EMPTY_STATEMENT)
        	this.statementType = "EmptyStatement";
        else
        	this.statementType = ""+type;
        Preconditions.checkArgument(belongTo != null);
        this.belongTo = belongTo;
        this.statementNo = statementNo; 
        nodeId = createNode(inserter);
	}
	
	private long createNode(BatchInserter inserter) {
        Map<String, Object> map = new HashMap<>();
        map.put(JavaExtractor.STATEMENT_TYPE, statementType);
        map.put(JavaExtractor.BELONG_TO, belongTo);
        map.put(JavaExtractor.STATEMENT_NO, statementNo);
        return inserter.createNode(map, JavaExtractor.STATEMENT);
    }
}

