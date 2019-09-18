package main.java.detector;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class UnsafeDateFormat extends Detector{
	private Neo4jDriver dbDriver;
	private static final String type = "线程不安全的DateFormat成员声明：静态常量";

	public UnsafeDateFormat(String dbFilePath) {
		super(dbFilePath);
		// TODO Auto-generated constructor stub
		dbDriver = super.getDbDriver();
	}
		
	/**检测不安全的DateFormat成员声明
	 * @author 柳沿河
	 * @return 含有缺陷信息的Map对象
	 */
	@Override
	public Map<String, Object> detect(){
		//执行流程：
		//1.调用dbDriver对象query方法执行cypher语句并获得结果
		//2.调用父类putDefectxxx方法向Map对象中添加缺陷信息
		//3.返回Map对象
		String query = "MATCH (n:Field) "
				+ "WHERE n.isStatic = true AND n.isFinal = true AND n.varialbleType = 'DateFormat' "
				+ "RETURN n.fullName";
		Map<String, Object> map = new HashMap<>();
		Result result = dbDriver.query(query, map);
		while(result.hasNext()) {
		Map<String, Object> row = result.next();
		for ( String key : result.columns() ){
			putDefectLocation(map, row.get(key));
		}
		}
		putDefectType(map, type);
		return map;
	}

}
