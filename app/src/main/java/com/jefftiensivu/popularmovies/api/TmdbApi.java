package com.jefftiensivu.popularmovies.api;

import com.jefftiensivu.popularmovies.model.TmdbModels;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by jeff on 10/6/2015.
 */
public interface TmdbApi {
    @GET("/3/discover/movie")
    void movieArray(
         @Query("sort_by") String sort,
         @Query("api_key") String key,
         Callback<TmdbModels> cb
     );
}