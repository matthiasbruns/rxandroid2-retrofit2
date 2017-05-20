package com.matthiasbruns.rxretrofit.network;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by mbruns on 20.05.17.
 */

public interface CityService {

    /**
     * This method returns all cities within a given bounding box
     *
     * Example from the api docs: citiesJSON?north=44.1&south=-9.9&east=-22.4&west=55.2&lang=de&username=demo
     *
     * @param north    bounding box north
     * @param south    bounding box south
     * @param east     bounding box east
     * @param west     bounding box west
     * @param lang     geoname output language
     */
    @GET("citiesJSON")
    Single<CityResponse> queryGeonames(@Query("north") double north, @Query("south") double south,
            @Query("east") double east, @Query("west") double west, @Query("lang") String lang);
}
