package main.java.javaextractor;

import org.eclipse.jdt.core.dom.*;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import main.java.javaextractor.infos.JavaClassInfo;
import main.java.javaextractor.infos.JavaFieldInfo;
import main.java.javaextractor.infos.JavaPackageInfo;
import main.java.javaextractor.infos.JavaProjectInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class JavaASTVisitor extends ASTVisitor {

    private JavaProjectInfo javaProjectInfo;
    private String sourceContent;
    private BatchInserter inserter;
    private static ArrayList<String> visitedClassNodeList = new ArrayList<>();

    public JavaASTVisitor(JavaProjectInfo javaProjectInfo, String sourceContent, BatchInserter inserter) {
        this.javaProjectInfo = javaProjectInfo;
        this.sourceContent = sourceContent;
        this.inserter = inserter;
    }

    public static String getVisibility(int modifiers) {
        if (Modifier.isPrivate(modifiers))
            return "private";
        if (Modifier.isProtected(modifiers))
            return "protected";
        if (Modifier.isPublic(modifiers))
            return "public";
        return "package";
    }
    
    @Override
    public void endVisit(PackageDeclaration node)
    {
    	String name = node.getName().getFullyQualifiedName();
    	if(!visitedClassNodeList.contains(name))
    	{
    		JavaPackageInfo javaPackageInfo = new JavaPackageInfo(inserter,name);
    		visitedClassNodeList.add(name);
    		javaProjectInfo.addPackageInfo(javaPackageInfo);
    	}
    }
    
    @Override
    public boolean visit(TypeDeclaration node) {
        JavaClassInfo javaClassInfo = createJavaClassInfo(node);
        javaProjectInfo.addClassInfo(javaClassInfo);
        
        FieldDeclaration[] fieldDeclarations = node.getFields();
        for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
            List<JavaFieldInfo> javaFieldInfos = createJavaFieldInfos(fieldDeclaration, javaClassInfo.getFullName());
            for (JavaFieldInfo javaFieldInfo : javaFieldInfos)
                {
            		javaProjectInfo.addFieldInfo(javaFieldInfo);
                }
        }
 
        return false;
    }

	private JavaClassInfo createJavaClassInfo(TypeDeclaration node) {
        String name = node.getName().getFullyQualifiedName();
        String fullName;
        @SuppressWarnings("unchecked")
		List<TypeParameter> typeParameterList = node.typeParameters();
        if(!typeParameterList .isEmpty()) {
	        String typeParameters = "<";
	        for(TypeParameter element : typeParameterList)
	        {
	        	typeParameters+=element.toString()+",";
	        }
	        typeParameters = typeParameters.substring(0, typeParameters.length()-1)+">";
	        fullName = NameResolver.getFullName(node) + typeParameters;
        }
        else
        	fullName = NameResolver.getFullName(node);
        boolean isInterface = node.isInterface();
        String visibility = JavaASTVisitor.getVisibility(node.getModifiers());
        boolean isAbstract = Modifier.isAbstract(node.getModifiers());
        boolean isFinal = Modifier.isFinal(node.getModifiers());
        String comment = node.getJavadoc() == null ? "" : sourceContent.substring(node.getJavadoc().getStartPosition(), node.getJavadoc().getStartPosition() + node.getJavadoc().getLength());
        String content = sourceContent.substring(node.getStartPosition(), node.getStartPosition() + node.getLength());
        String superClassType = node.getSuperclassType() == null ? "java.lang.Object" : NameResolver.getFullName(node.getSuperclassType());
        @SuppressWarnings("unchecked")
		String superInterfaceTypes = String.join(", ", (List<String>) node.superInterfaceTypes().stream().map(n -> NameResolver.getFullName((Type) n)).collect(Collectors.toList()));
        JavaClassInfo classInfo = new JavaClassInfo(inserter, name, fullName, isInterface, visibility, isAbstract, isFinal, comment, content, superClassType, superInterfaceTypes);
        @SuppressWarnings("unchecked")
		List<BodyDeclaration> bodyDeclarationList = node.bodyDeclarations();
        for(BodyDeclaration element : bodyDeclarationList)
        {
        	if(element.getNodeType() == ASTNode.TYPE_DECLARATION)
        	{
        		TypeDeclaration innerClassDeclaration = (TypeDeclaration)element;
        		JavaClassInfo innerClassInfo = createJavaClassInfo(innerClassDeclaration);
        		inserter.createRelationship(classInfo.getNodeId(), innerClassInfo.getNodeId(), JavaExtractor.HAVE_CLASS, new HashMap<>());
        	}
        }
        return classInfo;
    }

    @SuppressWarnings("unchecked")
	private List<JavaFieldInfo> createJavaFieldInfos(FieldDeclaration node, String belongTo) {
        List<JavaFieldInfo> r = new ArrayList<>();
        String type = node.getType().toString();
        String fullType = NameResolver.getFullName(node.getType());
        String visibility = getVisibility(node.getModifiers());
        boolean isStatic = Modifier.isStatic(node.getModifiers());
        boolean isFinal = Modifier.isFinal(node.getModifiers());
        String comment = node.getJavadoc() == null ? "" : sourceContent.substring(node.getJavadoc().getStartPosition(), node.getJavadoc().getStartPosition() + node.getJavadoc().getLength());
        node.fragments().forEach(n -> {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) n;
            String name = fragment.getName().getFullyQualifiedName();
            String fullName = belongTo + "." + name;
            r.add(new JavaFieldInfo(inserter, name, fullName, type, visibility, isStatic, isFinal, comment, belongTo, fullType));
        });
        return r;
    }

}
