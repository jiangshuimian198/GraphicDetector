package main.java.detector;

import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class UnreliableEquivalentComparison extends Detector{
	private Neo4jDriver dbDriver;
	private static final String type = "不可靠的等值判断：equals()参数可能非字符串对象";
	private static final String defectPattern = "MATCH (n:Statement) "
			+ "WHERE n.statementType=\"SwitchStatement\" AND n.haveDefaultCase=false "
			+ "return n.belongTo, n.rowNo";

	public UnreliableEquivalentComparison() {
		dbDriver = super.getDbDriver();
	}
	
	/**检测含有不可靠字符串等值判断的表达式
	 * @author 柳沿河
	 * @return 含有缺陷信息的Map对象
	 */
	@Override
	public void detect(Map<String, Object> map){
		Result result = dbDriver.query(defectPattern, map);
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
	}

}
