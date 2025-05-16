package com.ilk.portfoliotracker.service

import com.ilk.portfoliotracker.model.CoinGeckoData
import retrofit2.http.GET

interface CoinGeckoAPI {
    //https://api.binance.com/api/v3/ticker/price
    //belirli bir işlem çifti için : https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT

    //https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=100&page=1
    //top100'deki coinlerin tüm bilgilerini getirir

    //BASE URL : https://api.binance.com/
    //ENDPOINT : api/v3/ticker/price

    @GET("api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=250&page=1")
    suspend fun getMarketData() : List<CoinGeckoData>
}