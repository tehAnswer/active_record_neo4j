package example;

import example.model.Person;
import finder.impl.RecordFinder;
import org.neo4j.graphdb.*;
import util.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sergio on 2/4/15.
 */
public class Main {

    private static long NEO_ID = 1;
    private static int LIMIT = 100000;

    public static void main(String[] args) {
        System.out.println("Hello world.");
        GraphDatabaseService connection = ConnectionFactory.getConnection();
        createRecords(connection);
        RecordFinder<Person> personFinder = new RecordFinder(Person.class);
        System.out.println("\nFind deprecated...");
        System.out.println(personFinder.findDeprecated(NEO_ID));
        System.out.println("\nFind (Neo4j powered)...");
        System.out.println(personFinder.find(NEO_ID));
    }

    private static void createRecords(GraphDatabaseService connection) {
        Transaction tx = connection.beginTx();
        for (int i = 0; i < LIMIT; i++){
            Result rs = connection.execute("create(n:Person {name:\"Gol\", lastName: \"Senior\", age: 12}) return id(n) as neoId");
            NEO_ID = new Long(String.valueOf(rs.next().get("neoId")));
        }
        tx.success();
        System.out.println("Created " + LIMIT+ " nodes. Last NEO_ID: " + NEO_ID);
    }


}
