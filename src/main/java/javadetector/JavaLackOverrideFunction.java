package main.java.javadetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class JavaLackOverrideFunction extends JavaDetector{
	private Neo4jDriver dbDriver;
	private static final String type = "此类覆盖compareTo()但未覆盖equals()或未覆盖hashCode()";
	private static final String defectPattern = "MATCH (n:Class),(m:Method{name:'compareTo'}),(w:Method{name:'equals'}),(x:Method{name:'hashCode'}) "
			+"WHERE (n)-[:haveMethod]->(m) AND NOT (n)-[:haveMethod]->(w) AND NOT (n)-[:haveMethod]->(x)"
			+ "RETURN n.belongTo, n.rowNo";

	public JavaLackOverrideFunction() {
		dbDriver = super.getDbDriver();
	}
	
	/**检测覆盖compareTo()但未覆盖equals()或未覆盖hashCode()的类
	 * @author 孙天琪
	 * @return 含有缺陷信息的Map对象
	 */
	@Override
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
