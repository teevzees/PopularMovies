package com.jefftiensivu.popularmovies;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
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
import com.jefftiensivu.popularmovies.data.ReviewColumns;
import com.jefftiensivu.popularmovies.data.TrailerColumns;
import com.jefftiensivu.popularmovies.model.MovieDetails;
import com.jefftiensivu.popularmovies.model.MovieInfo;
import com.jefftiensivu.popularmovies.model.Result;
import com.jefftiensivu.popularmovies.model.Youtube;
import com.jefftiensivu.popularmovies.utility.Time;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;
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

    private List<Youtube> mTrailerArray;
    private List<Result> mReviewsArray;


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

        mTrailerArray = null;
        mReviewsArray = null;

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
            Log.v(LOG_TAG, "Favorite! query info " + c.getString(2));
        } else {
            mFavChecked = false;
            star.setChecked(false);
            Log.v(LOG_TAG, "Not Favorite! record not found!!!!!");
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
                    mTrailerArray = response.body().getTrailers().getYoutube();
                    mReviewsArray = response.body().getReviews().getResults();
                    Log.v(LOG_TAG, "Response code " + response.code());
                    if (mTrailerArray == null || mTrailerArray.size() < 1) {
                        Log.d(LOG_TAG, "mTrailerArray is empty!!!");
                    } else {
                        insertTrailers();
                        //TODO add trailers to UI
                    }
                    if (mReviewsArray == null || mReviewsArray.size() < 1) {
                        Log.d(LOG_TAG, "mReviewsArray is empty!!!");
                    } else {
                        insertReviews();
                        //Todo add reviews info to UI.
                    }
                    //TODO add supplemental info to DB and UI
                    mMovieDetails = response.body();
                    updateMovieDetails();
                } else {
                    Log.e(LOG_TAG, response.errorBody().toString());
                    Log.e(LOG_TAG, "Response code " + response.code());
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
     * Batch inserts Movie Review Table Data
     */
    public void insertReviews(){
        Log.d(LOG_TAG, "Inserting Movie Reviews");

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>(mReviewsArray.size());
                Uri contentUri = PopMoviesProvider.Reviews.CONTENT_URI;
                Cursor c = getActivity().getContentResolver().query(contentUri, null, null, null, null);

                if (c != null) {//check for instance of the DB
                    c.close();
                    c = getActivity().getContentResolver().query(contentUri, null,
                            "parent_tmdb_id = ? AND parent_table = ?",
                            new String[] {mMovieDetails.getId().toString(),
                                    PopMoviesDbHelper.sortUri(mSort).toString()},
                            null);
                    if(c == null || c.getCount() == 0){//check if the data is already there
                        for (Result reviewInfo : mReviewsArray) {
                            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                                    contentUri);
                            builder.withValue(ReviewColumns.PARENT_TABLE, PopMoviesDbHelper.sortUri(mSort).toString());
                            builder.withValue(ReviewColumns.PARENT_TMDB_ID, mMovieDetails.getId());

                            builder.withValue(ReviewColumns.AUTHOR, reviewInfo.getAuthor());
                            Log.i(LOG_TAG, "Inserting review data for " + reviewInfo.getAuthor());
                            builder.withValue(ReviewColumns.CONTENT, reviewInfo.getContent());
                            batchOperations.add(builder.build());
                        }
                        try {
                            getActivity().getContentResolver().applyBatch(PopMoviesProvider.AUTHORITY,
                                    batchOperations);
                        } catch (RemoteException | OperationApplicationException e) {
                            Log.e(LOG_TAG, "Error applying batch insert", e);
                        }
                        if(c != null) {
                            c.close();
                        }
                    }else{
                        c.close();
                    }
                }
            }
        }).start();
    }

    /**
     * Batch inserts Movie Trailer Table Data
     */
    public void insertTrailers(){
        Log.d(LOG_TAG, "Inserting Movie Trailers");

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>(mTrailerArray.size());
                Uri contentUri = PopMoviesProvider.Trailers.CONTENT_URI;
                Cursor c = getActivity().getContentResolver().query(contentUri, null, null, null, null);

                if (c != null) {
                    c.close();
                    c = getActivity().getContentResolver().query(contentUri, null,
                            "parent_tmdb_id = ? AND parent_table = ?",
                            new String[] {mMovieDetails.getId().toString(),
                                    PopMoviesDbHelper.sortUri(mSort).toString()},
                            null);
                    if(c == null || c.getCount() == 0) {//check if data is already there
                        for (Youtube trailerInfo : mTrailerArray) {
                            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                                    contentUri);
                            builder.withValue(TrailerColumns.PARENT_TABLE, PopMoviesDbHelper.sortUri(mSort).toString());
                            builder.withValue(TrailerColumns.PARENT_TMDB_ID, mMovieDetails.getId());

                            builder.withValue(TrailerColumns.NAME, trailerInfo.getName());
                            Log.i(LOG_TAG, "Inserting trailer data for " + trailerInfo.getName());
                            builder.withValue(TrailerColumns.SIZE, trailerInfo.getSize());
                            builder.withValue(TrailerColumns.SOURCE, trailerInfo.getSource());
                            builder.withValue(TrailerColumns.TYPE, trailerInfo.getType());
                            batchOperations.add(builder.build());
                        }
                        try {
                            getActivity().getContentResolver().applyBatch(PopMoviesProvider.AUTHORITY,
                                    batchOperations);
                        } catch (RemoteException | OperationApplicationException e) {
                            Log.e(LOG_TAG, "Error applying batch insert", e);
                        }
                        if(c != null){
                            c.close();
                        }
                    }else{
                        c.close();
                    }
                }
            }
        }).start();
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

                for (Uri contentUri : PopMoviesDbHelper.getMovieTableUris()){
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