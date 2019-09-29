package main.java.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class UnsafeDateFormat extends Detector{
	private Neo4jDriver dbDriver;
	private static final String type = "线程不安全的DateFormat成员声明：静态常量";
	private static final String defectPattern = "MATCH (n:Field{isStatic:true, isFinal:true, variableType:'DateFormat'}) "
			+ "RETURN n.belongTo, n.rowNo";

	public UnsafeDateFormat() {
		dbDriver = super.getDbDriver();
	}
		
	/**检测不安全的DateFormat成员声明
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
