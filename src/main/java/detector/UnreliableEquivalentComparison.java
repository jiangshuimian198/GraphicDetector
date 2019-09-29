package main.java.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class UnreliableEquivalentComparison extends Detector{
	private Neo4jDriver dbDriver;
	private static final String type = "不可靠的等值判断：equals()参数可能非字符串对象，建议对参数String.valueOf()处理";
	private static final String defectPattern = "MATCH p=(a)<-[:haveArgument]-(e:Expression{methodName:'equals'})-[:invocatedBy]->(s{declaredType:'String'}) "
			+ "WHERE NOT a.content=~'String.valueof(.*)' "
			+ "RETURN e.belongTo, e.rowNo";

	public UnreliableEquivalentComparison() {
		dbDriver = super.getDbDriver();
	}
	
	/**检测含有不可靠字符串等值判断的表达式
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
