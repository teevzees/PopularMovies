package com.jefftiensivu.popularmovies.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by jeff on 10/31/2015.
 */
@ContentProvider(authority = PopMoviesProvider.AUTHORITY, database = PopMoviesDatabase.class)
public final class PopMoviesProvider {
    public static final String AUTHORITY = "com.jefftiensivu.popularmovies.data.PopMoviesProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path{
        String POPULAR_MOVIES = "popular_movies";
        String FAVORITE_MOVIES = "favorite_movies";
        String HIGHEST_RATED_MOVIES = "highest_rated_movies";
/*
        String TRAILER_MAP = "trailer_map";
        String REVIEW_MAP = "review_map";
*/
    }

    private static Uri buildUri(String... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths){
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = PopMoviesDatabase.POPULAR_MOVIES) public static class PopularMovies{
        @ContentUri(
                path = Path.POPULAR_MOVIES,
                type = "vnd.android.cursor.dir/popular_movie",
                defaultSort = PopMoviesColumns.POPULARITY + " DESC")
        public static final Uri CONTENT_URI = buildUri(Path.POPULAR_MOVIES);

        @InexactContentUri(
                name = "MOVIE_ID",
                path = Path.POPULAR_MOVIES + "/#",
                type = "vnd.android.cursor.item/movie",
                whereColumn = PopMoviesColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id){
            return buildUri(Path.POPULAR_MOVIES, String.valueOf(id));
        }
    }

    @TableEndpoint(table = PopMoviesDatabase.HIGHEST_RATED_MOVIES) public static class HighestRatedMovies{
        @ContentUri(
                path = Path.HIGHEST_RATED_MOVIES,
                type = "vnd.android.cursor.dir/highest_rated_movie",
                defaultSort = PopMoviesColumns.VOTE_AVERAGE + " DESC")
        public static final Uri CONTENT_URI = buildUri(Path.HIGHEST_RATED_MOVIES);

        @InexactContentUri(
                name = "MOVIE_ID",
                path = Path.HIGHEST_RATED_MOVIES + "/#",
                type = "vnd.android.cursor.item/movie",
                whereColumn = PopMoviesColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id){
            return buildUri(Path.HIGHEST_RATED_MOVIES, String.valueOf(id));
        }
    }

    @TableEndpoint(table = PopMoviesDatabase.FAVORITE_MOVIES) public static class FavoriteMovies{
        @ContentUri(
                path = Path.FAVORITE_MOVIES,
                type = "vnd.android.cursor.dir/favorite_movie",
                defaultSort = PopMoviesColumns.POPULARITY + "ASC")
        public static final Uri CONTENT_URI = buildUri(Path.FAVORITE_MOVIES);

        @InexactContentUri(
                name = "MOVIE_ID",
                path = Path.FAVORITE_MOVIES + "/#",
                type = "vnd.android.cursor.item/movie",
                whereColumn = PopMoviesColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id){
            return buildUri(Path.FAVORITE_MOVIES, String.valueOf(id));
        }
    }
}
