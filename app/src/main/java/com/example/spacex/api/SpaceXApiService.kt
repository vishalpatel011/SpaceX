package com.example.spacex.api

import com.example.spacex.model.Launch
import com.example.spacex.model.Launchpad
import com.example.spacex.model.Rocket
import retrofit2.http.GET
import retrofit2.http.Path

interface SpaceXApiService {
    @GET("launches")
    suspend fun getLaunches(): List<Launch>

    @GET("launches/{id}")
    suspend fun getLaunchById(@Path("id") id: String): Launch

    @GET("rockets/{id}")
    suspend fun getRocketById(@Path("id") id: String): Rocket

    @GET("launchpads/{id}")
    suspend fun getLaunchpadById(@Path("id") id: String): Launchpad
}