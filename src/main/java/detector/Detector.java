package main.java.detector;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import main.java.driver.Neo4jDriver;

public class Detector {
	@Getter
	private Neo4jDriver dbDriver;
	private static String dbFilePath;
	
	public Detector() {
		dbDriver = new Neo4jDriver(new File(dbFilePath));
	}
	
	public Detector(String dbFilePath) {
		Detector.dbFilePath = dbFilePath;
	}
	
	/**执行各个检测
	 * @author 柳沿河
	 * @param dbFilePath：数据库文件路径
	 */
	public static void exec(String dbFilePath) {
		//Demo
		List<Map<String, Object>> mapList = new LinkedList<>();
		Detector detector = new Detector(dbFilePath);
		detector = new UnsafeDateFormat();
		mapList.add(detector.detect());
		
		detector = new UnhandledCase();
		mapList.add(detector.detect());
		
		detector = new UnreliableEquivalentComparison();
		mapList.add(detector.detect());
		
		detector = new UnsafeResourceReallocation();
		mapList.add(detector.detect());
		
		Iterator<Map<String, Object>> it = mapList.iterator();
		while(it.hasNext()) {
			Map<String, Object> curMap = it.next();
			if(!curMap.isEmpty()) {
				for(String key : curMap.keySet())
					System.out.println(key+":"+curMap.get(key));
			}
		}
	}
	
	protected void shutdown()
	{
		this.dbDriver.shutdown();
	} 
	
	/**子类需实现的方法
	 * @author 柳沿河
	 * @return 含有缺陷信息的Map对象
	 */
	public Map<String, Object> detect(){
		return null;
	}
	
	/**添加缺陷类型信息
	 * @author 柳沿河
	 * @param map：存放缺陷信息的映射
	 * @param type：缺陷类型信息
	 */
	public void putDefectType(Map<String, Object> map, Object type)
	{
		map.put("type", type);
	}
	
	/**添加缺陷位置信息
	 * @author 柳沿河
	 * @param map：存放缺陷信息的映射
	 * @param location：缺陷位置信息
	 */
	public void putDefectLocation(Map<String, Object> map, Object location)
	{
		if(location instanceof String)
			map.put("location", location);
		else if(location instanceof Integer)
			map.put("rowNo", location);
	}
	
	/**添加其他缺陷属性信息
	 * @author 柳沿河
	 * @param map：存放缺陷信息的映射
	 * @param prop：其他缺陷的属性，如严重程度、优先级等，后续更新
	 */
	public void putDefectProperties(Map<String, Object> map, String propKey, Object propVal)
	{
		map.put(propKey, propVal);
	}
}
