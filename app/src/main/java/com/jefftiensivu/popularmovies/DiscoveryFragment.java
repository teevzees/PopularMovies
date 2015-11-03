package com.jefftiensivu.popularmovies;

import android.app.Fragment;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.jefftiensivu.popularmovies.api.TmdbApi;
import com.jefftiensivu.popularmovies.api.TmdbService;
import com.jefftiensivu.popularmovies.data.PopMoviesColumns;
import com.jefftiensivu.popularmovies.data.PopMoviesProvider;
import com.jefftiensivu.popularmovies.model.MovieInfo;
import com.jefftiensivu.popularmovies.model.TmdbSorted;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by jeff on 9/28/2015.
 * Encapsulates the fetching and display of the movie posters
 */
//public class DiscoveryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
public class DiscoveryFragment extends Fragment{
    private static final String LOG_TAG = DiscoveryFragment.class.getSimpleName();
    private static final int CURSOR_LOADER_ID = 0;

    //When we get the data from The Movie DB it will live here.
    private static List<MovieInfo> movieArray;
    private static String mSort;
    private GridView gridview;


    public DiscoveryFragment() {
        movieArray = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //Log.v(LOG_TAG, "onCreate happened!!!!!!!!!!!!!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.discovery_fragment, container, false);
        gridview = (GridView) rootView.findViewById(R.id.gridview);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("DETAIL_RESULT", Parcels.wrap(movieArray.get(position)));
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Fetch the list of movies.
        executeSort();
        Log.v(LOG_TAG, "onStart happened!!!!!!!!!!!");
    }

    /**
     * sets the mSort.
     */
    public void executeSort() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSort = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        getMovieArray();
    }

    /**
     * This is way more complicated then it needs to be... but I can't think of a better way to do
     * this.
     * @throws Error
     */
    public void getMovieArray() throws Error{
        TmdbApi myService = TmdbService.getApiService();
        Call<TmdbSorted> call = myService.movieArray(mSort + ".desc", TmdbService.MY_API_KEY);
        Log.v(LOG_TAG, mSort + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        call.enqueue(new Callback<TmdbSorted>() {
            @Override
            public void onResponse(Response<TmdbSorted> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    movieArray = response.body().getResults();

                    insertData();

                    makeButtons();
//                    Log.v(LOG_TAG, response.toString());
                } else {
                    Log.e(LOG_TAG, response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getActivity(), "The Internet is down!", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, t.toString());
            }
        });
    }

    public void insertData(){
        Log.d(LOG_TAG, "insert");
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>(movieArray.size());
        long now = System.currentTimeMillis()/1000;

        Cursor c;
        Uri contentUri;
        switch (mSort) {
            case PopMoviesColumns.POPULARITY:
                contentUri = PopMoviesProvider.PopularMovies.CONTENT_URI;
                c = getActivity().getContentResolver().query(
                        contentUri, null, null, null, null);
                break;
            case PopMoviesColumns.VOTE_AVERAGE:
                contentUri = PopMoviesProvider.HighestRatedMovies.CONTENT_URI;
                c = getActivity().getContentResolver().query(
                        contentUri, null, null, null, null);
                break;
//            case PopMoviesColumns.FAV:
            default:
                contentUri = PopMoviesProvider.FavoriteMovies.CONTENT_URI;
                c = getActivity().getContentResolver().query(
                        contentUri, null, null, null, null);
        }

        if (c == null|| c.getCount() == 0) {
            for (MovieInfo movieInfo : movieArray) {
                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                        contentUri);
                builder.withValue(PopMoviesColumns.TMDB_ID, movieInfo.getId());
                builder.withValue(PopMoviesColumns.TITLE, movieInfo.getTitle());
                Log.i(LOG_TAG, "Inserting data for " + movieInfo.getTitle()); ///////////////////is this happening???
                builder.withValue(PopMoviesColumns.OVERVIEW, movieInfo.getOverview());
                builder.withValue(PopMoviesColumns.RELEASE_DATE, movieInfo.getReleaseDate());
                builder.withValue(PopMoviesColumns.POSTER_PATH, movieInfo.getUrl());
                builder.withValue(PopMoviesColumns.POPULARITY, movieInfo.getPopularity());
                builder.withValue(PopMoviesColumns.VOTE_AVERAGE, movieInfo.getVoteAverage());
                builder.withValue(PopMoviesColumns.VOTE_COUNT, movieInfo.getVoteCount());
                builder.withValue(PopMoviesColumns.TIME_STAMP, (int) now);
                batchOperations.add(builder.build());
            }

            try {
                getActivity().getContentResolver().applyBatch(PopMoviesProvider.AUTHORITY, batchOperations);
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, "Error applying batch insert", e);
            }
        }
        c.close();
    }

    public void addData(){
        Log.d(LOG_TAG, "Adding Data");
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>(movieArray.size());

        for(MovieInfo movieInfo : movieArray){
//            if(movieInfo.getId() == )
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                    PopMoviesProvider.PopularMovies.CONTENT_URI);
            builder.withValue(PopMoviesColumns.TMDB_ID, movieInfo.getId());
            builder.withValue(PopMoviesColumns.TITLE, movieInfo.getTitle());
            Log.i(LOG_TAG, "Adding data for " + movieInfo.getTitle()); ///////////////////is this happening???
            builder.withValue(PopMoviesColumns.OVERVIEW, movieInfo.getOverview());
            builder.withValue(PopMoviesColumns.RELEASE_DATE, movieInfo.getReleaseDate());
            builder.withValue(PopMoviesColumns.POSTER_PATH, movieInfo.getUrl());
            builder.withValue(PopMoviesColumns.POPULARITY, movieInfo.getPopularity());
            builder.withValue(PopMoviesColumns.VOTE_AVERAGE, movieInfo.getVoteAverage());
            builder.withValue(PopMoviesColumns.VOTE_COUNT, movieInfo.getVoteCount());
            batchOperations.add(builder.build());
        }

        try{
            getActivity().getContentResolver().applyBatch(PopMoviesProvider.AUTHORITY, batchOperations);
        }catch(RemoteException | OperationApplicationException e){
            Log.e(LOG_TAG, "Error applying batch insert", e);
        }
    }

/*
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        return new CursorLoader(getActivity(), PopMoviesProvider.PopularMovies.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mCursorAdapter.swapCursor(null);
    }
*/

    public void makeButtons(){
        String[] posterUrls = getPosterUrls();
        gridview.setAdapter(new ImageAdapterPicasso(getActivity(), posterUrls));
    }

    public String[] getPosterUrls(){
        String[] movieUrls = new String[movieArray.size()];
        for (int i = 0; i < movieArray.size(); i++) {
            MovieInfo object = movieArray.get(i);
            movieUrls[i] = object.getUrl();

        }
/*
        for(String s : movieUrls){
            Log.v(LOG_TAG, "Movie Path: " + s);
        }
*/
        return movieUrls;
    }
    public void onPause(){
        super.onPause();
        //Log.v(LOG_TAG, "onPause happened!!!!!!!!!!!!!!");
    }
    public void onResume(){
        super.onResume();
        //Log.v(LOG_TAG, "onResume happened!!!!!!!!!!!!!!");
    }
    public void onStop(){
        super.onStop();
        //Log.v(LOG_TAG, "onStop happened!!!!!!!!!!!!!!!");
    }
    public void onDestroy(){
        super.onDestroy();
        //Log.v(LOG_TAG, "onDetroy happened!!!!!!!!!!!!");
    }
}