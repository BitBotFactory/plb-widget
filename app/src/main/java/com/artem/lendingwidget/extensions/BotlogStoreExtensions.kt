package com.artem.lendingwidget.extensions

import android.content.Context
import com.artem.lendingwidget.data.Botlog
import com.artem.lendingwidget.data.CoinDeskRate
import com.artem.lendingwidget.data.CryptoCurrency
import com.artem.lendingwidget.data.Currency
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson

const val PREFS_NAME = "de.artem.lendingwidget.LendingWidget"

const val PREF_INITIAL_AMOUNT = "INITIAL_AMOUNT_"
const val PREF_INITIAL_DATE = "INITIAL_DATE_"
const val PREF_BOTLOG = "BOTLOG_"
const val PREF_URL = "URL_"
const val PREF_CRYPTO_CURRENCY = "CRYPTO_CURRENCY_"
const val PREF_TARGET_CURRENCY = "TARGET_CURRENCY_"
const val PREF_BPI_VALUE = "BPI_VALUE_"

fun Context.storeBotlog(botlog: Botlog, widgetId: Int) {
    val prefs = this.getSharedPreferences(PREFS_NAME, 0)
    val edit = prefs.edit()
    val currency = this.getCryptoCurrency(widgetId)

    if (prefs.getFloat(PREF_INITIAL_AMOUNT + widgetId, -1f) == -1f) run {
        val rawData = botlog.raw_data[currency] ?: return@run
        edit.putFloat(PREF_INITIAL_AMOUNT + widgetId, rawData.maxToLend)
        edit.putLong(PREF_INITIAL_DATE + widgetId, System.currentTimeMillis())
    }
    edit.putString(PREF_BOTLOG + widgetId, Gson().toJson(botlog))

    edit.apply()
}

fun Context.storeUrl(url: String, widgetId: Int) {
    val prefs = this.getSharedPreferences(PREFS_NAME, 0)
    val edit = prefs.edit()

    edit.putString(PREF_URL + widgetId, url)
    edit.apply()
}

fun Context.storeCoinDeskRate(coinDeskRate: CoinDeskRate, widgetId: Int) {
    val prefs = this.getSharedPreferences(PREFS_NAME, 0)
    val edit = prefs.edit()
    val currency = this.getCurrency(widgetId)

    coinDeskRate.bpi[currency] ?: return

    edit.putFloat(PREF_BPI_VALUE + widgetId, coinDeskRate.bpi[currency]!!.rate_float)
    edit.apply()
}

fun Context.getCoinDeskRate(widgetId: Int): Float {
    return this.getSharedPreferences(PREFS_NAME, 0).getFloat(PREF_BPI_VALUE + widgetId, 0f)
}

fun Context.getBotlog(widgetId: Int): Botlog? {
    val json: String = this.getSharedPreferences(PREFS_NAME, 0).getString(PREF_BOTLOG + widgetId, "null")

    return try {
        Gson().fromJson<Botlog>(json)
    } catch (e : Exception) {
        null
    }
}

fun Context.getCryptoCurrency(widgetId: Int): CryptoCurrency {
    return CryptoCurrency.valueOf(this.getSharedPreferences(PREFS_NAME, 0).getString(PREF_CRYPTO_CURRENCY + widgetId, "BTC"))
}

fun Context.getCurrency(widgetId: Int): Currency {
    return Currency.valueOf(this.getSharedPreferences(PREFS_NAME, 0).getString(PREF_TARGET_CURRENCY + widgetId, "EUR"))
}

fun Context.storeTargetCurrency(widgetId: Int, currency: String) {
    val prefs = this.getSharedPreferences(PREFS_NAME, 0)
    val edit = prefs.edit()

    edit.putString(PREF_TARGET_CURRENCY + widgetId, currency)
    edit.apply()
}

fun Context.getUrl(widgetId: Int): String {
    return this.getSharedPreferences(PREFS_NAME, 0).getString(PREF_URL + widgetId, "")
}

fun Context.getInitialAmount(widgetId: Int): Float {
    return this.getSharedPreferences(PREFS_NAME, 0).getFloat(PREF_INITIAL_AMOUNT + widgetId, 0f)
}

fun Context.getInitialDate(widgetId: Int): Long {
    return this.getSharedPreferences(PREFS_NAME, 0).getLong(PREF_INITIAL_DATE + widgetId, 0L)
}

fun Context.deleteWidgetData(widgetId: Int) {
    this.getSharedPreferences(PREFS_NAME, 0).edit()
            .remove(PREF_URL + widgetId)
            .remove(PREF_BOTLOG + widgetId)
            .remove(PREF_INITIAL_DATE + widgetId)
            .remove(PREF_CRYPTO_CURRENCY + widgetId)
            .remove(PREF_INITIAL_AMOUNT + widgetId)
            .apply()
}