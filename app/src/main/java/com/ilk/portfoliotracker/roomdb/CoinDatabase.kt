package com.ilk.portfoliotracker.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ilk.portfoliotracker.model.CoinGeckoData

@Database(entities = [CoinGeckoData::class], version = 1)
abstract class CoinDatabase : RoomDatabase() {
    abstract fun CoinDao(): CoinDAO

    companion object{//static ve singleton ile benzer işlev görür.
        //Bu yapı ile projenin herhangi bir yerinde bu classın nesnesi oluşturulursa her yerden erişilebilir. Oluşturulmamışsa oluşturulur.

        @Volatile
        private var instance : CoinDatabase? = null

        private val lock = Any()

        operator fun invoke(context : Context) = instance ?: synchronized(lock) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }


        private fun createDatabase(context: Context) = Room.databaseBuilder(context.applicationContext,CoinDatabase::class.java,"CoinDatabase").build()
        //=, return ile aynı işlevi görür.
    }

}