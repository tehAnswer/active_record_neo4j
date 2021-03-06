package finder;

import java.util.Map;
import java.util.Set;

/**
 * Created by Sergio on 2/4/15.
 */
public interface Finder<T> {

    T find(long neoId);
    Set<T> where (String key, Object value);
    Set<T> where (Map<String, Object> hash);
    T destroy();
    boolean save();
    boolean isDirty();
    boolean isNew();

}
