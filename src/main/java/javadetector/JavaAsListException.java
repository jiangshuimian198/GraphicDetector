/**
 * 
 */
package main.java.javadetector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;
 
public class JavaAsListException extends JavaDetector{
	private Neo4jDriver dbDriver;
	private static final String type = "可能出现空指针异常，原因是：\"Arrays.asList(null)\"会导致一个空指针异常。建议改为\"Arrays.toString()\"，这样将安全地返回null";
<<<<<<< HEAD
	private static final String defectPattern = "MATCH(throwCode:Statement{statementType:\"ThrowStatement\"})-[*]->(asListCode:Expression{methodName:\"asList\"})-[:invocatedBy]->(arraysCode:Expression{content:\"Arrays\"}) "
=======
	private static final String defectPattern = "MATCH(throwCode:Statement{statementType:'ThrowStatement'})-[*]->(asListCode:Expression{methodName:'asList'})-[:invocatedBy]->(arraysCode:Expression{content:'Arrays'}) "
>>>>>>> parent of 2903fac... Revert "v1.0"
			+ "RETURN throwCode.belongTo,throwCode.rowNo";

	public JavaAsListException() {
		dbDriver = super.getDbDriver();
	}
	
	/**
	 * 检测throw语句中Arrays.asList()的参数是null导致空指针异常的缺陷
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

