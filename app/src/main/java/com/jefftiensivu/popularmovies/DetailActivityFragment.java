package com.jefftiensivu.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jefftiensivu.popularmovies.api.TmdbService;
import com.jefftiensivu.popularmovies.model.MovieInfo;
import com.jefftiensivu.popularmovies.model.ReviewInfo;
import com.jefftiensivu.popularmovies.model.TmdbReviews;
import com.jefftiensivu.popularmovies.model.TmdbTrailers;
import com.jefftiensivu.popularmovies.model.TrailerInfo;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private final String PATH_PREFIX = "http://image.tmdb.org/t/p/w185";
    @Bind(R.id.movie_title) TextView textTitle;
    @Bind(R.id.movie_poster) ImageView imagePoster;
    @Bind(R.id.movie_year) TextView textYear;
    @Bind(R.id.movie_vote) TextView textVote;
    @Bind(R.id.movie_synopsis) TextView textSynopsis;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        MovieInfo result = null;
        try {
            result = Parcels.unwrap(getActivity().getIntent().getParcelableExtra("DETAIL_RESULT"));
        }catch (ClassCastException e){
            Log.e(LOG_TAG, e.getMessage());
            return rootView;
        }

        if(result != null){
            if(result.getTitle() != null) {
                textTitle.setText(result.getTitle());
            }else{
                textTitle.setText("Loading Error");
            }
            Picasso.with(getActivity())
                    .load(PATH_PREFIX + result.getUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder_error)
                    .into(imagePoster);

            if(result.getReleaseDate() != null) {
                String[] parts = result.getReleaseDate().split("-");
                textYear.setText(parts[0]);
            }else{
                textYear.setText("Loading Error");
            }

            if(result.getVoteAverage() != null) {
                textVote.setText(result.getVoteAverage() + "/10");
            }else{
                textVote.setText("Loading Error");
            }

            if (result.getOverview() != null) {
                textSynopsis.setText(result.getOverview());
            }else{
                textSynopsis.setText("Loading Error");
            }
            if (result.getId() != null) {
                getTrailerArray(result.getId().toString());
                getReviewArray(result.getId().toString());
//                getTrailersAndReviews(result.getId().toString());
                Log.v(LOG_TAG, "Movie id = " + result.getId().toString());
            }else{
                //textSynopsis.setText("Loading Error");
            }
        }
        return rootView;
    }

/*
    public void getTrailersAndReviews(String id) throws Error{
        TmdbService myService = new TmdbService();
        Call<TmdbTrailers> call = myService.apiService.trailerAndReviewCall(id, myService.MY_API_KEY);
        call.enqueue(new Callback<TmdbTrailers>() {
            @Override
            public void onResponse(Response<TmdbTrailers> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<TrailerInfo> trailerArray = response.body().getResults();
                    Log.v(LOG_TAG, response.toString());
                    if(trailerArray.size() < 1){
                        Log.e(LOG_TAG, "trailerArray is empty!!!");
                        Log.e(LOG_TAG, "Response code " + response.code());
                        //Todo take away Trailers section of UI.
                    }
                    for(TrailerInfo t : trailerArray){
                        Log.v(LOG_TAG, t.getName());
                        Log.v(LOG_TAG, t.getKey());
                        //Todo add Trailer info to UI.
                    }
                } else {
                    Log.e(LOG_TAG, response.errorBody().toString());
                    Log.e(LOG_TAG,"Response code " + response.code());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getActivity(), "The Internet is down!", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, t.toString());
            }
        });
    }
*/

    public void getTrailerArray(String id) throws Error{
        TmdbService myService = new TmdbService();
        Call<TmdbTrailers> call = TmdbService.apiService.trailerCall(id, TmdbService.MY_API_KEY);
        call.enqueue(new Callback<TmdbTrailers>() {
            @Override
            public void onResponse(Response<TmdbTrailers> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<TrailerInfo> trailerArray = response.body().getResults();
                    Log.v(LOG_TAG, response.toString());
                    if(trailerArray.size() < 1){
                        Log.e(LOG_TAG, "trailerArray is empty!!!");
                        Log.e(LOG_TAG, "Response code " + response.code());
                        //Todo take away Trailers section of UI.
                    }
                    for(TrailerInfo t : trailerArray){
                        Log.v(LOG_TAG, t.getName());
                        Log.v(LOG_TAG, t.getKey());
                        //Todo add Trailer info to UI.
                    }
                } else {
                    Log.e(LOG_TAG, response.errorBody().toString());
                    Log.e(LOG_TAG,"Response code " + response.code());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getActivity(), "The Internet is down!", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, t.toString());
            }
        });
    }

    public void getReviewArray(String id) throws Error{
        //Todo see if I can batch these two calls.
        TmdbService myService = new TmdbService();
        Call<TmdbReviews> call = TmdbService.apiService.reviewCall(id, TmdbService.MY_API_KEY);
        call.enqueue(new Callback<TmdbReviews>() {
            @Override
            public void onResponse(Response<TmdbReviews> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<ReviewInfo> reviewArray = response.body().getResults();
                    Log.v(LOG_TAG, response.toString());
                    if(reviewArray.size() < 1){
                        Log.e(LOG_TAG, "reviewArray is empty!!!");
                        Log.e(LOG_TAG, "Response code " + response.code());
                        //Todo take away Review section of UI.
                    }
                    for(ReviewInfo t : reviewArray){
                        Log.v(LOG_TAG, t.getAuthor());
                        Log.v(LOG_TAG, t.getContent());
                        //Todo add Review info to UI.
                    }
                } else {
                    Log.e(LOG_TAG, response.errorBody().toString());
                    Log.e(LOG_TAG,"Response code " + response.code());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getActivity(), "The Internet is down!", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, t.toString());
            }
        });
    }
}
