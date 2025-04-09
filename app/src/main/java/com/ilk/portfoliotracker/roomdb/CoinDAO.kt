package com.ilk.portfoliotracker.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ilk.portfoliotracker.model.CoinGeckoData

@Dao
interface CoinDAO {

    @Insert
    suspend fun insertAll(vararg coin : CoinGeckoData) : List<Long>

    @Query("SELECT * FROM coingeckodata")
    suspend fun getAllCoins() : List<CoinGeckoData>

    @Query("SELECT * FROM CoinGeckoData WHERE uuid = :coinID")
    suspend fun getCoin(coinID : Int) : CoinGeckoData

    @Query("DELETE FROM CoinGeckoData")
    suspend fun deleteAll()
}