package main.java.javadetector;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.graphdb.Result;
import main.java.driver.Neo4jDriver;

public class JavaUnsafeNullEqualsPreJava7 extends JavaDetector{

	private Neo4jDriver dbDriver;
	// 由于主要采取匹配错误模式的方法，因此这里仅提示有可能是缺陷
	private static final String type = "[提示] 可能出现空指针异常：使用“a.equals(b)”方法请确认之前有对“a==null || b==null”的检查。此方法多适用于Java7之前的版本，Java之后的版本建议改为“Objects.equals()”";
	private static final String defectPattern = "MATCH(e:Expression{methodName:\"equals\"})-[:invocatedBy]->(n:Expression) "
			+ "WHERE n.content <> \"Objects\" "
			+ "RETURN e.belongTo,e.rowNo"; 
	
	public JavaUnsafeNullEqualsPreJava7() {
		dbDriver = super.getDbDriver();
	}
	
	/**
	 * 提示使用.equals方法前检查其调用对象和参数是null的情况
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


