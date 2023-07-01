package com.roy.data

import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

object DataStoreHelper {

    private val HAS_COMPLETED_LEVEL_ZERO = preferencesKey<Boolean>("HAS_COMPLETED_LEVEL_ZERO")

    private val HIGH_SCORE = preferencesKey<Long>("HIGH_SCORE")

    private val MAX_LEVELS = preferencesKey<Int>("MAX_LEVELS")

    var dataStore: DataStore<Preferences>? = null

    fun initDataStore(activity: AppCompatActivity) {
        com.roy.data.DataStoreHelper.dataStore = activity.createDataStore(
            name = "app_settings"
        )

        activity.lifecycleScope.launchWhenCreated {
            com.roy.data.LevelInfo.hasPlayedTutorial =
                com.roy.data.DataStoreHelper.getHasCompletedTutorial()
        }
    }

    suspend fun setHasCompletedTutorial() {
        com.roy.data.DataStoreHelper.dataStore?.edit {
            it[com.roy.data.DataStoreHelper.HAS_COMPLETED_LEVEL_ZERO] = true
        }
    }

    private suspend fun getHasCompletedTutorial(): Boolean {
        return com.roy.data.DataStoreHelper.dataStore?.data?.first()?.get(com.roy.data.DataStoreHelper.HAS_COMPLETED_LEVEL_ZERO) ?: false
    }


    suspend fun setHighScore(score: Long) {
        com.roy.data.DataStoreHelper.dataStore?.edit {
            it[com.roy.data.DataStoreHelper.HIGH_SCORE] = score
            it[com.roy.data.DataStoreHelper.MAX_LEVELS] = com.roy.data.LevelInfo.level
        }
    }

    internal fun getHighScore(): Flow<Long> = com.roy.data.DataStoreHelper.dataStore?.data?.map { preference ->
        preference[com.roy.data.DataStoreHelper.HIGH_SCORE] ?: 0
    } ?: flowOf(0)

    internal fun getMaxLevels(): Flow<Int> = com.roy.data.DataStoreHelper.dataStore?.data?.map { preference ->
        preference[com.roy.data.DataStoreHelper.MAX_LEVELS] ?: 0
    } ?: flowOf(0)

}
