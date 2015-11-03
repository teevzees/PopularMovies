package com.jefftiensivu.popularmovies.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by jeff on 10/31/2015.
 */
@Database(version = PopMoviesDatabase.VERSION)
public final class PopMoviesDatabase {
    private PopMoviesDatabase(){}

    public static final int VERSION = 1;

    @Table(PopMoviesColumns.class) public static final String POPULAR_MOVIES = "popular_movies";

    @Table(PopMoviesColumns.class) public static final String HIGHEST_RATED_MOVIES = "highest_rated_movies";

    @Table(PopMoviesColumns.class) public static final String FAVORITE_MOVIES = "favorite_movies";
}
