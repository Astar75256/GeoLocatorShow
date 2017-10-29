package ru.astar.geolocatorshow;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by Astar on 27.10.2017.
 */

public class AppStorage {

    public static final String PREF_NAME_STORAGE = "GeoLocatorPreferences";

    private static Context context;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;

    public static void init(Context cont) {
        context = cont;
    }

    private static void init() {
        preferences = context.getSharedPreferences(PREF_NAME_STORAGE, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static void addProperty(String name, String value) {
        if (preferences == null) {
            init();
        }
        editor.putString(name, value);
        editor.commit();
    }

    public static void addPropertySet(String name, Set<String> values) {
        if (preferences == null) {
            init();
        }
        editor.putStringSet(name, values);
        editor.commit();
    }

    public static String getProperty(String name) {
        if (preferences == null) {
            init();
        }
        return preferences.getString(name, null);
    }


    public static Set<String> getSetProperty(String name) {
        if (preferences == null) {
            init();
        }
        return preferences.getStringSet(name, null);
    }

    public static boolean isContaints(String key) {
        if (preferences == null) {
            init();
        }
        return preferences.contains(key);
    }
}
