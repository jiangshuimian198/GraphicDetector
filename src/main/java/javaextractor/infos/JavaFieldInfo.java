package main.java.javaextractor.infos;


import com.google.common.base.Preconditions;
import lombok.Getter;
import main.java.javaextractor.JavaExtractor;

import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;
import java.util.Map;

public class JavaFieldInfo {

    @Getter
    private String name;
    @Getter
    private String fullName;
    @Getter
    private String type;
    @Getter
    private String visibility;
    @Getter
    private boolean isStatic;
    @Getter
    private boolean isFinal;
    @Getter
    private String comment;
    @Getter
    private int rowNo;

    @Getter
    private String belongTo;
    @Getter
    private String fullType;
    @Getter
    private long nodeId;

    public JavaFieldInfo(BatchInserter inserter, String name, String fullName, String type, String visibility, boolean isStatic, boolean isFinal, String comment, int rowNo, String belongTo, String fullType) {
        Preconditions.checkArgument(name != null);
        this.name = name;
        Preconditions.checkArgument(fullName != null);
        this.fullName = fullName;
        Preconditions.checkArgument(type != null);
        this.type = type;
        Preconditions.checkArgument(visibility != null);
        this.visibility = visibility;
        this.isStatic = isStatic;
        this.isFinal = isFinal;
        Preconditions.checkArgument(comment != null);
        this.comment = comment;
        this.rowNo = rowNo;
        Preconditions.checkArgument(belongTo != null);
        this.belongTo = belongTo;
        Preconditions.checkArgument(fullType != null);
        this.fullType = fullType;
        nodeId = createNode(inserter);
        this.comment = null;
    }

    private long createNode(BatchInserter inserter) {
        Map<String, Object> map = new HashMap<>();
        map.put(JavaExtractor.NAME, name);
        map.put(JavaExtractor.FULLNAME, fullName);
        map.put(JavaExtractor.BELONG_TO, belongTo);
        map.put(JavaExtractor.VAR_TYPE_STR, type);
        map.put(JavaExtractor.VISIBILITY, visibility);
        map.put(JavaExtractor.IS_STATIC, isStatic);
        map.put(JavaExtractor.IS_FINAL, isFinal);
        map.put(JavaExtractor.COMMENT, comment);
        map.put(JavaExtractor.ROW_NO, rowNo);
        return inserter.createNode(map, JavaExtractor.FIELD);
    }

}