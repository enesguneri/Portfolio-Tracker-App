package com.ilk.portfoliotracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity
data class CoinGeckoData(
    val id: String?,
    val symbol: String?,
    val name: String?,
    val image: String?,
    var current_price: Double?,
    val market_cap : Double?,
    val market_cap_rank : Int?,
    val fully_diluted_valuation: Long?,
    val total_volume: Double?,
    val high_24h: Double?,
    val low_24h: Double?,
    val price_change_24h: Double?,
    val price_change_percentage_24h: Double?,
    val market_cap_change_24h: Double?,
    val market_cap_change_percentage_24h: Double?,
    val circulating_supply: Double?,
    val total_supply: Double?,
    val max_supply: Double?,
    val ath: Double?,
    val ath_change_percentage: Double?,
    val ath_date: String?,
    val atl: Double?,
    val atl_change_percentage: Double?,
    val atl_date: String?,
    //val roi: HashMap<String,String>?,
    val last_updated: String?


) {
    @PrimaryKey(autoGenerate = true)
    var uuid : Int = 0
}
