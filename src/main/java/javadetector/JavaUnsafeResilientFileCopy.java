package main.java.javadetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;

public class JavaUnsafeResilientFileCopy extends JavaDetector {
	private Neo4jDriver dbDriver;
	private static final String type = "使用&&运算导致有操作数可能无法执行";
	private static final String defectPattern = "MATCH (m:Expression{expressionType : 'MethodInvocation'})<-[:rightOperand]-(n:Expression)-[:infix]->(p:Operator)"
			+ "WHERE p.operatorLiteral='&&' "
			+ "RETURN n.belongTo, n.rowNo";

	public JavaUnsafeResilientFileCopy() {
		dbDriver = super.getDbDriver();
	}
		
	/**使用&&运算导致有操作数可能无法执行
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
