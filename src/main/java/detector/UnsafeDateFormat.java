package main.java.detector;

import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class UnsafeDateFormat extends Detector{
	private Neo4jDriver dbDriver;
	private static final String type = "线程不安全的DateFormat成员声明：静态常量";
	private static final String defectPattern = "MATCH (n:Field) "
			+ "WHERE n.isStatic = true AND n.isFinal = true AND n.varialbleType = 'DateFormat' "
			+ "RETURN n.belongTo, n.rowNo";

	public UnsafeDateFormat() {
		dbDriver = super.getDbDriver();
	}
		
	/**检测不安全的DateFormat成员声明
	 * @author 柳沿河
	 * @return 含有缺陷信息的Map对象
	 */
	@Override
	public void detect(Map<String, Object> map){
		//执行流程：
		//1.调用dbDriver对象query方法执行cypher语句并获得结果
		//2.调用父类putDefectxxx方法向Map对象中添加缺陷信息
		//3.关闭数据库连接
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
