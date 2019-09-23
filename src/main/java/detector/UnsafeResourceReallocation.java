package main.java.detector;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class UnsafeResourceReallocation extends Detector{
	private Neo4jDriver dbDriver;
	private static final String type = "不安全的资源释放：同一try语句中多个资源释放，这可能导致后面的释放不成功";
	private static final String defectPattern = "MATCH p=(t:Statement{statementType:'TryStatement'})-[*]->(e:Expression{expressionType:'MethodInvocation', methodName:'close'}) "
			+ "WITH last(collect(t)) AS try, COUNT(e) AS num "
			+ "WHERE num>1 "
			+ "RETURN try.belongTo, try.rowNo";

	public UnsafeResourceReallocation() {
		dbDriver = super.getDbDriver();
	}
		
	/**检测不安全的资源释放
	 * @author 柳沿河
	 * @return 含有缺陷信息的Map对象
	 */
	@Override
	public Map<String, Object> detect(){
		//执行流程：
		//1.调用dbDriver对象query方法执行cypher语句并获得结果
		//2.调用父类putDefectxxx方法向Map对象中添加缺陷信息
		//3.关闭数据库连接
		//4.返回map对象
		Map<String, Object> map = new HashMap<>();
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
		return map;
	}

}
