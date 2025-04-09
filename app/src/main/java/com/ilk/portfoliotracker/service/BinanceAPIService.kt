package com.ilk.portfoliotracker.service

import com.ilk.portfoliotracker.model.BinanceData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BinanceAPIService {
    private val retrofit = Retrofit.Builder().baseUrl("https://api.binance.com/").addConverterFactory(GsonConverterFactory.create())
        .build().create(BinanceAPI::class.java)

    suspend fun getData() : List<BinanceData> {
        val allData = retrofit.getMarketData()
        return allData.filter {
            it.symbol?.endsWith("USDT") ?: false
        }
    }
}