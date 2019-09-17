package main.java.detector;

import java.util.Map;

import main.java.driver.Neo4jDriver;

public class SubDetectorExample extends Detector{
	private Neo4jDriver dbDriver;

	public SubDetectorExample(String dbFilePath) {
		super(dbFilePath);
		// TODO Auto-generated constructor stub
		dbDriver = super.getDbDriver();
	}
		
	/**继承父类的方法。此处注明函数功能
	 * @author 此处注明作者
	 * @return 含有缺陷信息的Map对象
	 */
	@Override
	public Map<String, Object> detect(){
		//执行流程：
		//1.调用dbDriver对象query方法执行cypher语句并获得结果
		//2.调用父类putDefectxxx方法向Map对象中添加缺陷信息
		//3.返回Map对象
		return null;
	}

}
