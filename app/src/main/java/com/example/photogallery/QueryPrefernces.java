package com.example.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPrefernces {
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    //Returns the query value stored in shared preferences.
    // It does so by first acquiring the default
    // SharedPreferences for the given context.
    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    //Writes the input the input query to the
    // default shared preferences for the given text
    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }
}
