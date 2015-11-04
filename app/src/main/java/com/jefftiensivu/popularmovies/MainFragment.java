package com.jefftiensivu.popularmovies;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.jefftiensivu.popularmovies.data.PopMoviesDbHelper;
import com.jefftiensivu.popularmovies.model.MovieInfo;
import com.jefftiensivu.popularmovies.model.TmdbSorted;

import org.parceler.Parcels;

import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by jeff on 9/28/2015.
 * Encapsulates the fetching and display of the movie posters
 */
//public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
public class MainFragment extends Fragment{
    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private static final int CURSOR_LOADER_ID = 0;

    //When we get the data from The Movie DB it will live here.
    private static List<MovieInfo> movieArray;
    private static String mSort;
    private GridView gridview;


    public MainFragment() {
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

                    PopMoviesDbHelper.insertData(movieArray, mSort, getActivity());

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