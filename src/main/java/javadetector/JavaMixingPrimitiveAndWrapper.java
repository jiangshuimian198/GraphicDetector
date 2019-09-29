package main.java.javadetector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;
 
public class JavaMixingPrimitiveAndWrapper extends JavaDetector{
	//不考虑调用a.valueOf().bValue()中a和b是否相等，直接推荐使用parseB方法
	private Neo4jDriver dbDriver;
	private static final String type = "三元运算符中混合原语和包装器类型会导致包装器类型的拆箱和立即重新装箱";
	private static final String defectPattern = "MATCH (exp:Expression{expressionType:'ConditionalExpression'})-[:then|:else]->(primitiveNode:Expression{expressionType:'NumberLiteral'}),(exp)-[:then|:else]->(wrapperNode:Expression)"
			+ " WHERE wrapperNode.expressionType <> 'NumberLiteral'"
			+ " RETURN exp.belongTo,exp.rowNo";

	public JavaMixingPrimitiveAndWrapper() {
		dbDriver = super.getDbDriver();
	}
	
	/**
	 * 检测三元运算符中混合原语和包装器类型会导致包装器类型的拆箱和立即重新装箱`
	 * @author 丁婧伊
	 * @return 含有缺陷信息的Map对象
	 */
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

