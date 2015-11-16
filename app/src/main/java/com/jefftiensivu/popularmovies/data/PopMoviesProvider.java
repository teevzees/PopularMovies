package com.jefftiensivu.popularmovies.data;

import android.net.Uri;

import com.jefftiensivu.popularmovies.data.PopMoviesDatabase.Tables;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by jeff on 10/31/2015.
 */
@ContentProvider(authority = PopMoviesProvider.AUTHORITY, database = PopMoviesDatabase.class)
public final class PopMoviesProvider {
    private static final String LOG_TAG = PopMoviesProvider.class.getSimpleName();


    private PopMoviesProvider(){
    }

    public static final String AUTHORITY = "com.jefftiensivu.popularmovies.data.PopMoviesProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path{
        String POPULAR_MOVIES = "popular_movies";
        String FAVORITE_MOVIES = "favorite_movies";
        String HIGHEST_RATED_MOVIES = "highest_rated_movies";

        String FROM_MOVIES_TABLE = "from_movies_table";

        String TRAILERS = "trailers";
        String REVIEWS = "reviews";
    }

    private static Uri buildUri(String... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths){
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = Tables.POPULAR_MOVIES) public static class PopularMovies{
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
        public static Uri withTmdbId(long tmdb_id){
            return buildUri(Path.POPULAR_MOVIES, String.valueOf(tmdb_id));
        }
    }

    @TableEndpoint(table = Tables.HIGHEST_RATED_MOVIES) public static class HighestRatedMovies{
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
        public static Uri withTmdbId(long tmdb_id){
            return buildUri(Path.HIGHEST_RATED_MOVIES, String.valueOf(tmdb_id));
        }
    }

    @TableEndpoint(table = Tables.FAVORITE_MOVIES) public static class FavoriteMovies{
        @ContentUri(
                path = Path.FAVORITE_MOVIES,
                type = "vnd.android.cursor.dir/favorite_movie",
                defaultSort = PopMoviesColumns.POPULARITY + " DESC")
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

        @InexactContentUri(
                name = "TMDB_ID",
                path = Path.FAVORITE_MOVIES + "/tmdb",
                type = "vnd.android.cursor.item/tmdb",
                whereColumn = PopMoviesColumns.TMDB_ID,
                pathSegment = 1)
        public static Uri withTmdbId(int tmdb_id){
            Uri u = buildUri(Path.FAVORITE_MOVIES);

            return buildUri(Path.FAVORITE_MOVIES, String.valueOf(tmdb_id));
        }

//        public static void copyFav(long tmdb_id, Context parentContext){
//            String db = parentContext.getDatabasePath("popMoviesDatabase").getAbsolutePath();
//            db.execSQL("INSERT INTO " + FavoriteMovies.CONTENT_URI + " SELECT * FROM "
//                    + PopularMovies.CONTENT_URI + " WHERE tmdb_id = " + tmdb_id);
//        }
    }

    @TableEndpoint(table = PopMoviesDatabase.TRAILERS) public static class Trailers{

    }
}
