package main.java.javadetector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;


public class JavaUnsafeExceptionPrinter extends JavaDetector {
	private Neo4jDriver dbDriver;
	private static final String type = "三元运算符缺少括号会导致意外的日志消息";
	private static final String defectPattern = "MATCH (n:Expression{expressionType:'InfixExpression'}) "
			+ "WHERE n.content=~'.*\\\\(.*\\\\?.*:.*\\\\)'"
			+ "RETURN n.content, n.belongTo, n.rowNo";

	public JavaUnsafeExceptionPrinter() {
		dbDriver = super.getDbDriver();
	}
		
	/**三元运算符缺少括号会导致意外的日志消息
	 * @author 谢佳锋
	 * @return 含有缺陷信息的Map对象
	 */
	@Override
	public List<Map<String, Object>> detect(){
		//执行流程：
		//1.调用dbDriver对象query方法执行cypher语句并获得结果
		//2.调用父类putDefectxxx方法向Map对象中添加缺陷信息
		//3.关闭数据库连接
		List<Map<String, Object>> mapList = new LinkedList<>();
		Result result = dbDriver.query(defectPattern, new HashMap<>());
		if(result != null && result.hasNext()) {
			while(result.hasNext()) {
				Map<String, Object> map = new HashMap<>();
				Map<String, Object> row = result.next();
				putDefectType(map, type);
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