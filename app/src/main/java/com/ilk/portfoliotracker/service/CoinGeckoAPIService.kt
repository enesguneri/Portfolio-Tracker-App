package com.ilk.portfoliotracker.service

import com.ilk.portfoliotracker.model.CoinGeckoData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CoinGeckoAPIService {
    private val retrofit = Retrofit.Builder().baseUrl("https://api.coingecko.com/").addConverterFactory(
        GsonConverterFactory.create())
        .build().create(CoinGeckoAPI::class.java)

    suspend fun getData() : List<CoinGeckoData> {
        return retrofit.getMarketData()
    }
}