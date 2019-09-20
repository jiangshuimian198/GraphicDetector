package main.java.javaextractor.infos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
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
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.javaextractor.JavaExtractor;

public class JavaStatementInfo {
	private static int conditionNo = 0;
//	private long nodeId;
//	private String statementType;
	private static JavaExpressionInfo m_JavaExpressionInfo = new JavaExpressionInfo();
	
	@SuppressWarnings("unchecked")
	public long createJavaStatementNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String className, String sourceContent, Statement statement)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		long nodeId;
		String statementType;
		
		if(statement!=null) 
		{
			switch(statement.getNodeType())
			{
				case ASTNode.ASSERT_STATEMENT:
				{
		        	statementType="AssertStatement";
					addProperties(statement, sourceContent, map, statementType, className);
					AssertStatement assertStatement = (AssertStatement)statement;
					nodeId = createNode(inserter, map);
					
					Expression assertExpression = assertStatement.getExpression();
		    		m_JavaExpressionInfo.createRelationship(inserter, assertExpression, nodeId, new HashMap<>(), JavaExtractor.ASSERT, sourceContent, className, javaProjectInfo);
		        }break;
		        
				case ASTNode.BLOCK:
				{
					statementType="Block";
					addProperties(statement, sourceContent, map, statementType, className);
					nodeId = createNode(inserter, map);
					
					Block block = (Block)statement;
					List<Statement> statements = block.statements();
					int statementNo = 0;
					for(Statement element : statements)
					{
						Map<String, Object> relProp = new HashMap<>();
						relProp.put(JavaExtractor.STATEMENT_NO, statementNo++);
						createRelationship(inserter, element, statementType, nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
					}
				}break;
				
				case ASTNode.BREAK_STATEMENT:
				{
					statementType="BreakStatement";
					addProperties(statement, sourceContent, map, statementType, className);
					BreakStatement breakStatement = (BreakStatement)statement;
					SimpleName identifier = breakStatement.getLabel();
					if(identifier!=null)
						map.put(JavaExtractor.LABEL,identifier.getIdentifier());
					else
						map.put(JavaExtractor.LABEL,"null");
					nodeId = createNode(inserter, map);
		        }break;
		        
				case ASTNode.CONSTRUCTOR_INVOCATION:
				{
					statementType="ConstructorInvocation";
					addProperties(statement, sourceContent, map, statementType, className);
					ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
					List<ITypeBinding> bindedTypeList = m_JavaExpressionInfo.addTypeArgProperties(map,constructorInvocation.typeArguments());
					nodeId = createNode(inserter, map);
					
					List<Expression> constructorArgs = constructorInvocation.arguments();
					for(Expression element : constructorArgs)
					{
			    		m_JavaExpressionInfo.createRelationship(inserter, element, nodeId, new HashMap<>(), JavaExtractor.HAVE_ARG, sourceContent, className, javaProjectInfo);
					}
					
					for(ITypeBinding element : bindedTypeList)
						m_JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element, JavaExtractor.TYPE_ARG_TYPE_BINDING);
					IMethodBinding constructorBinding = constructorInvocation.resolveConstructorBinding();
					if(constructorBinding != null)
						m_JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, constructorBinding);
		        }break;
		        
				case ASTNode.CONTINUE_STATEMENT:
				{
		        	statementType="ContinueStatement";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	ContinueStatement continueStatement = (ContinueStatement)statement;
		    		SimpleName identifier = continueStatement.getLabel();
		    		if(identifier!=null)
		    			map.put(JavaExtractor.LABEL,identifier.getIdentifier());
		    		else
		    			map.put(JavaExtractor.LABEL,"null");
		    		nodeId = createNode(inserter, map);
		        }break;
		        
				case ASTNode.DO_STATEMENT:
				{
		        	statementType="DoStatement";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	nodeId = createNode(inserter, map);
		        	
		        	DoStatement doStatement = (DoStatement)statement;
		    		Statement doBody = doStatement.getBody();
		    		Expression loopCondition = doStatement.getExpression();
		    		m_JavaExpressionInfo.createRelationship(inserter, loopCondition, nodeId, new HashMap<>(), JavaExtractor.LOOP_CONDITION, sourceContent, className, javaProjectInfo);
		    		int statementNo = 0;
					Map<String, Object> relProp = new HashMap<>();
					relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		    		createRelationship(inserter, doBody, statementType, nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
		        }break;
		        
				case ASTNode.EMPTY_STATEMENT:
				{
		        	statementType = "EmptyStatement";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	nodeId = createNode(inserter, map);
		        }break;
		        
				case ASTNode.ENHANCED_FOR_STATEMENT:
				{
		        	statementType="EnhancedForStatement";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	nodeId = createNode(inserter, map);
		        	
		        	EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
		    		Expression loopCondition = enhancedForStatement.getExpression();
		    		Statement forBody = enhancedForStatement.getBody();
		    		m_JavaExpressionInfo.createRelationship(inserter, loopCondition, nodeId, new HashMap<>(), JavaExtractor.LOOP_CONDITION, sourceContent, className, javaProjectInfo);
		    		int statementNo = 0;
					Map<String, Object> relProp = new HashMap<>();
					relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		    		createRelationship(inserter, forBody, statementType, nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
		        }break;
		        
				case ASTNode.EXPRESSION_STATEMENT:
				{
		        	statementType="ExpressionStatement";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	nodeId = createNode(inserter, map);
		        	
		        	ExpressionStatement expressionStatement = (ExpressionStatement)statement;
		    		Expression expression = expressionStatement.getExpression();
		    		int statementNo = 0;
					Map<String, Object> relProp = new HashMap<>();
					relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		    		m_JavaExpressionInfo.createRelationship(inserter, expression, nodeId, relProp, JavaExtractor.STATEMENT_BODY, sourceContent, className, javaProjectInfo);
		        }break;
		        
				case ASTNode.FOR_STATEMENT:
				{
		        	statementType="ForStatement";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	nodeId = createNode(inserter, map);
		    		
		    		ForStatement forStatement = (ForStatement)statement;
		    		List<Expression> initializers = forStatement.initializers();
		    		Expression loopCondition = forStatement.getExpression();
		    		Statement forBody = forStatement.getBody();
		    		List<Expression> updaters = forStatement.updaters();
		    		for(Expression element : initializers)
		    		{
			    		m_JavaExpressionInfo.createRelationship(inserter, element, nodeId, new HashMap<>(), JavaExtractor.INITIALIZER, sourceContent, className, javaProjectInfo);
		    		}
		    		m_JavaExpressionInfo.createRelationship(inserter, loopCondition, nodeId, new HashMap<>(), JavaExtractor.LOOP_CONDITION, sourceContent, className, javaProjectInfo);
		    		for(Expression element : updaters)
		    		{
			    		m_JavaExpressionInfo.createRelationship(inserter, element, nodeId, new HashMap<>(), JavaExtractor.UPDATER, sourceContent, className, javaProjectInfo);
		    		}
		    		int statementNo = 0;
					Map<String, Object> relProp = new HashMap<>();
					relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		    		createRelationship(inserter, forBody, statementType, nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
				}break;
				
				case ASTNode.IF_STATEMENT:
				{
					
		        	statementType="IfStatement";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	map.put(JavaExtractor.IF_CONDITION_NO,conditionNo);
		        	nodeId = createNode(inserter, map);
		    		
		    		IfStatement ifStatement = (IfStatement)statement;
		    		Statement thenStatement = ifStatement.getThenStatement();
		    		Statement elseStatement = ifStatement.getElseStatement();
		    		Expression conditionalExpression = ifStatement.getExpression();
		    		m_JavaExpressionInfo.createRelationship(inserter, conditionalExpression, nodeId, new HashMap<>(), JavaExtractor.ENTER_CONDITION, sourceContent, className, javaProjectInfo);
		    		int statementNo = 0;
					Map<String, Object> relProp = new HashMap<>();
					relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		    		createRelationship(inserter, elseStatement, statementType, nodeId, JavaExtractor.ELSE, relProp, sourceContent, className, javaProjectInfo);
		    		createRelationship(inserter, thenStatement, statementType, nodeId, JavaExtractor.THEN, relProp, sourceContent, className, javaProjectInfo);
				}break;
				
				case ASTNode.LABELED_STATEMENT:
				{
		        	statementType = "LabeledStatement";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	LabeledStatement labeledStatement = (LabeledStatement)statement;
		        	Statement labeledBody = labeledStatement.getBody();
		        	map.put(JavaExtractor.LABEL, labeledStatement.getLabel().getIdentifier());
					nodeId = createNode(inserter, map);
					
					int statementNo = 0;
					Map<String, Object> relProp = new HashMap<>();
					relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		    		createRelationship(inserter, labeledBody, statementType, nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
		        }break;
		        
				case ASTNode.RETURN_STATEMENT:
				{
		        	statementType="ReturnStatement";
					addProperties(statement, sourceContent, map, statementType, className);
					nodeId = createNode(inserter, map);
					
					ReturnStatement returnStatement = (ReturnStatement)statement;
					Expression returnExpression = returnStatement.getExpression();
		    		m_JavaExpressionInfo.createRelationship(inserter, returnExpression, nodeId, new HashMap<>(), JavaExtractor.RETURN, sourceContent, className, javaProjectInfo);
		        }break;
		        
				case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
				{
		        	statementType = "SuperConstructorInvocation";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	nodeId = createNode(inserter, map);
		        	SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;

		        	List<ITypeBinding> bindedTypeList = m_JavaExpressionInfo.addTypeArgProperties(map,superConstructorInvocation.typeArguments());
		        	List<Expression> constructorArgs = superConstructorInvocation.arguments();
					for(Expression element : constructorArgs)
					{
			    		m_JavaExpressionInfo.createRelationship(inserter, element, nodeId, new HashMap<>(), JavaExtractor.HAVE_ARG, sourceContent, className, javaProjectInfo);
					}
					Expression expression = superConstructorInvocation.getExpression();
		    		m_JavaExpressionInfo.createRelationship(inserter, expression, nodeId, new HashMap<>(), JavaExtractor.INVOCATED_BY, sourceContent, className, javaProjectInfo);

		    		for(ITypeBinding element : bindedTypeList)
						m_JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, element, JavaExtractor.TYPE_ARG_TYPE_BINDING);
		    		IMethodBinding constructorBinding = superConstructorInvocation.resolveConstructorBinding();
					if(constructorBinding != null)
						m_JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, constructorBinding);
		        }break;
		        
				case ASTNode.SWITCH_CASE:
				{
		        	statementType="SwitchCase";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	SwitchCase switchCase = (SwitchCase)statement;
		        	Boolean isDefault = switchCase.isDefault();
					map.put(JavaExtractor.IS_DEFAULT, isDefault);
					nodeId = createNode(inserter, map);
					
					Expression caseExpression = switchCase.getExpression();
		    		m_JavaExpressionInfo.createRelationship(inserter, caseExpression, nodeId, new HashMap<>(), JavaExtractor.ENTER_CONDITION, sourceContent, className, javaProjectInfo);
		        }break;
		        
				case ASTNode.SWITCH_STATEMENT:
				{
		        	statementType="SwitchStatement";
					addProperties(statement, sourceContent, map, statementType, className);
					SwitchStatement switchStatement = (SwitchStatement)statement;
					List<Statement> statements = switchStatement.statements();
					boolean haveDefault = false;
					for(Statement element : statements)
					{
			    		if(element.getNodeType() == ASTNode.SWITCH_CASE)
			    		{
			    			SwitchCase switchCase = (SwitchCase)element;
			    			if(switchCase.isDefault())
			    			{
			    				haveDefault = true;
			    				break;
			    			}
			    		}
					}
					map.put(JavaExtractor.HAVE_DEFAULT_CASE, haveDefault);
					nodeId = createNode(inserter, map);
					
					Expression enterCondition = switchStatement.getExpression();
		    		m_JavaExpressionInfo.createRelationship(inserter, enterCondition, nodeId, new HashMap<>(), JavaExtractor.SWITCH, sourceContent, className, javaProjectInfo);

					int statementNo = 0;
					for(Statement element : statements)
					{
						Map<String, Object> relProp = new HashMap<>();
						relProp.put(JavaExtractor.STATEMENT_NO, statementNo++);
			    		createRelationship(inserter, element, statementType, nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
					}
		        }break;
		        
				case ASTNode.SYNCHRONIZED_STATEMENT:
				{
		        	statementType = "SynchronizedStatement";
		        	addProperties(statement, sourceContent, map, statementType, className);
		        	nodeId = createNode(inserter, map);
		        	
		        	SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
		        	Statement synchronizedBody = synchronizedStatement.getBody();
		        	Expression synchronizedExpression = synchronizedStatement.getExpression();
		        	int statementNo = 0;
					Map<String, Object> relProp = new HashMap<>();
					relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		    		createRelationship(inserter, synchronizedBody, statementType, nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
		    		m_JavaExpressionInfo.createRelationship(inserter, synchronizedExpression, nodeId, new HashMap<>(), JavaExtractor.SYNCHRONIZED, sourceContent, className, javaProjectInfo);
		        }break;
		        
				case ASTNode.THROW_STATEMENT:
				{
		        	statementType="ThrowStatement";
					addProperties(statement, sourceContent, map, statementType, className);
					nodeId = createNode(inserter, map);
					
					ThrowStatement throwStatement = (ThrowStatement)statement;
					Expression throwExpression = throwStatement.getExpression();
		    		m_JavaExpressionInfo.createRelationship(inserter, throwExpression, nodeId, new HashMap<>(), JavaExtractor.THROW, sourceContent, className, javaProjectInfo);
				}break;
				
				case ASTNode.TRY_STATEMENT:
				{
		        	statementType="TryStatement";
					addProperties(statement, sourceContent, map, statementType, className);
					nodeId = createNode(inserter, map);
					
					TryStatement tryStatement = (TryStatement)statement;
					Statement tryBody = tryStatement.getBody();
					Statement tryFinally = tryStatement.getFinally();
					List<Expression> resources = tryStatement.resources();
					List<CatchClause> catchClauses = tryStatement.catchClauses();
					int statementNo = 0;
					Map<String, Object> relProp = new HashMap<>();
					relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		    		createRelationship(inserter, tryBody, statementType, nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
		    		createRelationship(inserter, tryFinally, statementType, nodeId, JavaExtractor.FINALLY, relProp, sourceContent, className, javaProjectInfo);

					for(CatchClause element : catchClauses)
					{
						long catchId = createCatchClauseNode(inserter, javaProjectInfo, className, sourceContent, element);
						inserter.createRelationship(nodeId, catchId, JavaExtractor.CATCH, new HashMap<>());
					}
					for(Expression element : resources)
					{
			    		m_JavaExpressionInfo.createRelationship(inserter, element, nodeId, new HashMap<>(), JavaExtractor.TRY_RESOURCE, sourceContent, className, javaProjectInfo);
					}
		        }break;
		        
				case ASTNode.VARIABLE_DECLARATION_STATEMENT:
				{
		        	statementType="VariableDeclarationStatement";
					addProperties(statement, sourceContent, map, statementType, className);
					VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
					map.put(JavaExtractor.VAR_TYPE_STR, variableDeclarationStatement.getType().toString());
					m_JavaExpressionInfo.addModifierProperty(map, variableDeclarationStatement.getModifiers());
					m_JavaExpressionInfo.addDeclaredTypeProperty(map, variableDeclarationStatement.getType());
					nodeId = createNode(inserter, map);
					
					List<VariableDeclarationFragment> fragments = variableDeclarationStatement.fragments();
					for(VariableDeclarationFragment element : fragments)
					{
						long id = createVariableDeclarationFragmentNode(inserter, javaProjectInfo, className, element, sourceContent);
						if(id!=-1)
							inserter.createRelationship(nodeId, id, JavaExtractor.VAR_DECLARATION_FRAG, new HashMap<>());
						else;
					}
	            }break;
	            
				case ASTNode.WHILE_STATEMENT:
				{
		        	statementType="WhileStatement";
					addProperties(statement, sourceContent, map, statementType, className);
					nodeId = createNode(inserter, map);
					
					WhileStatement whileStatement = (WhileStatement)statement;
					Expression loopCondition = whileStatement.getExpression();
					Statement whileBody = whileStatement.getBody();
		    		m_JavaExpressionInfo.createRelationship(inserter, loopCondition, nodeId, new HashMap<>(), JavaExtractor.LOOP_CONDITION, sourceContent, className, javaProjectInfo);
		    		int statementNo = 0;
					Map<String, Object> relProp = new HashMap<>();
					relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		    		createRelationship(inserter, whileBody, statementType, nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
				}break;
				
				default:
				{
					OtherStatementInfo info = new OtherStatementInfo(inserter, className, statement);
					nodeId = info.getNodeId();
				}
			}
			return nodeId;
		}
		else
			return -1;	
	}
	
	void createRelationship(BatchInserter inserter, Statement statement, String statementType, long originalNodeId,
			RelationshipType relationship, Map<String, Object> relProp, String sourceContent, String methodName, JavaProjectInfo javaProjectInfo) {
		long terminalNodeId = createJavaStatementNode(inserter, javaProjectInfo, methodName, sourceContent, statement);
		if(terminalNodeId != -1)
		{
			inserter.createRelationship(originalNodeId, terminalNodeId, relationship, relProp);
			if(statementType.equals("IfStatement"))
				conditionNo++;
		}
		else
			if(statementType.equals("IfStatement"))
				conditionNo = 0;
	}

	long createVariableDeclarationFragmentNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String methodName,
			VariableDeclarationFragment variableDeclarationFragment, String sourceContent) {
		HashMap<String, Object> map = new  HashMap<>();
		map.put(JavaExtractor.BELONG_TO, methodName);
		map.put(JavaExtractor.CONTENT, sourceContent.substring(variableDeclarationFragment.getStartPosition(),variableDeclarationFragment.getStartPosition()+variableDeclarationFragment.getLength()));
		map.put(JavaExtractor.ROW_NO, sourceContent.substring(0, variableDeclarationFragment.getStartPosition()).split("\n").length);
		Expression initializer = variableDeclarationFragment.getInitializer();
		map.put(JavaExtractor.EXTRA_DIMENSION_NUM, variableDeclarationFragment.getExtraDimensions());
		map.put(JavaExtractor.IDENTIFIER, variableDeclarationFragment.getName().getIdentifier());
		long nodeId = inserter.createNode(map, JavaExtractor.VARIABLE_DECLARATION_FRAGMENT);
		m_JavaExpressionInfo.createRelationship(inserter, initializer, nodeId, new HashMap<>(), JavaExtractor.INITIALIZER, sourceContent, methodName, javaProjectInfo);
		return nodeId;
	}

	private long createCatchClauseNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String className, String sourceContent,
			CatchClause catchClause) {
		HashMap<String, Object> map = new  HashMap<>();
		map.put(JavaExtractor.BELONG_TO, className);
		map.put(JavaExtractor.CONTENT, sourceContent.substring(catchClause.getStartPosition(),catchClause.getStartPosition()+catchClause.getLength()));
		map.put(JavaExtractor.ROW_NO, sourceContent.substring(0, catchClause.getStartPosition()).split("\n").length);
		long nodeId = inserter.createNode(map, JavaExtractor.CATCH_CLAUSE);
		Statement catchBody = catchClause.getBody();
		SingleVariableDeclaration exception = catchClause.getException();
		int statementNo = 0;
		Map<String, Object> relProp = new HashMap<>();
		relProp.put(JavaExtractor.STATEMENT_NO, statementNo);
		createRelationship(inserter, catchBody, "CatchClause", nodeId, JavaExtractor.STATEMENT_BODY, relProp, sourceContent, className, javaProjectInfo);
		long exceptionId = createSingleVarDeclarationNode(inserter, javaProjectInfo, className, sourceContent, exception);
		if(exceptionId!=-1)
		{
			inserter.createRelationship(nodeId, exceptionId, JavaExtractor.EXCEPTION_CAUGHT, new HashMap<>());
		}else;
		return nodeId;
	}

	public long createSingleVarDeclarationNode(BatchInserter inserter, JavaProjectInfo javaProjectInfo, String className, String sourceContent, SingleVariableDeclaration singleVarDec) {
		HashMap<String, Object> map = new HashMap<>();
		map.put(JavaExtractor.BELONG_TO, className);
		map.put(JavaExtractor.CONTENT, sourceContent.substring(singleVarDec.getStartPosition(),singleVarDec.getStartPosition()+singleVarDec.getLength()));
		map.put(JavaExtractor.ROW_NO, sourceContent.substring(0, singleVarDec.getStartPosition()).split("\n").length);
		map.put(JavaExtractor.IDENTIFIER, singleVarDec.getName().getIdentifier());
		m_JavaExpressionInfo.addModifierProperty(map, singleVarDec.getModifiers());
		map.put(JavaExtractor.IS_VARIABLE_ARITY_METHOD_ARG, Boolean.toString(singleVarDec.isVarargs()));
//		singleVarDec.varargsAnnotations();
		Type varType = singleVarDec.getType();
		map.put(JavaExtractor.VAR_TYPE_STR, varType.toString());
		long nodeId = inserter.createNode(map, JavaExtractor.SINGLE_VARIABLE_DECLARATION);
		m_JavaExpressionInfo.resolveTypeBinding(inserter, javaProjectInfo, nodeId, varType.resolveBinding(), JavaExtractor.TYPE_BINDING);
		Expression initializer = singleVarDec.getInitializer();
		m_JavaExpressionInfo.createRelationship(inserter, initializer, nodeId, map, JavaExtractor.INITIALIZER, sourceContent, className, javaProjectInfo);
		IVariableBinding binding = singleVarDec.resolveBinding();
		m_JavaExpressionInfo.resolveMethodBinding(inserter, javaProjectInfo, nodeId, binding.getDeclaringMethod());
		@SuppressWarnings("unchecked")
		List<Dimension> list = singleVarDec.extraDimensions();
		if(list != null)
			for(Dimension element : list)
			{
				HashMap<String, Object> dmap = new HashMap<>();
				dmap.put(JavaExtractor.BELONG_TO, className);
		    	dmap.put(JavaExtractor.CONTENT, sourceContent.substring(element.getStartPosition(),element.getStartPosition()+element.getLength()));
				dmap.put(JavaExtractor.ROW_NO, sourceContent.substring(0, element.getStartPosition()).split("\n").length);
				long dimensionId = inserter.createNode(dmap, JavaExtractor.DIMENSION);
				inserter.createRelationship(nodeId, dimensionId, JavaExtractor.EXTRA_DIMENSION, new HashMap<>());
				@SuppressWarnings("unchecked")
				List<Annotation> annoList = element.annotations();
				for(Annotation anno : annoList) 
				{
					long annoId = m_JavaExpressionInfo.createJavaExpressionNode(inserter, anno, sourceContent, className, javaProjectInfo);
					if(annoId!=-1)
						inserter.createRelationship(nodeId, annoId, JavaExtractor.HAVE_ANNOTATION, new HashMap<>());
					else;
				}
			}
		else;
		return nodeId;	
	}

	private static long createNode(BatchInserter inserter, HashMap<String, Object> map) {
		long id = inserter.createNode(map, JavaExtractor.STATEMENT);
        return id;
    }
	
	private static void addProperties(ASTNode statement, String codeContent, HashMap<String, Object> map, String statementType, String belongTo) {
        map.put(JavaExtractor.STATEMENT_TYPE, statementType);
        map.put(JavaExtractor.BELONG_TO, belongTo);
		map.put(JavaExtractor.CONTENT, codeContent.substring(statement.getStartPosition(),statement.getStartPosition()+statement.getLength()));
		map.put(JavaExtractor.ROW_NO, codeContent.substring(0, statement.getStartPosition()).split("\n").length);
    }
}
