package main.java.JCExtractor;

import main.java.Extractor.KnowledgeExtractor;
import main.java.infos.JavaProjectInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 解析java源代码，抽取出代码实体以及这些代码实体之间的静态依赖关系，并将它们存储到neo4j图数据库中：
 * <p>
 * Class实体示例：
 * name: UnixStat
 * fullName: zstorg.apache.tools.zip.UnixStat
 * content, comment, isAbstract, isFinal, isInterface, visibility
 * <p>
 * Method实体示例：
 * name: error
 * fullName: cn.edu.pku.sei.tsr.service.ras.util.ZipGenerator.error( String msg, boolean quit )
 * paramType: String msg, boolean quit
 * returnType: void
 * content, comment, isAbstract, isConstructor, isFinal, isStatic, isSynchronized, visibility
 * <p>
 * Field实体示例：
 * name: STRATEGY_ASSIGN
 * fullName: cn.edu.pku.sei.tsr.entity.ConfigurationItem.STRATEGY_ASSIGN
 * isFinal, isStatic, type, visibility
 */

@Slf4j
public class JavaExtractor extends KnowledgeExtractor {

	//节点类型
    public static final Label CLASS = Label.label("Class");
	public static final Label ANONYMOUS_CLASS = Label.label("AnonymousClass");
    public static final Label METHOD = Label.label("Method");
    public static final Label FIELD = Label.label("Field");
    public static final Label PACKAGE = Label.label("Package");
    public static final Label STATEMENT = Label.label("Statement");
    public static final Label EXPREESION = Label.label("Expression");
    public static final Label OPERATOR = Label.label("Operator");
    public static final Label VARIABLE_DECLARATION_FRAGMENT = Label.label("VariableDeclarationFragment");
    public static final Label SINGLE_VARIABLE_DECLARATION = Label.label("SingleVariableDeclaration");
	public static final Label CATCH_CLAUSE = Label.label("CatchClause");
	public static final Label DIMENSION = Label.label("Dimension");
	
    
    //关系类型
    public static final RelationshipType EXTEND = RelationshipType.withName("extend");
    public static final RelationshipType HAVE_CLASS = RelationshipType.withName("haveClass");
    public static final RelationshipType HAVE_METHOD = RelationshipType.withName("haveMethod");
    public static final RelationshipType HAVE_STATEMENT = RelationshipType.withName("haveStatement");
    public static final RelationshipType STATEMENT_BODY = RelationshipType.withName("statementBody");
    public static final RelationshipType IMPLEMENT = RelationshipType.withName("implement");
    public static final RelationshipType PARAM_TYPE = RelationshipType.withName("paramType");
    public static final RelationshipType RETURN_TYPE = RelationshipType.withName("returnType");
    public static final RelationshipType THROW_TYPE = RelationshipType.withName("throwType");
    public static final RelationshipType METHOD_CALL = RelationshipType.withName("methodCall");
    public static final RelationshipType VARIABLE_TYPE = RelationshipType.withName("variableType");
    public static final RelationshipType HAVE_FIELD = RelationshipType.withName("haveField");
    public static final RelationshipType FIELD_TYPE = RelationshipType.withName("fieldType");
    public static final RelationshipType FIELD_ACCESS = RelationshipType.withName("fieldAccess");
	public static final RelationshipType THEN = RelationshipType.withName("then");
    public static final RelationshipType ELSE = RelationshipType.withName("else");
    public static final RelationshipType ENTER_CONDITION = RelationshipType.withName("enterCondition");
    public static final RelationshipType LOOP_CONDITION = RelationshipType.withName("loopCondition");
    public static final RelationshipType RETURN = RelationshipType.withName("return");
    public static final RelationshipType INITIALIZER = RelationshipType.withName("initializer");
	public static final RelationshipType UPDATER = RelationshipType.withName("updater");
	public static final RelationshipType ASSERT = RelationshipType.withName("assert");
	public static final RelationshipType THROW = RelationshipType.withName("throw");
	public static final RelationshipType FINALLY = RelationshipType.withName("finally");
	public static final RelationshipType CATCH = RelationshipType.withName("catch");
	public static final RelationshipType TRY_RESOURCE = RelationshipType.withName("tryResource");
	public static final RelationshipType EXCEPTION_CAUGHT = RelationshipType.withName("exceptionCaught");
	public static final RelationshipType HAVE_PARAM = RelationshipType.withName("haveParament");
	public static final RelationshipType SYNCHRONIZED = RelationshipType.withName("synchronized");
	public static final RelationshipType ARRAY_ACCESS = RelationshipType.withName("arrayAccess");
	public static final RelationshipType ARRAY_ACCESS_INDEX = RelationshipType.withName("arrayAccessIndex");
	public static final RelationshipType ARRAY_INITIALIZER = RelationshipType.withName("arrayInitializer");
	public static final RelationshipType SUB_ARRAY_INITIALIZER = RelationshipType.withName("subArrayInitializer");
	public static final RelationshipType DIMENSIONS = RelationshipType.withName("dimensions");
	public static final RelationshipType ASSIGNMENT = RelationshipType.withName("assignment");
	public static final RelationshipType LEFT_OPERAND = RelationshipType.withName("leftOperand");
	public static final RelationshipType RIGHT_OPERAND = RelationshipType.withName("rightOperand");
	public static final RelationshipType VAR_DECLARATION_FRAG = RelationshipType.withName("variableDeclarationFragment");
	public static final RelationshipType INVOCATION = RelationshipType.withName("invocation");
	public static final RelationshipType NORMAL_ANNOTATION_VALUE = RelationshipType.withName("normalannotationValue");
	public static final RelationshipType SINGLE_MEMBER_ANNOTATION_VALUE = RelationshipType.withName("singleMemberAnnotation");
	public static final RelationshipType PREFIX_OPRD = RelationshipType.withName("prefixOprd");
	public static final RelationshipType PREFIX = RelationshipType.withName("prefix");
	public static final RelationshipType POSTFIX = RelationshipType.withName("postfix");
	public static final RelationshipType POSTFIX_OPRD = RelationshipType.withName("postfixOprd");
	public static final RelationshipType INFIX = RelationshipType.withName("infix");
	public static final RelationshipType CAST = RelationshipType.withName("cast");	
	public static final RelationshipType LAMBDA_BODY = RelationshipType.withName("lambdaBody");
	public static final RelationshipType EXTRA_DIMENSION = RelationshipType.withName("extraDimension");
	public static final RelationshipType HAVE_ANNOTATION = RelationshipType.withName("haveAnnotation");
	public static final RelationshipType LAMBDA_PARAMETER = RelationshipType.withName("lambdaParameter");
	public static final RelationshipType SWITCH = RelationshipType.withName("switch");
	public static final RelationshipType PARENTHESIZE = RelationshipType.withName("parenthesize");
	public static final RelationshipType CREATED_BY = RelationshipType.withName("createdBy");
	public static final RelationshipType DOMAIN = RelationshipType.withName("domain");
	public static final RelationshipType BINDED_TYPE = RelationshipType.withName("bindedType");
	public static final RelationshipType BINDED_METHOD = RelationshipType.withName("bindedMethod");
	public static final RelationshipType ANONYMOUS_CLASS_DECLARATION = RelationshipType.withName("anonymousClassDeclaration");

    //属性类型
    public static final String NAME = "name";
    public static final String FULLNAME = "fullName";
    public static final String LABEL = "label";
    public static final String IS_INTERFACE = "isInterface";
    public static final String VISIBILITY = "visibility";
    public static final String IS_ABSTRACT = "isAbstract";
    public static final String IS_FINAL = "isFinal";
	public static final String IS_VOLATILE = "isVolatile";
	public static final String IS_TRANSIENT = "isTransient";
    public static final String COMMENT = "comment";
    public static final String CONTENT = "content";
    public static final String RETURN_TYPE_STR = "returnType";
    public static final String VAR_TYPE_STR = "varialbleType";
    public static final String PARAM_TYPE_STR = "paramentType";
    public static final String IS_CONSTRUCTOR = "isConstructor";
    public static final String IS_STATIC = "isStatic";
    public static final String IS_SYNCHRONIZED = "isSynchronized";
    public static final String SIMPLENAME_TYPE = "simpleNameType";
    public static final String STATEMENT_TYPE = "statementType";
    public static final String METHOD_NAME = "mehtodName";
    public static final String STATEMENT_NO = "statementNo";
    public static final String IF_CONDITION_NO = "ifConditionNo";
    public static final String EXPRESSION_TYPE = "expressionType";
    public static final String IS_BLOCK_ELSE = "isBlockElse";
	public static final String IS_DEFAULT = "isDefault";
	public static final String DECLARED_TYPE = "declaredType";
	public static final String ARRAY_TYPE = "arrayType";
	public static final String DIMENSION_NUM = "dimensionNum";
	public static final String ELEMENT_TYPE = "elementType";
	public static final String COMPONENT_TYPE = "componentType";
	public static final String OPERATOR_TYPE = "operatorType";
	public static final String OPERATOR_LITERAL = "operatorLiteral";
	public static final String ROW_NO = "rowNo";
	public static final String QUALIFIER = "qualifier";
	public static final String TYPE_ARG_TYPE_NAME = "typeArgmentType";
	public static final String METHOD_TYPE = "methodType";
	public static final String IDENTIFIER = "identifier";
	public static final String IS_VARIABLE_ARITY_METHOD_ARG = "isVarargs";
	public static final String TYPE_NAME = "typeName";
	public static final String IS_RESOLVED_TYPE_INFERRED_FROM_EXPECTED_TYPE = "isResolvedTypeInferredFromExpectedType";
	public static final String TYPE_ARG_DECLARED_TYPE = "typeArgDeclaredType";
	public static final String EXTRA_DIMENSION_NUM = "extraDimensionNum";
	public static final String CONST_EXPR_VALUE = "contantExpressionValue";
	public static final String SUPER_CLASS = "superClass";
	
    @Override
    public boolean isBatchInsert() {
        return true;
    }

    @Override
    public void extraction() {

    	//新建项目信息对象
        JavaProjectInfo javaProjectInfo = new JavaProjectInfo();
        Collection<File> javaFiles = FileUtils.listFiles(new File(this.getDataDir()), new String[]{"java"}, true);
        Set<String> srcPathSet = new HashSet<>();
        Set<String> srcFolderSet = new HashSet<>();
        for (File javaFile : javaFiles) {
            String srcPath = javaFile.getAbsolutePath();
            String srcFolderPath = javaFile.getParentFile().getAbsolutePath();
            srcPathSet.add(srcPath);
            srcFolderSet.add(srcFolderPath);
        }
        String[] srcPaths = new String[srcPathSet.size()];
        srcPathSet.toArray(srcPaths);

        String[] srcFolderPaths = new String[srcFolderSet.size()];
        srcFolderSet.toArray(srcFolderPaths);

        BatchInserter inserter = this.getInserter();

        ASTParser parser = ASTParser.newParser(AST.JLS10);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        parser.setEnvironment(null, new String[]{this.getDataDir()}, new String[]{"utf-8"}, true);
        Map<String, String> options = JavaCore.getOptions();
        options.put("org.eclipse.jdt.core.compiler.source", "1.8");
        parser.setCompilerOptions(options);
        String[] encodings = new String[srcPaths.length];
        for (int i = 0; i < srcPaths.length; i++)
            encodings[i] = "utf-8";
        //对每一个Java源代码文件创建Java语法树
        parser.createASTs(srcPaths, encodings, new String[]{}, new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit javaUnit) {
                try {
                    log.debug("AST parsing: " + sourceFilePath);
                    //连接语法树结点(java源文件）对应的ASTVisitor
                    javaUnit.accept(new JavaASTVisitor(javaProjectInfo, FileUtils.readFileToString(new File(sourceFilePath), "utf-8"), inserter));
                    javaUnit.accept(new JavaStatementVisitor(javaProjectInfo, FileUtils.readFileToString(new File(sourceFilePath), "utf-8"), inserter));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, null);
       
        //解析依赖并创立数据库结点和关系
        javaProjectInfo.parseRels(this.getInserter());
    }

}


