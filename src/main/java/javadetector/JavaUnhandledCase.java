package main.java.javadetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class JavaUnhandledCase extends JavaDetector{
	private Neo4jDriver dbDriver;
	private static final String type = "不完整的switch语句：无default case";
	private static final String defectPattern = "MATCH (n:Statement{statementType:'SwitchStatement', haveDefaultCase:false}) "
			+ "RETURN n.belongTo, n.rowNo";

	public JavaUnhandledCase() {
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
