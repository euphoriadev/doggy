package ru.euphoria.doggy.data.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GeoJsonParser {

    // Feature object type
    private static final String FEATURE = "Feature";
    private static final String FEATURE_GEOMETRY = "geometry";
    private static final String FEATURE_COLLECTION = "FeatureCollection";
    private static final String FEATURE_COLLECTION_ARRAY = "features";

    private static final String GEOMETRY_COORDINATES_ARRAY = "coordinates";
    private static final String GEOMETRY_COLLECTION = "GeometryCollection";
    private static final String GEOMETRY_COLLECTION_ARRAY = "geometries";

    private static final String BOUNDING_BOX = "bbox";
    private static final String PROPERTIES = "properties";

    private static final String POINT = "Point";
    private static final String MULTIPOINT = "MultiPoint";
    private static final String LINESTRING = "LineString";
    private static final String MULTILINESTRING = "MultiLineString";
    private static final String POLYGON = "Polygon";
    private static final String MULTIPOLYGON = "MultiPolygon";


    public static ArrayList<Feature> parse(JSONObject source) throws JSONException {
        ArrayList<Feature> list = new ArrayList<>();

        JSONArray features = source.optJSONArray(FEATURE_COLLECTION_ARRAY);
        if (features == null || features.length() == 0) {
            return list;
        }

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.optJSONObject(i);
            HashMap<String, String> properties = parseProperties(feature.optJSONObject(PROPERTIES));
            Geometry geometry = parseGeometry(feature.optJSONObject(FEATURE_GEOMETRY));

            list.add(new Feature(geometry, "", properties));
        }
        return list;
    }

    private static HashMap<String, String> parseProperties(JSONObject source) {
        HashMap<String, String> map = new HashMap<>();

        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, source.optString(key));
        }
        return map;
    }

    private static Geometry parseGeometry(JSONObject source) throws JSONException {
        String type = source.optString("type");
        if (isGeometry(type)) {
            JSONArray coordinates = source.optJSONArray(GEOMETRY_COORDINATES_ARRAY);
            switch (type) {
                case POINT:
                    return new Point(parseCoordinate(coordinates));
            }
        }
        return null;
    }

    private static LatLng parseCoordinate(JSONArray coordinates) throws JSONException {
        // GeoJSON stores coordinates as Lng, Lat so we need to reverse
        return new LatLng(coordinates.getDouble(1), coordinates.getDouble(0));
    }


    private static boolean isGeometry(String type) {
        return type.matches(POINT + "|" + MULTIPOINT
                + "|" + LINESTRING + "|" + MULTILINESTRING
                + "|" + POLYGON + "|" + MULTIPOLYGON
                + "|" + GEOMETRY_COLLECTION);
    }
}
