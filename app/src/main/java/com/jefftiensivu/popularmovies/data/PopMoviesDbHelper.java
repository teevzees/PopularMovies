package com.jefftiensivu.popularmovies.data;

import android.net.Uri;
import android.support.v4.util.SimpleArrayMap;

/**
 * Created by jeff on 11/3/2015.
 * Not an SQLiteOpenHelper.
 * Methods return the 3 Table Uri's
 */
public class PopMoviesDbHelper {
    private static final String LOG_TAG = PopMoviesDbHelper.class.getSimpleName();

    private static final Uri POPULARITY_TABLE = PopMoviesProvider.PopularMovies.CONTENT_URI;
    private static final Uri HIGHEST_RATED_TABLE = PopMoviesProvider.HighestRatedMovies.CONTENT_URI;
    private static final Uri FAVORITE_TABLE = PopMoviesProvider.FavoriteMovies.CONTENT_URI;

    private static final Uri[] sTableUris = {POPULARITY_TABLE, HIGHEST_RATED_TABLE, FAVORITE_TABLE};

    private static final SimpleArrayMap<String, Uri> sTableMap;
    static
    {
        sTableMap = new SimpleArrayMap<>(3);
        sTableMap.put("popularity", POPULARITY_TABLE);
        sTableMap.put("vote_average", HIGHEST_RATED_TABLE);
        sTableMap.put("favorite", FAVORITE_TABLE);
    }



    public static Uri getPopularityTable(){
        return POPULARITY_TABLE;
    }

    public static Uri getHighestRatedTable(){
        return HIGHEST_RATED_TABLE;
    }

    public static Uri getFavoriteTable(){
        return FAVORITE_TABLE;
    }

    public static Uri[] getMovieTableUris(){
        return sTableUris;
    }


    public static Uri sortUri(String sort){
        return sTableMap.get(sort);
    }
}
