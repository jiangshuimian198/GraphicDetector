package main.java.javadetector;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.graphdb.Result;
import main.java.driver.Neo4jDriver;

public class JavaUnsafeNullCompare extends JavaDetector{
	private Neo4jDriver dbDriver;
	// 由于主要采取匹配错误模式的方法，因此这里仅提示有可能是缺陷
	private static final String type = "[提示] 可能出现空指针异常：请检查compareTo方法的调用对象和参数是否为空";
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
	private static final String defectPattern = "MATCH(e:Expression{methodName:\"compareTo\"}) "
=======
	private static final String defectPattern = "MATCH(e:Expression{methodName:'compareTo'}) "
>>>>>>> parent of 2903fac... Revert "v1.0"
=======
	private static final String defectPattern = "MATCH(e:Expression{methodName:'compareTo'}) "
>>>>>>> parent of 2903fac... Revert "v1.0"
=======
	private static final String defectPattern = "MATCH(e:Expression{methodName:\"compareTo\"}) "
>>>>>>> parent of cdf4fe8... ...
			+ "RETURN e.belongTo,e.rowNo"; 
	
	public JavaUnsafeNullCompare() {
		dbDriver = super.getDbDriver();
	}
	
	/**
	 * 提示使用.conpareTo方法前检查其调用对象和参数是null的情况
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


