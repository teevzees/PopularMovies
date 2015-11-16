package com.jefftiensivu.popularmovies.api;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by jeff on 10/24/2015.
 */
public class TmdbService {
    private static final String LOG_TAG = TmdbService.class.getSimpleName();
    private static final String BASE_URL = "http://api.themoviedb.org/";

    public static final String MY_API_KEY = "62d35596628c0b0ad37ecb6e87c385b2";

    private static TmdbApi apiService;

    public static TmdbApi getApiService(){
        if(apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(TmdbApi.class);
        }
        return apiService;
    }
}