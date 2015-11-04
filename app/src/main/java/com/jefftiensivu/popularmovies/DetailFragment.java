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

import com.jefftiensivu.popularmovies.api.TmdbApi;
import com.jefftiensivu.popularmovies.api.TmdbService;
import com.jefftiensivu.popularmovies.model.MovieDetails;
import com.jefftiensivu.popularmovies.model.MovieInfo;
import com.jefftiensivu.popularmovies.model.Result;
import com.jefftiensivu.popularmovies.model.Youtube;
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
public class DetailFragment extends Fragment {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String PATH_PREFIX = "http://image.tmdb.org/t/p/w185";
    @Bind(R.id.movie_title) TextView textTitle;
    @Bind(R.id.movie_poster) ImageView imagePoster;
    @Bind(R.id.movie_year) TextView textYear;
    @Bind(R.id.movie_vote) TextView textVote;
    @Bind(R.id.movie_synopsis) TextView textSynopsis;

    public DetailFragment() {
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
//                getTrailerArray(result.getId().toString());
//                getReviewArray(result.getId().toString());
                getTrailersAndReviews(result.getId().toString());
                Log.v(LOG_TAG, "Movie id = " + result.getId().toString());
            }else{
                //textSynopsis.setText("Loading Error");
            }
        }
        return rootView;
    }

    public void getTrailersAndReviews(String id) throws Error{
        TmdbApi myService = TmdbService.getApiService();
        Call<MovieDetails> call = myService.trailerAndReviewCall(id, TmdbService.MY_API_KEY, "trailers,reviews");
        call.enqueue(new Callback<MovieDetails>() {
            @Override
            public void onResponse(Response<MovieDetails> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<Youtube> trailerArray = response.body().getTrailers().getYoutube();
                    List<Result> reviewsArray = response.body().getReviews().getResults();
                    Log.v(LOG_TAG, "Response code " + response.code());
                    if(trailerArray == null || trailerArray.size() < 1){
                        Log.e(LOG_TAG, "trailerArray is empty!!!");
                        //Todo take away Trailers section of UI.
                    }else {
                        for (Youtube t : trailerArray) {
                            Log.v(LOG_TAG, t.getName());
                            Log.v(LOG_TAG, t.getSource());
                            //Todo add Trailer info to UI.
                        }
                    }
                    if(reviewsArray == null || reviewsArray.size() < 1){
                        Log.e(LOG_TAG, "reviewsArray is empty!!!");
                        //TODO take away the reviews section of UI.
                    }else {
                        for (Result t : reviewsArray) {
                            Log.v(LOG_TAG, t.getAuthor());
                            Log.v(LOG_TAG, t.getContent());
                            //Todo add Trailer info to UI.
                        }
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
