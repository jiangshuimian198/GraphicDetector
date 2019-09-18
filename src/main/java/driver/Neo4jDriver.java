package main.java.driver;

import java.io.File;
import java.util.Map;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.*;

public class Neo4jDriver {
	private GraphDatabaseService graphDb;
	
	public Neo4jDriver(File dataBaseFile) {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dataBaseFile);
	}
	
	public Result query(String query, Map<String, Object> parameters) {
		Result result = graphDb.execute(query, parameters);
		return result;
	}
	
	public void registerShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                graphDb.shutdown();
            }
        });
    }
}
