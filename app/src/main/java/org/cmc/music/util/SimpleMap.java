/*
 * Modified By Romulus U. Ts'ai
 * Copied from SimpleMap.java in SharedLib (http://www.fightingquaker.com/sharedlib/)
 * On Oct 6, 2008
 *
 * Removed all debug executions
 *
 */

package org.cmc.music.util;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


public class SimpleMap implements Map
//extends Hashtable
{
    private final Map map = new Hashtable();

    public SimpleMap() {
    }

    private boolean verbose_discarded = false;

    public void setVerboseDiscarded() {
        verbose_discarded = true;
    }

    public SimpleMap(Map other) {
        map.putAll(other);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    private Object simplifyKey(Object key) {
        if (key == null)
            return null;
        if (!(key instanceof String))
            return key;
        return ((String) key).toLowerCase();
    }

    public boolean containsKey(Object key) {
        if (key == null)
            return false;
        return map.containsKey(simplifyKey(key));
    }

    public boolean containsValue(Object value) {
        if (value == null)
            return false;
        return map.containsValue(value);
    }

    public Object get(Object key) {
        if (key == null) {
            return null;
        }
        return map.get(simplifyKey(key));
    }

    public Object put(Object key, Object value) {
        if (key == null) {
            return null;
        }
        if (value == null) {
            map.remove(key);
            return null;
        }
        return map.put(simplifyKey(key), value);
    }

    public Object remove(Object key) {
        if (key == null) {
            return null;
        }
        return map.remove(simplifyKey(key));
    }

    public void putAll(Map t) {
        Vector entries = new Vector(t.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = (Entry) entries.get(i);
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        map.clear();
    }

    public Set keySet() {
        return map.keySet();
    }

    public Collection values() {
        return map.values();
    }

    public Set entrySet() {
        return map.entrySet();
    }

}