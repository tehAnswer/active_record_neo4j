package finder.impl;

import finder.Finder;
import org.neo4j.graphdb.*;
import util.ConnectionFactory;
import java.lang.StringBuilder;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.beans.Statement;

/**
 * Created by Sergio on 2/4/15.
 */
public class RecordFinder<T> implements Finder {

    private final Class<T> clazz;

    public RecordFinder(Class<T> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("deprecated")
    @Deprecated
    public T findDeprecated(long neoId) {
        GraphDatabaseService con = ConnectionFactory.getConnection();
        Transaction tx = con.beginTx();
        T ret = null;

        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("neo_id", neoId);
            long before = System.currentTimeMillis();
            Result rs = con.execute("MATCH (n) where id(n) = {neo_id} return n", params);
            long after = System.currentTimeMillis();
            System.out.println("Querying... " + String.valueOf(after - before) + "ms");
            ResourceIterator<Node> it = rs.columnAs("n");
            if(it.hasNext()) {
                ret = this.createInstance(it.next());
            }
            rs.close();
            it.close();
            tx.success();
            return ret;
        } catch (Exception exc) {
            tx.failure();
            tx.terminate();
            throw new RuntimeException("Unexpected error: " + exc.getMessage());
        }
    }

    public T find(long neoId) {
        try {
            GraphDatabaseService con = ConnectionFactory.getConnection();
            Transaction tx = con.beginTx();
            long before = System.currentTimeMillis();
            Node node = con.getNodeById(neoId);
            long after = System.currentTimeMillis();
            System.out.println("Querying... " + String.valueOf(after - before) + "ms");
            T ret = this.createInstance(node);
            tx.success();
            return ret;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }

    public Set<T> where(String key, Object value) {
        GraphDatabaseService connection = ConnectionFactory.getConnection();
        Label label = DynamicLabel.label(clazz.getSimpleName());
        Transaction tx = connection.beginTx();
        long before = System.currentTimeMillis();
        ResourceIterator<Node> nodes = connection.findNodes(label, key, value);
        long after = System.currentTimeMillis();
        System.out.println("Querying... " + String.valueOf(after - before) + "ms");
        tx.success();
        return createSetOfObjects(nodes);
    }

    public Set<T> where(Map hash) {
        GraphDatabaseService connection = ConnectionFactory.getConnection();
        Label label = DynamicLabel.label(clazz.getSimpleName());
        Transaction tx = connection.beginTx();
        long before = System.currentTimeMillis();
        Result rs = connection.execute("MATCH (n:Person) USING INDEX n:Person(name) " + this.whereStatement(hash) + " return n", hash);
        long after = System.currentTimeMillis();
        System.out.println("Querying... " + String.valueOf(after - before) + "ms");
        ResourceIterator<Node> nodes = rs.columnAs("n");
        return this.createSetOfObjects(nodes);
    }

    private String whereStatement(Map<String, Object> hash) {
        StringBuilder sb = new StringBuilder("where ");
        for (String key : hash.keySet()) {
            if(sb.length() != 6 )
                sb.append(" and ");
            sb.append("n.");
            sb.append (key).append(" = {").append(key).append("}");
        }
        return sb.toString();
    }

    public T destroy() {
        return null;
    }

    public boolean save() {
        return false;
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isNew() {
        return false;
    }

    private String getLabel () {
        return clazz.getSimpleName();
    }

    private T createInstance(Node node){
        try {
            long before = System.currentTimeMillis();
            T ret = clazz.newInstance();
            for (String property : node.getPropertyKeys()) {
                String methodName = this.getSetterMethodName(property);
                Object value = node.getProperty(property);

                if (this.respondTo(methodName, value.getClass())) {
                    new Statement(ret, methodName, new Object[]{value}).execute();
                }
            }
            long after = System.currentTimeMillis();
            System.out.println("Creating data... " + String.valueOf(after - before) + "ms");
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSetterMethodName(String key) {
        return "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
    }

    private boolean respondTo (String methodName, Class klass) {
        try {
            Method method = clazz.getMethod(methodName, klass);
            return method != null;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }

    private Set<T> createSetOfObjects(ResourceIterator<Node> nodes) {
        Set<T> ret = new HashSet<T>() ;
        while (nodes.hasNext()) {
            Node node = nodes.next();
            T object = this.createInstance(node);
            ret.add(object);
        }
        return ret;
    }
}
