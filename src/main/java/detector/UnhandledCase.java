package main.java.detector;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class UnhandledCase extends Detector{
	private Neo4jDriver dbDriver;
	private static final String type = "不完整的switch语句：无default case";
	private static final String defectPattern = "MATCH (n:Statement) "
			+ "WHERE n.statementType=\"SwitchStatement\" AND n.haveDefaultCase=false "
			+ "return n.belongTo, n.rowNo";

	public UnhandledCase() {
		dbDriver = super.getDbDriver();
	}
	
	/**检测不含default的switch语句
	 * @author 柳沿河
	 * @return 含有缺陷信息的Map对象
	 */
	@Override
	public Map<String, Object> detect(){
		Map<String, Object> map = new HashMap<>();
		Result result = dbDriver.query(defectPattern, new HashMap<>());
		if(result != null && result.hasNext()) {
			putDefectType(map, type);
			while(result.hasNext()) {
				Map<String, Object> row = result.next();
				for ( String key : result.columns() ){
					putDefectLocation(map, row.get(key));
				}
			}
		}
		shutdown();
		return map;
	}
}
