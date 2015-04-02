package example;

import example.model.Person;
import finder.impl.RecordFinder;
import org.neo4j.cypher.internal.compiler.v2_0.functions.Str;
import org.neo4j.graphdb.*;
import util.ConnectionFactory;
import util.RandomStringGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Sergio on 2/4/15.
 */
public class Main {

    private static int LIMIT = 400000;
    private static String NAME = "";
    private static long NEO_ID = Long.valueOf(String.valueOf(new Random().nextInt(LIMIT)));


    public static void main(String[] args) throws Exception {

        GraphDatabaseService connection = ConnectionFactory.getConnection();
        connection.execute("CREATE INDEX ON :Person(name)");
        createRecords(connection);
        RecordFinder<Person> personFinder = new RecordFinder(Person.class);

        System.out.println("\nFind [" + NEO_ID + "] (Neo4j powered)...");
        System.out.println(personFinder.find(NEO_ID));
        System.out.println("\nFind [" + NEO_ID + "] (Sergio hyper-deprecated)...");
        System.out.println(personFinder.findDeprecated(NEO_ID));



        System.out.println("\nFind [" + NAME + "] (Neo4j...)");
        System.out.println(personFinder.where("name", NAME));


        Map<String, Object> hash = new HashMap<String, Object>();
        hash.put("name", NAME);

        System.out.println("\nFind [" + NAME + "] (Sergio...)");
        System.out.println(personFinder.where(hash));




    }

    private static void createRecords(GraphDatabaseService connection) throws Exception {
        Transaction tx = connection.beginTx();
        for (int i = 0; i < LIMIT; i++) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", RandomStringGenerator.generate(5, RandomStringGenerator.Mode.ALPHA));
            NAME = params.get("name").toString();
            params.put("lastName", RandomStringGenerator.generate(8, RandomStringGenerator.Mode.ALPHA));
            params.put("age", Long.valueOf(RandomStringGenerator.generate(2, RandomStringGenerator.Mode.NUMERIC)));
            Result rs = connection.execute("create(n:Person {name:{name}, lastName: {lastName}, age: {age} }) return id(n) as neoId", params);

        }
        tx.success();
        System.out.println("Created " + LIMIT + " nodes");
    }


}
