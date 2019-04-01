package main.java.infos;

import com.google.common.base.Preconditions;
import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;

import org.neo4j.unsafe.batchinsert.BatchInserter;

import java.util.HashMap;
import java.util.Map;

public class JavaPackageInfo {
	
	@Getter
    private String name;
	//@Getter
    //private String superPackage;
	@Getter
    private long nodeId;
	
	
	public JavaPackageInfo(BatchInserter inserter, String name) {
		// TODO Auto-generated constructor stub
		Preconditions.checkArgument(name != null);
        this.name = name;
        //Preconditions.checkArgument(superPackage != null);
        //this.superPackage = superPackage;
        nodeId = createNode(inserter);
	}

	private long createNode(BatchInserter inserter) {
        Map<String, Object> map = new HashMap<>();
        map.put(JavaExtractor.NAME, name);
        return inserter.createNode(map, JavaExtractor.PACKAGE);
    }

}
