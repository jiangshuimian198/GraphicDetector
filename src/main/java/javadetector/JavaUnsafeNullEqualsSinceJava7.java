package main.java.javadetector;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.graphdb.Result;
import main.java.driver.Neo4jDriver;

public class JavaUnsafeNullEqualsSinceJava7 extends JavaDetector{

	private Neo4jDriver dbDriver;
	private static final String type = "[提示] 版本问题：“Objects.equals()”方法仅适用于Java7及之后的版本";
	private static final String defectPattern = "MATCH(exp:Expression{expressionType:'MethodInvocation', methodName:'equals'})-[:invocatedBy]->(obj:Expression{content:'Objects'}) "
			+ "RETURN exp.belongTo, exp.rowNo";
	
	public JavaUnsafeNullEqualsSinceJava7() {
		dbDriver = super.getDbDriver();
	}
	
	/**
	 * 提示Java7之后才能用Objects.equals(o1,o2)方法
	 * @author 丁婧伊
	 * @return 含有缺陷信息的Map对象
	 */
	public List<Map<String, Object>> detect(){
		List<Map<String, Object>> mapList = new ArrayList<>();
		Result result = dbDriver.query(defectPattern, new HashMap<>());
		if(result != null && result.hasNext()) {
			while(result.hasNext()) {
				Map<String, Object> map = new HashMap<>();
				putDefectType(map, type);
				Map<String, Object> row = result.next();
				for ( String key : result.columns() ){
					putDefectLocation(map, row.get(key));
				}
				mapList.add(map);
			}
		}
		shutdown();
		return mapList;
	}
}


