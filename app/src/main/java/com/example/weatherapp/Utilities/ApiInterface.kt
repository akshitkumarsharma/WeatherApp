package com.example.weatherapp.Utilities


import com.example.weatherapp.POJO.ModelClass
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("weather")
    fun getCurrentWeatherData(
        @Query("lat") latitude:String,
        @Query("lon") longitude:String,
        @Query("APPID") api_key:String
    ): retrofit2.Call<ModelClass>

    @GET("weather")
    fun getCityWeatherData(
        @Query("q") CityName:String,
        @Query("APPID") api_key:String
    ): retrofit2.Call<ModelClass>
}