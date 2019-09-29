package main.java.javadetector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Result;

import main.java.driver.Neo4jDriver;
 
public class JavaUnnecessaryBoxingOfNumber extends JavaDetector{
	//不考虑调用a.valueOf().bValue()中a和b是否相等，直接推荐使用parseB方法
	private Neo4jDriver dbDriver;
	private static final String type = "不必要的拆箱：建议使用XXX.parseXXX()方法，如Integer.parseInt()";
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
	private static final String defectPattern = "MATCH (exp:Expression)-[:invocatedBy]->(exp2:Expression{methodName:\"valueOf\"})-[:invocatedBy]->(exp3:Expression)"
=======
	private static final String defectPattern = "MATCH (exp:Expression)-[:invocatedBy]->(exp2:Expression{methodName:'valueOf'})-[:invocatedBy]->(exp3:Expression)"
>>>>>>> parent of 2903fac... Revert "v1.0"
=======
	private static final String defectPattern = "MATCH (exp:Expression)-[:invocatedBy]->(exp2:Expression{methodName:'valueOf'})-[:invocatedBy]->(exp3:Expression)"
>>>>>>> parent of 2903fac... Revert "v1.0"
=======
	private static final String defectPattern = "MATCH (exp:Expression)-[:invocatedBy]->(exp2:Expression{methodName:\"valueOf\"})-[:invocatedBy]->(exp3:Expression)"
>>>>>>> parent of cdf4fe8... ...
			+ " WHERE exp.methodName IN ['longValue','byteValue','doubleValue','floatValue','intValue','shortValue']"
			+ " AND exp3.content IN ['Long','Byte','Double','Float','Integer','Short']"
			+ " RETURN exp.belongTo,exp.rowNo";

	public JavaUnnecessaryBoxingOfNumber() {
		dbDriver = super.getDbDriver();
	}
	
	/**
	 * 检测Number子类如Integer使用形如Integer.valueOf(number).intValue()，造成不必要的拆箱，可以改为Integer.parseInt()
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

