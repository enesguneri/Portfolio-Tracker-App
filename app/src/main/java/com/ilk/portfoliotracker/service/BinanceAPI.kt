package com.ilk.portfoliotracker.service

import com.ilk.portfoliotracker.model.BinanceData
import retrofit2.http.GET

interface BinanceAPI {
    @GET("api/v3/ticker/price")
    suspend fun getMarketData() : List<BinanceData>
}