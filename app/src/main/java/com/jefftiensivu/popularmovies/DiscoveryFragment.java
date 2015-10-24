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
import com.jefftiensivu.popularmovies.model.Result;
import com.jefftiensivu.popularmovies.model.TmdbModels;

import org.parceler.Parcels;

import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by jeff on 9/28/2015.
 * Encapsulates the fetching and display of the movie posters
 */
public class DiscoveryFragment extends Fragment {
    private static final String LOG_TAG = DiscoveryFragment.class.getSimpleName();
    private static final String BASE_URL = "http://api.themoviedb.org/";
    private static final String MY_API_KEY = "************************************";
    private static TmdbApi apiService;
    //When we get the data from The Movie DB it will live here.
    private static List<Result> movieArray;
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
        //Fetch the list of movies for the first time.
        executeSort();
        //Log.v(LOG_TAG, "onStart happened!!!!!!!!!!!");
    }

    public void executeSort() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        createService();
        getMovieArray(sort);
    }

    public void createService(){
        if(apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(TmdbApi.class);
        }else{
            Log.v(LOG_TAG, "Reusing Retrofit Instance.");
        }
    }

    public void getMovieArray(String sort) throws Error{
        Call<TmdbModels> call = apiService.movieArray(sort, MY_API_KEY);
        call.enqueue(new Callback<TmdbModels>(){
            @Override
            public void onResponse(Response<TmdbModels> response, Retrofit retrofit){
                if(response.isSuccess()) {
                    movieArray = response.body().getResults();
                    makeButtons();
                    Log.v(LOG_TAG, response.toString());
                }else{
                    Log.e(LOG_TAG, response.errorBody().toString());
                }
            }
            @Override
            public void onFailure(Throwable t){
                Toast.makeText(getActivity(), "The Internet is down!", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, t.toString());
            }
        });
    }

    public void makeButtons(){
        String[] posterUrls = getPosterUrls();
        gridview.setAdapter(new ImageAdapterPicasso(getActivity(), posterUrls));
}

    public String[] getPosterUrls(){
        String[] movieUrls = new String[movieArray.size()];
        for (int i = 0; i < movieArray.size(); i++) {
            Result object = movieArray.get(i);
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