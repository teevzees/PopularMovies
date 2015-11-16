package com.jefftiensivu.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jefftiensivu.popularmovies.api.TmdbApi;
import com.jefftiensivu.popularmovies.api.TmdbService;
import com.jefftiensivu.popularmovies.data.PopMoviesColumns;
import com.jefftiensivu.popularmovies.data.PopMoviesDbHelper;
import com.jefftiensivu.popularmovies.data.PopMoviesProvider;
import com.jefftiensivu.popularmovies.model.MovieDetails;
import com.jefftiensivu.popularmovies.model.MovieInfo;
import com.jefftiensivu.popularmovies.model.Result;
import com.jefftiensivu.popularmovies.model.Youtube;
import com.jefftiensivu.popularmovies.utility.Time;
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

    private MovieInfo mMovieInfo;
    private MovieDetails mMovieDetails;
    private String mSort;
    private Boolean mFavChecked;

    @Bind(R.id.movie_title) TextView textTitle;
    @Bind(R.id.movie_poster) ImageView imagePoster;
    @Bind(R.id.movie_year) TextView textYear;
    @Bind(R.id.movie_vote) TextView textVote;
    @Bind(R.id.movie_synopsis) TextView textSynopsis;
    @Bind(R.id.fav) CheckBox star;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_fragment, container, false);
        ButterKnife.bind(this, rootView);

        mMovieInfo = null;
        try {
            mMovieInfo = Parcels.unwrap(getActivity().getIntent().getParcelableExtra("DETAIL_RESULT"));
        }catch (ClassCastException e){
            Log.e(LOG_TAG, e.getMessage());
            return rootView;
        }

        if(mMovieInfo != null){
            if(mMovieInfo.getTitle() != null) {
                textTitle.setText(mMovieInfo.getTitle());
            }else{
                textTitle.setText("Loading Error");
            }
            Picasso.with(getActivity())
                    .load(PATH_PREFIX + mMovieInfo.getUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder_error)
                    .into(imagePoster);

            if(mMovieInfo.getReleaseDate() != null) {
                String[] parts = mMovieInfo.getReleaseDate().split("-");
                textYear.setText(parts[0]);
            }else{
                textYear.setText("Loading Error");
            }

            if(mMovieInfo.getVoteAverage() != null) {
                textVote.setText(mMovieInfo.getVoteAverage() + "/10");
            }else{
                textVote.setText("Loading Error");
            }

            if (mMovieInfo.getOverview() != null) {
                textSynopsis.setText(mMovieInfo.getOverview());
            }else{
                textSynopsis.setText("Loading Error");
            }
            if (mMovieInfo.getId() != null) {
                getTrailersAndReviews(mMovieInfo.getId().toString());
                Log.v(LOG_TAG, "Movie id = " + mMovieInfo.getId().toString());
            }else{
                //textSynopsis.setText("Loading Error");
            }
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSort = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        //Checking the Fav table for our movie. Then setting the Star Checkbox.
        Log.v(LOG_TAG, PopMoviesProvider.FavoriteMovies.withTmdbId(mMovieInfo.getId()).toString());
        star.setOnCheckedChangeListener(null);
        Cursor c = getActivity().getContentResolver().query(
                PopMoviesProvider.FavoriteMovies.CONTENT_URI, null, "tmdb_id = ?",
                new String[]{mMovieInfo.getId().toString()}, null);
        if (c.moveToFirst()) {
            mFavChecked = true;
            star.setChecked(true);
            Log.v(LOG_TAG, "Yay! query info " + c.getString(2));
        } else {
            mFavChecked = false;
            star.setChecked(false);
            Log.v(LOG_TAG, "Nay! record not found!!!!!");
        }
        c.close();
        star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFavChecked = isChecked;
                fav();
            }
        });
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
                    //TODO add supplemental info to DB and UI
                    mMovieDetails = response.body();
                    updateMovieDetails();
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

    /**
     * Updates the DB with Supplemental data from the MovieDetails class.
     */
    private void updateMovieDetails(){
        Log.d(LOG_TAG, "Updating details of " + mMovieDetails.getTitle());

        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentValues cv = new ContentValues();
                cv.put(PopMoviesColumns.TMDB_ID, mMovieDetails.getId());
                cv.put(PopMoviesColumns.TITLE, mMovieDetails.getTitle());
                Log.i(LOG_TAG, "Updating details for " + mMovieDetails.getTitle()); ///////////////////is this happening???
                cv.put(PopMoviesColumns.OVERVIEW, mMovieDetails.getOverview());
                cv.put(PopMoviesColumns.RELEASE_DATE, mMovieDetails.getReleaseDate());
                cv.put(PopMoviesColumns.RUN_TIME, mMovieDetails.getRuntime());
                cv.put(PopMoviesColumns.POSTER_PATH, mMovieDetails.getPosterPath());
                cv.put(PopMoviesColumns.POPULARITY, mMovieDetails.getPopularity());
                cv.put(PopMoviesColumns.VOTE_AVERAGE, mMovieDetails.getVoteAverage());
                cv.put(PopMoviesColumns.TIME_STAMP, Time.getTimeStamp());

                for (Uri contentUri : PopMoviesDbHelper.getTableUris()){
                    int uRows = getActivity().getContentResolver().update(
                            contentUri,
                            cv,
                            "tmdb_id = ?",
                            new String[]{mMovieDetails.getId().toString()}
                    );
                    Log.v(LOG_TAG, "Updated " + uRows + " Movies with MovieDetails in " + contentUri.toString());
                    Log.v(LOG_TAG, "tmdb_id = " + mMovieDetails.getId());
                }
            }
        }).start();
    }

    private void fav(){
        Log.d(LOG_TAG, "Fav'ing of " + mMovieInfo.getTitle());

        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = getActivity().getContentResolver();
                ContentValues cv = new ContentValues();

                if(mFavChecked){
                    //Copy entry from one table to another
                    Cursor c = contentResolver.query(PopMoviesDbHelper.sortUri(mSort), null,
                            "tmdb_id = ?", new String[]{mMovieInfo.getId().toString()}, null);
                    if(c.moveToFirst()) {
                        DatabaseUtils.cursorRowToContentValues(c, cv);
                        c.close();
                        c = contentResolver.query(PopMoviesProvider.FavoriteMovies.CONTENT_URI,
                                null,null,null,"_id ASC");
                        if(c.moveToLast()) {
                            int columnIndex = c.getColumnIndex("_id");
                            int nRows = c.getInt(columnIndex) + 1;
                            Log.v(LOG_TAG, "Trying to insert a fav into row " + nRows);
                            cv.put(PopMoviesColumns._ID, nRows);
                        }else { // Favs must be empty
                            Log.v(LOG_TAG, "Populating empty Favs Table.");
                            cv.put(PopMoviesColumns._ID, 0);
                        }
                    }
                    contentResolver.insert(PopMoviesProvider.FavoriteMovies.CONTENT_URI, cv);
                    c.close();
                }else{ //mFavChecked == false
                    int dRows = contentResolver.delete(
                            PopMoviesProvider.FavoriteMovies.CONTENT_URI, "tmdb_id = ?",
                            new String[]{mMovieInfo.getId().toString()});
                    Log.v(LOG_TAG,"Deleted " + dRows + " rows of Favs!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
            }
        }).start();
    }
}