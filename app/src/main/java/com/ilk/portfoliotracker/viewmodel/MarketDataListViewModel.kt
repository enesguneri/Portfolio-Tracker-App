package com.ilk.portfoliotracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ilk.portfoliotracker.model.BinanceData
import com.ilk.portfoliotracker.model.CoinGeckoData
import com.ilk.portfoliotracker.roomdb.CoinDatabase
import com.ilk.portfoliotracker.service.BinanceAPIService
import com.ilk.portfoliotracker.service.CoinGeckoAPIService
import com.ilk.portfoliotracker.util.PrivateSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarketDataListViewModel(application: Application) : AndroidViewModel(application) {
    private val coinGeckoAPIService = CoinGeckoAPIService()
    private val binanceAPIService = BinanceAPIService()

    private val privateSharedPreferences = PrivateSharedPreferences(getApplication())

    val coinGeckoDataList = MutableLiveData<List<CoinGeckoData>>()
    val binanceDataList = MutableLiveData<List<BinanceData>>()
    val dataExceptionAlert = MutableLiveData<Boolean>()
    val dataLoading = MutableLiveData<Boolean>()
    private var dataLoaded : Boolean = false

    private val priceUpdateTime = 1000 * 1000 * 1000L//1 saniye
    private val updateAllData = 60 * 1000 * 1000 * 1000L//1 dakika


    private fun getDataFromAPI() {
        dataLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val coinGeckoData =
                    coinGeckoAPIService.getData()//limiti aşmamak için coinGeckoAPI'dan tekrar veri çekilmez.
                val binanceData =
                    binanceAPIService.getData()//Binance API'den sadece price bilgisi çekilir ve böylece sınırsız şekilde güncel fiyat bilgisine erişilebilir.
                for (i in coinGeckoData.indices) {
                    //İşlemleri uzatmamak için binanceApi'den alınan price verisi CoinGecko data class'ındaki price verisiyle değiştirilir.
                    //Doğru price verisini bulmak için klasik for döngüsüyle coingecko'daki sembol binance'dekiyle eşleştirilir.
                    val data = coinGeckoData[i]
                    if (data.symbol != null) {
                        val symbol = data.symbol.uppercase()
                        for (j in binanceData.indices) {
                            val binanceDataSymbol = binanceData[j].symbol
                            if (binanceDataSymbol != null) {
                                if (binanceDataSymbol.startsWith(symbol)) {
                                    coinGeckoData[i].current_price = binanceData[j].price
                                    break
                                }
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    dataLoading.value = false
                    dataExceptionAlert.value = false
                    coinGeckoDataList.value = coinGeckoData
                    binanceDataList.value = binanceData
                    //println(data.size)
                    saveDataToRoomDB(coinGeckoData)
                }
                dataLoaded = true
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    dataLoading.value = false
                    dataExceptionAlert.value = true
                }
            }
        }
    }

    fun refreshData(){
        val savedTime = privateSharedPreferences.getTime()

        if(!dataLoaded){
            getDataFromAPI()
        }


        if(dataExceptionAlert.value == true)
            getDataFromRoomDB()
        else {
            if (savedTime != null && savedTime != 0L && System.nanoTime() - savedTime < priceUpdateTime) {
                getDataFromRoomDB()
            } else if (savedTime != null && savedTime != 0L && System.nanoTime() - savedTime > updateAllData) {
                getDataFromAPI()
            } else {
                refreshDataFromAPI()
            }
        }



    }

    fun getDataFromRoomDB(){
        dataLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val data = CoinDatabase(getApplication()).CoinDao().getAllCoins()
            withContext(Dispatchers.Main) {
                dataLoading.value = false
                coinGeckoDataList.value = data
            }
        }
    }


    //API sınırına takılmamak için veriler bir kere çekildikten sonra Local Database'e kaydedilir.
    //Current price gibi veriler anlık olarak güncellenmeye devam eder.
    private fun saveDataToRoomDB(coinGeckoData: List<CoinGeckoData>){
        viewModelScope.launch {
            val dao = CoinDatabase(getApplication()).CoinDao()
            dao.deleteAll()
            val uuidList = dao.insertAll(*coinGeckoData.toTypedArray())
            for (i in uuidList.indices) {
                coinGeckoData[i].uuid = uuidList[i].toInt()
            }

            coinGeckoDataList.value = coinGeckoData
        }
        privateSharedPreferences.saveTime(System.nanoTime())
    }

    fun refreshDataFromAPI() {

        dataLoading.value = false
        viewModelScope.launch {
            try {
                if (coinGeckoDataList.value != null && binanceDataList.value != null) {
                    val coinGeckoData =
                        coinGeckoDataList.value!!//limiti aşmamak için coinGeckoAPI'dan tekrar veri çekilmez.
                    val binanceData =
                        binanceAPIService.getData()//Binance API'den sadece price bilgisi çekilir ve böylece sınırsız şekilde güncel fiyat bilgisine erişilebilir.
                    for (i in coinGeckoData.indices) {
                        //İşlemleri uzatmamak için binanceApi'den alınan price verisi CoinGecko data class'ındaki price verisiyle değiştirilir.
                        //Doğru price verisini bulmak için klasik for döngüsüyle coingecko'daki sembol binance'dekiyle eşleştirilir.
                        val data = coinGeckoData[i]
                        if (data.symbol != null) {
                            val symbol = data.symbol.uppercase()
                            for (j in binanceData.indices) {
                                val binanceDataSymbol = binanceData[j].symbol
                                if (binanceDataSymbol != null) {
                                    if (binanceDataSymbol.startsWith(symbol)) {
                                        coinGeckoData[i].current_price = binanceData[j].price
                                        break
                                    }
                                }
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        dataLoading.value = false
                        dataExceptionAlert.value = false
                        coinGeckoDataList.value = coinGeckoData
                        binanceDataList.value = binanceData
                        saveDataToRoomDB(coinGeckoData)
                    }
                }
            } catch (e : Exception){
                withContext(Dispatchers.Main){
                    dataLoading.value = false
                    dataExceptionAlert.value = true
                }
            }
        }
    }
    fun isAvailableOnBinance(symbol: String): Boolean {
        val binanceData =
            binanceDataList.value!!
        val uppercasedSymbol = symbol.uppercase()
        for (j in binanceData.indices) {
            val binanceDataSymbol = binanceData[j].symbol
            if (binanceDataSymbol != null) {
                if (binanceDataSymbol.startsWith(uppercasedSymbol)) {
                    return true
                }
            }
        }
        return false
    }
}