package com.imufortka

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager

class App : Application() {

    companion object {
        private lateinit var preferences: SharedPreferences

        fun storeIntPreference(key: String, defaultValue: Int) {
            val editor: SharedPreferences.Editor = preferences.edit()
            editor.putInt(key, defaultValue)
            editor.apply()
        }

        fun getIntPreference(key: String, defaultValue: Int): Int {
            return preferences.getInt(key, defaultValue)
        }

        fun storeStringPrefernce(key:String,defaultValue: String){
            val editor:SharedPreferences.Editor= preferences.edit()
            editor.putString(key,defaultValue)
            editor.apply()
        }

        fun getStringPrefernce(key: String,defaultValue: String): String? {
            return preferences.getString(key,defaultValue)
        }
    }

    private fun setPreferences(preference: SharedPreferences) {
        preferences = preference
    }

    override fun onCreate() {
        super.onCreate()
        setPreferences(PreferenceManager.getDefaultSharedPreferences(this))
    }


}