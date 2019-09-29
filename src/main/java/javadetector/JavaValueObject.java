package main.java.javadetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class JavaValueObject  extends JavaDetector{
	private Neo4jDriver dbDriver;
	private static final String type = "compareTo() and equals() should be consistent此处无equals()的重构， hashCode() and equals() must always be consistent此处无hashCode重构";
	private static final String defectPattern = "MATCH (n:Class)-[:haveMethod]-(m:Method{name:'compareTo'}) " + 
			"MATCH (w:Method{name:'equals'}) WHERE NOT (n)-[:haveMethod]-(w)  "+
			"MATCH (y:Method{name:'hashCode'}) WHERE NOT (n)-[:haveMethod]-(y)"+
			" return m.belongTo, m.rowNo" ;
			

	public JavaValueObject() {
		dbDriver = super.getDbDriver();
	}
	
	/**检测不含default的switch语句
	 * @author 柳沿河
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
