/*
 * Modified By Romulus U. Ts'ai
 * Copied from MyMap.java in SharedLib (http://www.fightingquaker.com/sharedlib/)
 * On Oct 6, 2008
 *
 * Removed all debug executions
 *
 */

package org.cmc.music.util;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class MyMap extends Hashtable {
    public static final long serialVersionUID = 1;

    public MyMap() {
    }

    public MyMap(Map other) {
        super(other);
    }

    private Object actual_get(Object key, Object def) {
        Object result = super.get(key);
        return (result == null) ? (def) : (result);

    }

    public final Object getRequired(Object key) throws Exception {
        Object result = actual_get(key, null);

        if (result == null)
            throw new Exception("missing: " + key);

        return result;
    }

    //	public final Number getRequiredNumber(Object key) throws Exception
    //	{
    //		return (Number) getRequired(key);
    //	}

    public final Boolean getRequiredBoolean(Object key) throws Exception {
        Boolean result = getBoolean(key);

        if (result == null)
            throw new Exception("missing: " + key);

        return result;
    }

    public final Number getRequiredNumber(Object key) throws Exception {
        Number result = getNumber(key);

        if (result == null)
            throw new Exception("missing: " + key);

        return result;
    }

    public final String getRequiredString(Object key) throws Exception {
        String result = getString(key);

        if (result == null)
            throw new Exception("missing: " + key);

        return result;
    }

    public Object get(Object key, Object def) {
        return actual_get(key, def);

    }

    public Integer get(Object key, Integer def) {
        return (Integer) actual_get(key, def);
    }

    public String get(Object key, String def) {
        return (String) actual_get(key, def);
    }

    public Boolean get(Object key, Boolean def) {
        return (Boolean) actual_get(key, def);
    }

    public Integer get(Object key, int def) {
        return (Integer) actual_get(key, def);
    }

    public final Object put(Object key, long value) {
        return put(key, new Long(value));
    }

    public final Object put(Object key, int value) {
        return put(key, new Integer(value));
    }

    public final Object put(Object key, boolean value) {
        return put(key, new Boolean(value));
    }

    public final Object put(Object key, char value) {
        return put(key, new Character(value));
    }

    public final Object put(Object key, double value) {
        return put(key, new Double(value));
    }

    public final Object put(Object key, float value) {
        return put(key, new Float(value));
    }

    public final Object put(Object key, byte value) {
        return put(key, new Byte(value));
    }

    public final Object put(Object key, short value) {
        return put(key, new Short(value));
    }

    public final Object put(Object key, Object value) {
        if ((key != null) && (value != null))
            return super.put(key, value);

        super.remove(key);
        return null;
        //		return super.get(key);
    }

    public Boolean getBoolean(Object key) {
        return getBoolean(key, null);

    }

    public Boolean getBoolean(Object key, Boolean def) {
        try {
            return (Boolean) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public File getFile(Object key) {
        return getFile(key, null);
    }

    public File getFile(Object key, File def) {
        try {
            return (File) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public Vector getVector(Object key) {
        return getVector(key, null);
    }

    public Vector getVector(Object key, Vector def) {
        try {
            return (Vector) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public Map getMap(Object key) {
        return getMap(key, null);
    }

    public Map getMap(Object key, Map def) {
        try {
            return (Map) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public byte[] getByteArray(Object key) {
        return getByteArray(key, null);
    }

    public byte[] getByteArray(Object key, byte[] def) {
        try {
            return (byte[]) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public Number getNumber(Object key) {
        return getNumber(key, null);
    }

    public Number getNumber(Object key, Number def) {
        try {
            return (Number) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public Integer getInteger(Object key) {
        return getInteger(key, null);
    }

    public Integer getInteger(Object key, Integer def) {
        try {
            return (Integer) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public Double getDouble(Object key) {
        return getDouble(key, null);
    }

    public Double getDouble(Object key, Double def) {
        try {
            return (Double) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public Float getFloat(Object key) {
        return getFloat(key, null);
    }

    public Float getFloat(Object key, Float def) {
        try {
            return (Float) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public String getString(Object key) {
        return getString(key, null);
    }

    public String getString(Object key, String def) {
        try {
            return (String) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

    public Date getDate(Object key) {
        return getDate(key, null);
    }

    public Date getDate(Object key, Date def) {
        try {
            return (Date) actual_get(key, def);
        } catch (ClassCastException e) {

            return null;
        }
    }

}