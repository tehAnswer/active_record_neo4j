package finder.impl;

import finder.Finder;
import org.neo4j.graphdb.*;
import util.ConnectionFactory;
import java.lang.StringBuilder;

import java.lang.reflect.Method;
import java.util.HashMap;
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

    public Set<T> where(Map hash) {
        return null;
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

    private T createInstance(Node node) throws Exception {
        long before = System.currentTimeMillis();
        T ret = clazz.newInstance();
        for (String property : node.getPropertyKeys()) {
            String methodName = this.getSetterMethodName(property);
            Object value = node.getProperty(property);

            if (this.respondTo(methodName, value.getClass())) {
                new Statement(ret, methodName, new Object[]{ value }).execute();
            }
        }
        long after = System.currentTimeMillis();
        System.out.println("Creating data... " + String.valueOf(after - before) + "ms");
        return ret;

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
}
