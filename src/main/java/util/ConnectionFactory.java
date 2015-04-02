package util;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * Created by Sergio on 2/4/15.
 */
public class ConnectionFactory {
    private static String DB_PATH = "/Users/Sergio/Desktop/neo4j-community-2.2.0-x/data/graph.db";
    private static GraphDatabaseService connection = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

    public static GraphDatabaseService getConnection() {
        return connection;
    }


}
