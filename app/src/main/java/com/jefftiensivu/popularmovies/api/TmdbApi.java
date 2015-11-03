package com.jefftiensivu.popularmovies.api;

import com.jefftiensivu.popularmovies.model.MovieDetails;
import com.jefftiensivu.popularmovies.model.TmdbSorted;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by jeff on 10/6/2015.
 */
public interface TmdbApi {
    @GET("3/discover/movie")
    Call<TmdbSorted> movieArray(
            @Query("sort_by") String sort,
            @Query("api_key") String key
    );

    @GET("3/movie/{id}")
    Call<MovieDetails> trailerAndReviewCall(
            @Path("id") String id,
            @Query("api_key") String key,
            @Query("append_to_response") String appendThis
    );

/*
    @GET("3/movie/{id}/videos")
    Call<TmdbTrailers> trailerCall(
            @Path("id") String id,
            @Query("api_key") String key
    );

    @GET("3/movie/{id}/reviews")
    Call<TmdbReviews> reviewCall(
            @Path("id") String id,
            @Query("api_key") String key
    );
*/
}