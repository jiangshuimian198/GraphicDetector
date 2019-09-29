package main.java.javadetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class JavaUnsafesmartComparator extends JavaDetector {
	private Neo4jDriver dbDriver;
	private static final String type = "简单对判断型整数取反可能会导致相同的错误";
	private static final String defectPattern = "MATCH (p:Expression)<-[:else]-(n:Expression{expressionType:'ConditionalExpression'})-[:then]->(m:Expression{expressionType:'PrefixExpression'}) "
			+ "WHERE m.content=~'-.*' "
			+ "RETURN n.belongTo, n.rowNo";

	public JavaUnsafesmartComparator() {
		dbDriver = super.getDbDriver();
	}
		
	/**检测不安全的DateFormat成员声明
	 * @author 谢佳锋
	 * @return 含有缺陷信息的Map对象
	 */
	@Override
	public List<Map<String, Object>> detect(){
		//执行流程：
		//1.调用dbDriver对象query方法执行cypher语句并获得结果
		//2.调用父类putDefectxxx方法向Map对象中添加缺陷信息
		//3.关闭数据库连接
		List<Map<String, Object>> mapList = new ArrayList<>();
		Result result = dbDriver.query(defectPattern, new HashMap<>());
		if(result != null && result.hasNext()) {
			while(result.hasNext()) {
				Map<String, Object> map = new HashMap<>();
				putDefectType(map, type);
				Map<String, Object> row = result.next();
				for ( String key : result.columns() ){
					//System.out.println(key);
					//System.out.println(row.get(key));
					putDefectLocation(map, row.get(key));
				}
				mapList.add(map);
			}
		}
		shutdown();
		return mapList;
		
	}

}
