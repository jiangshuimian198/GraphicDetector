package main.java.infos;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.JCExtractor.JavaExtractor;

public abstract class JavaStatementInfo {
	private static int conditionNo = 0;
	
	public static long createJavaStatementNode(BatchInserter inserter, String methodName, String sourceContent, Statement statement)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		long nodeId;
		String statementType;
		
		if(statement!=null) 
		{
			if(statement.getNodeType() == ASTNode.ASSERT_STATEMENT)
	        {
	        	statementType="AssertStatement";
				addProperties(statement, sourceContent, map, statementType, methodName);
				AssertStatement assertStatement = (AssertStatement)statement;
				nodeId = createNode(inserter, map);
				Expression assertExpression = assertStatement.getExpression();
				long assertId = JavaExpressionInfo.createJavaExpressionNode(inserter, assertExpression, sourceContent, methodName);
	    		if(assertId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, assertId, JavaExtractor.ASSERT, new HashMap<>());
	    		}else;
	        }
			else if(statement.getNodeType()==ASTNode.BLOCK) 
			{
				statementType="Block";
				addProperties(statement, sourceContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				
				Block block = (Block)statement;
				@SuppressWarnings("unchecked")
				List<Statement> statements = block.statements();
				for(int i = 0; i<statements.size();i++)
				{
					long id = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, statements.get(i));
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.STATEMENT_BODY, new HashMap<>());
					else;
				}
			}
			else if(statement.getNodeType() == ASTNode.BREAK_STATEMENT)
	        {
				statementType="BreakStatement";
				addProperties(statement, sourceContent, map, statementType, methodName);
				BreakStatement breakStatement = (BreakStatement)statement;
				SimpleName identifier = breakStatement.getLabel();
				if(identifier!=null)
					map.put(JavaExtractor.LABEL,identifier.getIdentifier());
				else
					map.put(JavaExtractor.LABEL,"null");
				nodeId = createNode(inserter, map);
	        }
			else if(statement.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION)
	        {
				statementType="ConstructorInvocation";
				addProperties(statement, sourceContent, map, statementType, methodName);
				ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
				//constructorInvocation.resolveConstructorBinding();
				@SuppressWarnings("unchecked")
				List<Expression> constructorArgs = constructorInvocation.arguments();
				@SuppressWarnings("unchecked")
				List<Type> constructorTypeArgs =constructorInvocation.typeArguments();
				String[] typeArgsDeclaredTypes = new String[constructorTypeArgs.size()];
				String[] typeArgsTypes = new String[constructorTypeArgs.size()];
				for(int i = 0; i<constructorTypeArgs.size(); i++)
				{
					Type type = constructorTypeArgs.get(i);
					typeArgsTypes[i]=type.toString();
					//type.resolveBinding();
					boolean isAnnotatable = type.isAnnotatable();
					boolean isArrayType = type.isArrayType();
					boolean isIntersectionType = type.isIntersectionType();
					boolean isNameQualifiedType = type.isNameQualifiedType();
					boolean isParameterizedType = type.isParameterizedType();
					boolean isPrimitiveType = type.isPrimitiveType();
					boolean isQualifiedType = type.isQualifiedType();
					boolean isSimpleType = type.isSimpleType();
					boolean isUnionType = type.isUnionType();
					boolean isVar = type.isVar();
					boolean isWildcardType = type.isWildcardType();
					if(isAnnotatable)
						typeArgsDeclaredTypes[i]="Annotatable";
					else if(isArrayType)
						typeArgsDeclaredTypes[i]="ArrayType";
					else if(isIntersectionType)
						typeArgsDeclaredTypes[i]="IntersectionType";
					else if(isNameQualifiedType)
						typeArgsDeclaredTypes[i]="NameQualifiedType";
					else if(isParameterizedType)
						typeArgsDeclaredTypes[i]="ParameterizedType";
					else if(isPrimitiveType)
						typeArgsDeclaredTypes[i]="PrimitiveType";
					else if(isQualifiedType)
						typeArgsDeclaredTypes[i]="QualifiedType";
					else if(isSimpleType)
						typeArgsDeclaredTypes[i]="SimpleType";
					else if(isUnionType)
						typeArgsDeclaredTypes[i]="UnionType";
					else if(isVar)
						typeArgsDeclaredTypes[i]="Varialbe";
					else if(isWildcardType)
						typeArgsDeclaredTypes[i]="WildcardType";
				}
				map.put(JavaExtractor.DECLARED_TYPE, typeArgsDeclaredTypes);
				map.put(JavaExtractor.TYPE_ARG_TYPE_STR, typeArgsDeclaredTypes);
				nodeId = createNode(inserter, map);
				for(int i = 0; i<constructorArgs.size(); i++)
				{
					long argId = JavaExpressionInfo.createJavaExpressionNode(inserter, constructorArgs.get(i), sourceContent, methodName);
		    		if(argId!=-1)
		    		{
		    			inserter.createRelationship(nodeId, argId, JavaExtractor.HAVE_PARAM, new HashMap<>());
		    		}else;
				}
	        }
			else if(statement.getNodeType() == ASTNode.CONTINUE_STATEMENT)
	        {
	        	statementType="ContinueStatement";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	ContinueStatement continueStatement = (ContinueStatement)statement;
	    		SimpleName identifier = continueStatement.getLabel();
	    		if(identifier!=null)
	    			map.put(JavaExtractor.LABEL,identifier.getIdentifier());
	    		else
	    			map.put(JavaExtractor.LABEL,"null");
	    		nodeId = createNode(inserter, map);
	        }
	        else if(statement.getNodeType() == ASTNode.DO_STATEMENT)
	        {
	        	statementType="DoStatement";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        	DoStatement doStatement = (DoStatement)statement;
	    		Statement doBody = doStatement.getBody();
	    		Expression loopCondition = doStatement.getExpression();
	    		
	    		long loopConditionId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition, sourceContent, methodName);
	    		if(loopConditionId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
	    		}else;
	    		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, doBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}else;
	        }
	        else if(statement.getNodeType() == ASTNode.EMPTY_STATEMENT)
	        {
	        	statementType = "EmptyStatement";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        }
	        else if(statement.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT)
	        {
	        	statementType="EnhancedForStatement";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        	EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
	    		Expression loopCondition = enhancedForStatement.getExpression();
	    		Statement forBody = enhancedForStatement.getBody();
	    		long loopId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition, sourceContent, methodName);
	    		if(loopId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, loopId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
	    		}
	    		else;
	    		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, forBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	        }
	        else if(statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT)
	        {
	        	statementType="ExpressionStatement";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        	ExpressionStatement expressionStatement = (ExpressionStatement)statement;
	    		Expression expression = expressionStatement.getExpression();
	    		long expressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, expression, sourceContent, methodName);
	    		if(expressionId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, expressionId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	        }
	        else if(statement.getNodeType()==ASTNode.FOR_STATEMENT) 
			{
	        	statementType="ForStatement";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	    		
	    		ForStatement forStatement = (ForStatement)statement;
	    		@SuppressWarnings("unchecked")
				List<Expression> initializers = forStatement.initializers();
	    		Expression loopCondition = forStatement.getExpression();
	    		Statement forBody = forStatement.getBody();
	    		@SuppressWarnings("unchecked")
				List<Expression> updaters = forStatement.updaters();
	    		for(int i =0;i<initializers.size();i++)
	    		{
	    			long initId = JavaExpressionInfo.createJavaExpressionNode(inserter, initializers.get(i), sourceContent, methodName);
	    			if(initId!=-1)
	    			{
	    				inserter.createRelationship(nodeId, initId, JavaExtractor.INITIALIZER, new HashMap<>());
	    			}
	    			else;
	    		}
	    		long loopId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition, sourceContent, methodName);
	    		if(loopId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, loopId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
	    		}
	    		else;
	    		for(int i =0;i<updaters.size();i++)
	    		{
	    			long initId = JavaExpressionInfo.createJavaExpressionNode(inserter, updaters.get(i), sourceContent, methodName);
	    			if(initId!=-1)
	    			{
	    				inserter.createRelationship(nodeId, initId, JavaExtractor.UPDATERS, new HashMap<>());
	    			}
	    			else;
	    		}
	    		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, forBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
			}
	        else if(statement.getNodeType()==ASTNode.IF_STATEMENT) 
			{
	
	        	statementType="IfStatement";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	map.put(JavaExtractor.IF_CONDITION_NO,conditionNo);
	    		
	    		IfStatement ifStatement = (IfStatement)statement;
	    		Statement thenStatement = ifStatement.getThenStatement();
	    		Statement elseStatement = ifStatement.getElseStatement();
	    		Expression conditionalExpression = ifStatement.getExpression();
	    		nodeId = createNode(inserter, map);
	    		
	    		long conditionalExpressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, conditionalExpression, sourceContent, methodName);
	    		if(conditionalExpressionId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, conditionalExpressionId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
	    			conditionNo++;
	    		}
	    		else;
	    		long elseId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, elseStatement);
	    		if(elseId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, elseId, JavaExtractor.ELSE, new HashMap<>());
	    		}
	    		else
	    			conditionNo = 0;
	    		long thenId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, thenStatement);
	    		if(thenId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, thenId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
			}
	        else if(statement.getNodeType() == ASTNode.LABELED_STATEMENT)
	        {
	        	statementType = "LabeledStatement";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	LabeledStatement labeledStatement = (LabeledStatement)statement;
	        	Statement labeledBody = labeledStatement.getBody();
	        	String identifier = labeledStatement.getLabel().getIdentifier();
	        	map.put(JavaExtractor.LABEL, identifier);
				nodeId = createNode(inserter, map);
				long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, labeledBody);
	    		if(bodyId!=-1)
	    		{
	    			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
	    		}
	    		else;
	        }
	        else if(statement.getNodeType() == ASTNode.RETURN_STATEMENT)
	        {
	        	statementType="ReturnStatement";
				addProperties(statement, sourceContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				ReturnStatement returnStatement = (ReturnStatement)statement;
				Expression returnExpression = returnStatement.getExpression();
				long returnId = JavaExpressionInfo.createJavaExpressionNode(inserter, returnExpression, sourceContent, methodName);
				if(returnId!=-1)
				{
					inserter.createRelationship(nodeId, returnId, JavaExtractor.RETURN, new HashMap<>());
				}
				else;
	        }
	        else if(statement.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
	        {
	        	statementType = "SuperConstructorInvocation";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;
	        	
	        	@SuppressWarnings("unchecked")
				List<Type> constructorTypeArgs =superConstructorInvocation.typeArguments();
				String[] typeArgsDeclaredTypes = new String[constructorTypeArgs.size()];
				String[] typeArgsTypes = new String[constructorTypeArgs.size()];
				for(int i = 0; i<constructorTypeArgs.size(); i++)
				{
					Type type = constructorTypeArgs.get(i);
					typeArgsTypes[i]=type.toString();
					//type.resolveBinding();
					boolean isAnnotatable = type.isAnnotatable();
					boolean isArrayType = type.isArrayType();
					boolean isIntersectionType = type.isIntersectionType();
					boolean isNameQualifiedType = type.isNameQualifiedType();
					boolean isParameterizedType = type.isParameterizedType();
					boolean isPrimitiveType = type.isPrimitiveType();
					boolean isQualifiedType = type.isQualifiedType();
					boolean isSimpleType = type.isSimpleType();
					boolean isUnionType = type.isUnionType();
					boolean isVar = type.isVar();
					boolean isWildcardType = type.isWildcardType();
					if(isAnnotatable)
						typeArgsDeclaredTypes[i]="Annotatable";
					else if(isArrayType)
						typeArgsDeclaredTypes[i]="ArrayType";
					else if(isIntersectionType)
						typeArgsDeclaredTypes[i]="IntersectionType";
					else if(isNameQualifiedType)
						typeArgsDeclaredTypes[i]="NameQualifiedType";
					else if(isParameterizedType)
						typeArgsDeclaredTypes[i]="ParameterizedType";
					else if(isPrimitiveType)
						typeArgsDeclaredTypes[i]="PrimitiveType";
					else if(isQualifiedType)
						typeArgsDeclaredTypes[i]="QualifiedType";
					else if(isSimpleType)
						typeArgsDeclaredTypes[i]="SimpleType";
					else if(isUnionType)
						typeArgsDeclaredTypes[i]="UnionType";
					else if(isVar)
						typeArgsDeclaredTypes[i]="Varialbe";
					else if(isWildcardType)
						typeArgsDeclaredTypes[i]="WildcardType";
				}
				map.put(JavaExtractor.DECLARED_TYPE, typeArgsDeclaredTypes);
				map.put(JavaExtractor.TYPE_ARG_TYPE_STR, typeArgsDeclaredTypes);
	        	@SuppressWarnings("unchecked")
				List<Expression> constructorArgs = superConstructorInvocation.arguments();
				nodeId = createNode(inserter, map);
				for(int i = 0; i<constructorArgs.size(); i++)
				{
					long argId = JavaExpressionInfo.createJavaExpressionNode(inserter, constructorArgs.get(i), sourceContent, methodName);
		    		if(argId!=-1)
		    		{
		    			inserter.createRelationship(nodeId, argId, JavaExtractor.HAVE_PARAM, new HashMap<>());
		    		}else;
				}
				Expression expression = superConstructorInvocation.getExpression();
				long expressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, expression, sourceContent, methodName);
	    		if(expressionId!=-1)
	    		{
	    			inserter.createRelationship(expressionId, nodeId, JavaExtractor.INVOCATION, new HashMap<>());
	    		}else;
				//superConstructorInvocation.resolveConstructorBinding();
	        }
	        else if(statement.getNodeType() == ASTNode.SWITCH_CASE)
	        {
	        	statementType="SwitchCase";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	SwitchCase switchCase = (SwitchCase)statement;
	        	Boolean isDefault = switchCase.isDefault();
				map.put(JavaExtractor.IS_DEFAULT, isDefault);
				nodeId = createNode(inserter, map);
				Expression caseExpression = switchCase.getExpression();
				long caseId = JavaExpressionInfo.createJavaExpressionNode(inserter, caseExpression, sourceContent, methodName);
				if(caseId!=-1)
				{
					inserter.createRelationship(nodeId, caseId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
				}
	        }
	        else if(statement.getNodeType() == ASTNode.SWITCH_STATEMENT)
	        {
	        	statementType="SwitchStatement";
				addProperties(statement, sourceContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				SwitchStatement switchStatement = (SwitchStatement)statement;
				Expression enterCondition = switchStatement.getExpression();
				long conditionId = JavaExpressionInfo.createJavaExpressionNode(inserter, enterCondition, sourceContent, methodName);
				if(conditionId!=-1)
				{
					inserter.createRelationship(nodeId, conditionId, JavaExtractor.ENTER_CONDITION, new HashMap<>());
				}
				@SuppressWarnings("unchecked")
				List<Statement> statements=switchStatement.statements();
				for(int i = 0; i<statements.size();i++)
				{
					long id = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, statements.get(i));
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.STATEMENT_BODY, new HashMap<>());
					else;
				}
	        }
	        else if(statement.getNodeType() == ASTNode.SYNCHRONIZED_STATEMENT)
	        {
	        	statementType = "SynchronizedStatement";
	        	addProperties(statement, sourceContent, map, statementType, methodName);
	        	nodeId = createNode(inserter, map);
	        	SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
	        	Statement synchronizedBody = synchronizedStatement.getBody();
	        	Expression synchronizedExpression = synchronizedStatement.getExpression();
	        	long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, synchronizedBody);
				if(bodyId!=-1)
					inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
				else;
				long syncExpressionId = JavaExpressionInfo.createJavaExpressionNode(inserter, synchronizedExpression, sourceContent, methodName);
				if(syncExpressionId!=-1)
				{
					inserter.createRelationship(nodeId, syncExpressionId, JavaExtractor.SYNCHRONIZED, new HashMap<>());
				}else;
	        }
	        else if(statement.getNodeType() == ASTNode.THROW_STATEMENT)
	        {
	        	statementType="ThrowStatement";
				addProperties(statement, sourceContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				ThrowStatement throwStatement = (ThrowStatement)statement;
				Expression throwExpression = throwStatement.getExpression();
				long throwId = JavaExpressionInfo.createJavaExpressionNode(inserter, throwExpression, sourceContent, methodName);
				if(throwId!=-1)
				{
					inserter.createRelationship(nodeId, throwId, JavaExtractor.THROW, new HashMap<>());
				}else;
			}
	        else if(statement.getNodeType() == ASTNode.TRY_STATEMENT)
	        {
	        	statementType="TryStatement";
				addProperties(statement, sourceContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				TryStatement tryStatement = (TryStatement)statement;
				Statement tryBody = tryStatement.getBody();
				Statement tryFinally = tryStatement.getFinally();
				@SuppressWarnings("unchecked")
				List<Expression> resources = tryStatement.resources();
				@SuppressWarnings("unchecked")
				List<CatchClause> catchClauses = tryStatement.catchClauses();
				long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, tryBody);
				if(bodyId!=-1)
				{
					inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
				}else;
				long finallyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, tryFinally);
				if(finallyId!=-1)
				{
					inserter.createRelationship(nodeId, finallyId, JavaExtractor.FINALLY, new HashMap<>());
				}else;
				for(int i = 0; i < catchClauses.size(); i++)
				{
					long catchId = JavaStatementInfo.createCatchClauseNode(inserter, statement, methodName, sourceContent, catchClauses.get(i));
					inserter.createRelationship(nodeId, catchId, JavaExtractor.CATCH, new HashMap<>());
				}
				for(Expression resource:resources)
				{
					long resourceId = JavaExpressionInfo.createJavaExpressionNode(inserter, resource, sourceContent, methodName);
					inserter.createRelationship(nodeId, resourceId, JavaExtractor.TRY_RESOURCE, new HashMap<>());
				}
	        }
	        else if(statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT)
            {
	        	statementType="VariableDeclarationStatement";
				addProperties(statement, sourceContent, map, statementType, methodName);
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
				Type type = variableDeclarationStatement.getType();
				String typeLiteral = type.toString();
				map.put(JavaExtractor.VAR_TYPE_STR, typeLiteral);
				int modifier = variableDeclarationStatement.getModifiers();
				if(Modifier.isFinal(modifier))
					map.put(JavaExtractor.IS_FINAL, true);
				else
					map.put(JavaExtractor.IS_FINAL, false);
				if(Modifier.isStatic(modifier))
					map.put(JavaExtractor.IS_STATIC, true);
				else
					map.put(JavaExtractor.IS_STATIC, false);
				if(Modifier.isVolatile(modifier))
					map.put(JavaExtractor.IS_VOLATILE, true);
				else
					map.put(JavaExtractor.IS_VOLATILE, false);
				if(Modifier.isTransient(modifier))
					map.put(JavaExtractor.IS_TRANSIENT, true);
				else
					map.put(JavaExtractor.IS_TRANSIENT, false);
				boolean isAnnotatable = type.isAnnotatable();
				boolean isArrayType = type.isArrayType();
				boolean isIntersectionType = type.isIntersectionType();
				boolean isNameQualifiedType = type.isNameQualifiedType();
				boolean isParameterizedType = type.isParameterizedType();
				boolean isPrimitiveType = type.isPrimitiveType();
				boolean isQualifiedType = type.isQualifiedType();
				boolean isSimpleType = type.isSimpleType();
				boolean isUnionType = type.isUnionType();
				boolean isVar = type.isVar();
				boolean isWildcardType = type.isWildcardType();
				if(isAnnotatable)
				map.put(JavaExtractor.DECLARED_TYPE, "Annotatable");
				else if(isArrayType)
				map.put(JavaExtractor.DECLARED_TYPE, "ArrayType");
				else if(isIntersectionType)
				map.put(JavaExtractor.DECLARED_TYPE, "IntersectionType");
				else if(isNameQualifiedType)
				map.put(JavaExtractor.DECLARED_TYPE, "NameQualifiedType");
				else if(isParameterizedType)
				map.put(JavaExtractor.DECLARED_TYPE, "ParameterizedType");
				else if(isPrimitiveType)
				map.put(JavaExtractor.DECLARED_TYPE, "PrimitiveType");
				else if(isQualifiedType)
				map.put(JavaExtractor.DECLARED_TYPE, "QualifiedType");
				else if(isSimpleType)
				map.put(JavaExtractor.DECLARED_TYPE, "SimpleType");
				else if(isUnionType)
				map.put(JavaExtractor.DECLARED_TYPE, "UnionType");
				else if(isVar)
				map.put(JavaExtractor.DECLARED_TYPE, "Varialbe");
				else if(isWildcardType)
				map.put(JavaExtractor.DECLARED_TYPE, "WildcardType");
				nodeId = createNode(inserter, map);
				
				@SuppressWarnings("unchecked")
				List<VariableDeclarationFragment> fragments = variableDeclarationStatement.fragments();
				for(int i = 0; i<fragments.size(); i++)
				{
					long id = JavaStatementInfo.createVariableDeclarationFragmentNode(inserter, map, methodName, i, fragments.get(i), sourceContent);
					if(id!=-1)
						inserter.createRelationship(nodeId, id, JavaExtractor.VAR_DECLARATION_FRAG, new HashMap<>());
					else;
				}
            }
	        else if(statement.getNodeType()==ASTNode.WHILE_STATEMENT) 
			{
	        	statementType="WhileStatement";
				addProperties(statement, sourceContent, map, statementType, methodName);
				nodeId = createNode(inserter, map);
				WhileStatement whileStatement = (WhileStatement)statement;
				Expression loopCondition = whileStatement.getExpression();
				Statement whileBody = whileStatement.getBody();
				long loopConditionId = JavaExpressionInfo.createJavaExpressionNode(inserter, loopCondition, sourceContent, methodName);
				if(loopConditionId!=-1)
				{
					inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
				}else;
				long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, whileBody);
				if(bodyId!=-1)
				{
					inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
				}else;
			}
	        else
			{
				TempStatementInfo info = new TempStatementInfo(inserter, methodName, statement);
				nodeId = info.getNodeId();
			}
			return nodeId;
		}
		else
			return -1;	
	}
	
	static long createVariableDeclarationFragmentNode(BatchInserter inserter, HashMap<String, Object> map, String methodName, int decNo,
			VariableDeclarationFragment variableDeclarationFragment, String sourceContent) {
		String statementType = "VariableDeclarationFragment";
		addProperties(variableDeclarationFragment, sourceContent, map, statementType, methodName);
		int dimensions = variableDeclarationFragment.getExtraDimensions();
		Expression initializer = variableDeclarationFragment.getInitializer();
		String identifier = variableDeclarationFragment.getName().getIdentifier();
//		variableDeclarationFragment.resolveBinding();
		map.put(JavaExtractor.DIMENSION, dimensions);
		map.put(JavaExtractor.NAME, identifier);
		long nodeId = inserter.createNode(map, JavaExtractor.VARIABLE_DECLARATION_FRAGMENT);
		long initializerId = JavaExpressionInfo.createJavaExpressionNode(inserter, initializer, sourceContent, methodName);
		if(initializerId!=-1)
			inserter.createRelationship(nodeId, initializerId, JavaExtractor.INITIALIZER, new HashMap<>());
		else;
		return nodeId;
	}

	private static long createCatchClauseNode(BatchInserter inserter, Statement statement, String methodName, String sourceContent,
			CatchClause catchClause) {
		HashMap<String, Object> map = new  HashMap<>();
		int rowNo = sourceContent.substring(0, statement.getStartPosition()).split("\n").length;
		map.put(JavaExtractor.ROW_NO, rowNo);
		map.put(JavaExtractor.METHOD_NAME, methodName);
		long nodeId = inserter.createNode(map, JavaExtractor.CATCH_CLAUSE);
		Statement catchBody = catchClause.getBody();
		SingleVariableDeclaration exception = catchClause.getException();
		long bodyId = JavaStatementInfo.createJavaStatementNode(inserter, methodName, sourceContent, catchBody);
		if(bodyId!=-1)
		{
			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}else;
		long exceptionId = JavaStatementInfo.createSingleVarDeclarationNode(inserter, methodName, exception);
		if(exceptionId!=-1)
		{
			inserter.createRelationship(nodeId, exceptionId, JavaExtractor.EXCEPTION_DECLARATION, new HashMap<>());
		}else;
		return nodeId;
	}

	private static long createSingleVarDeclarationNode(BatchInserter inserter, String methodName, SingleVariableDeclaration exception) {
		HashMap<String, Object> map = new  HashMap<>();
		map.put(JavaExtractor.METHOD_NAME, methodName);
		//exception.
		long nodeId = inserter.createNode(map, JavaExtractor.SINGLE_VARIABLE_DECLARATION);
		return nodeId;	
	}

	private static long createNode(BatchInserter inserter, HashMap<String, Object> map) {
		long id = inserter.createNode(map, JavaExtractor.STATEMENT);
        return id;
    }
	
	private static void addProperties(ASTNode statement, String sourceContent, HashMap<String, Object> map, String statementType, String belongTo) {
        map.put(JavaExtractor.STATEMENT_TYPE, statementType);
        map.put(JavaExtractor.METHOD_NAME, belongTo);
    	String content = sourceContent.substring(statement.getStartPosition(),statement.getStartPosition()+statement.getLength());
		map.put(JavaExtractor.CONTENT, content);
		int rowNo = sourceContent.substring(0, statement.getStartPosition()).split("\n").length;
		map.put(JavaExtractor.ROW_NO, rowNo);
    }
}
