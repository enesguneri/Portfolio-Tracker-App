package com.ilk.portfoliotracker.util

import android.content.Context
import android.content.SharedPreferences

class PrivateSharedPreferences {

    companion object {//Kodun bir bölümünde aynı anda aynı nesne hem yazdırılmaya hem de okunmaya çalışılırsa Data Race meydana gelir. Bunu önlemek için bu kod gereklidir.

        private var sharedPreferences : SharedPreferences? = null
        private val TIME = "time"

        @Volatile
        private var instance : PrivateSharedPreferences? = null

        private val lock = Any()

        //invoke ile fonksiyona class adından sonra parantez ile erişilebilir.
        operator fun invoke(context : Context) = instance ?: synchronized(lock) {//synchronized ile aynı anda farklı threadlerden buraya erişilmeye çalışılırsa engellenir.
            instance ?: createPrivSharedPref(context).also {
                instance = it
            }
        }

        private fun createPrivSharedPref(context: Context) : PrivateSharedPreferences {
            sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return PrivateSharedPreferences()
        }

    }

    fun saveTime(time : Long) {
        sharedPreferences?.edit()?.putLong(TIME,time)?.apply()
    }

    fun getTime() = sharedPreferences?.getLong(TIME,0)
}