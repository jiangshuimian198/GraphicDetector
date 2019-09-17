package main.java.detector;

import java.io.File;
import java.util.Map;

import lombok.Getter;
import main.java.driver.Neo4jDriver;

public class Detector {
	@Getter
	private Neo4jDriver dbDriver;
	
	public Detector(String dbFilePath) {
		dbDriver = new Neo4jDriver(new File(dbFilePath));
	}
	
	protected void finalize()
	{
		dbDriver.registerShutdownHook();
	} 
	
	/**子类需实现的方法
	 * @author 柳沿河
	 * @return 含有缺陷信息的Map对象
	 */
	public Map<String, Object> detect(){
		return null;
	}
	
	/**
	 * @author 柳沿河
	 * @param map：存放缺陷信息的映射
	 * @param type：缺陷类型描述
	 */
	public void putDefectType(Map<String, Object> map, Object type)
	{
		map.put("type", type);
	}
	
	/**
	 * @author 柳沿河
	 * @param map：存放缺陷信息的映射
	 * @param location：缺陷位置信息，精良精确到语句或表达式，格式为'所在方法全名'+':'+'代码行号'，语句块的缺陷填第一条语句所在行号
	 */
	public void putDefectLocation(Map<String, Object> map, Object location)
	{
		map.put("location", location);
	}
	
	/**
	 * @author 柳沿河
	 * @param map：存放缺陷信息的映射
	 * @param prop：其他缺陷的属性，如严重程度、优先级等，后续更新
	 */
	public void putDefectProperties(Map<String, Object> map, String propKey, Object propVal)
	{
		map.put(propKey, propVal);
	}
}
