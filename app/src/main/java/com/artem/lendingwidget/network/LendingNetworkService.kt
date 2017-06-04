package com.artem.lendingwidget.network

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.artem.lendingwidget.LendingWidgetUI
import com.artem.lendingwidget.data.Botlog
import com.artem.lendingwidget.data.CoinDeskRate
import com.artem.lendingwidget.extensions.*
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL



private const val EXTRA_NOTIFY = "com.artem.lendingwidget.extra.NOTIFY"
private const val EXTRA_WIDGET_ID = "com.artem.lendingwidget.extra.WIDGET_ID"

private const val COINDESK_PATH_FORMAT = "http://api.coindesk.com/v1/bpi/currentprice/%s.json"

class LendingNetworkService : IntentService("LendingNetworkService") {

    // Always true. For now there is no reason not to update the views after retrieving data.
    var updateViews = true
    // We want to update the UI from the service so we don't rely on the starter of the service still being alive.
    lateinit var lendingWidgetUI: LendingWidgetUI

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val notify = intent.getBooleanExtra(EXTRA_NOTIFY, false)
            val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID || this.getUrl(widgetId).isBlank()) {
                return
            }

            lendingWidgetUI = LendingWidgetUI(this, AppWidgetManager.getInstance(this), widgetId)
            when (intent.action) {
                ACTION_BOTLOG -> handleActionBotlog(notify,widgetId)
                ACTION_BPI -> handleActionBpi(notify, widgetId)
            }
        }
    }

    private fun handleActionBotlog(notify: Boolean, widgetId: Int) {
        val result = try {
            // TODO handle https
            val url = URL("http://${this.getUrl(widgetId)}")
            val reader: BufferedReader = BufferedReader(InputStreamReader(url.openStream()))
            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
            gson.fromJson<Botlog>(reader)
        } catch (e: Exception) {
            this.logError(e)
            e.printStackTrace()
            null
        }
        if (result != null) {
            this.storeBotlog(result, widgetId)
            if (updateViews) {
                lendingWidgetUI.updateViews()
            }
        }
        if (notify) {
            sendResult(ACTION_BOTLOG, widgetId, result != null)
        }
    }

    private fun handleActionBpi(notify: Boolean, widgetId: Int) {
        val result = try {
            val url = URL(String.format(COINDESK_PATH_FORMAT, this.getCurrency(widgetId)))
            val reader: BufferedReader = BufferedReader(InputStreamReader(url.openStream()))
            Gson().fromJson<CoinDeskRate>(reader)
        } catch (e: Exception) {
            this.logError(e)
            e.printStackTrace()
            null
        }
        if (result != null) {
            this.storeCoinDeskRate(result, widgetId)
            if (updateViews) {
                lendingWidgetUI.updateViews()
            }
        }
        if (notify) {
            sendResult(ACTION_BPI, widgetId, result != null)
        }
    }

    private fun sendResult(originalAction: String, widgetId: Int, success: Boolean) {
        val intent = Intent(ACTION_RESULT) //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra(RESULT_SUCCESS, success)
        intent.putExtra(RESULT_WIDGET_ID, widgetId)
        intent.putExtra(RESULT_ACTION, originalAction)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {

        const val RESULT_SUCCESS = "RESULT_SUCCESS"
        const val RESULT_WIDGET_ID = "RESULT_WIDGET_ID"
        const val RESULT_ACTION = "RESULT_ACTION"

        const val ACTION_RESULT = "com.artem.lendingwidget.action.ACTION_RESULT"

        const val ACTION_BOTLOG = "com.artem.lendingwidget.action.BOTLOG"
        const val ACTION_BPI = "com.artem.lendingwidget.action.BPI"

        fun updateBotlog(context: Context, notify: Boolean, widgetId: Int) {
            val intent = Intent(context, LendingNetworkService::class.java)
            intent.action = ACTION_BOTLOG
            intent.putExtra(EXTRA_NOTIFY, notify)
            intent.putExtra(EXTRA_WIDGET_ID, widgetId)
            context.startService(intent)
        }

        fun updateBpi(context: Context, notify: Boolean, widgetId: Int) {
            val intent = Intent(context, LendingNetworkService::class.java)
            intent.action = ACTION_BPI
            intent.putExtra(EXTRA_NOTIFY, notify)
            intent.putExtra(EXTRA_WIDGET_ID, widgetId)
            context.startService(intent)
        }
    }
}
