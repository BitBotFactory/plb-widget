package com.artem.lendingwidget.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.text.format.DateFormat
import android.widget.RemoteViews
import com.artem.lendingwidget.LendingWidgetConfigureActivity
import com.artem.lendingwidget.R
import com.artem.lendingwidget.extensions.*
import java.util.*


class LendingWidgetUI(val context: Context, val appWidgetManager: AppWidgetManager, val widgetId: Int) {

    internal fun updateViews() {
        val views = RemoteViews(context.packageName, R.layout.lending_widget)
        updateBotlogViews(views)
        updateBpiViews(views)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun updateBotlogViews(views: RemoteViews) {
        val botlog = context.getBotlog(widgetId) ?: return
        val cryptoCurrency = context.getCryptoCurrency(widgetId)
        val data = botlog!!.raw_data[cryptoCurrency] ?: return
        val initialCurrency = context.getInitialAmount(widgetId)
        val earnedCurrency = data!!.maxToLend - initialCurrency
        val earnedPercentage = ((earnedCurrency / initialCurrency) * 100)
        val initialDate = DateFormat.format("d.M.yyyy", Date(context.getInitialDate(widgetId)))
        val lastUpdateDate = DateFormat.format("d.M.yyyy HH:mm", botlog.last_update)

        views.setTextViewText(R.id.tv_crypto_amount, String.format("%.3f $cryptoCurrency", data!!.maxToLend))
        views.setTextViewText(R.id.tv_earnings, String.format("+%.3f (%.1f%%) since $initialDate", earnedCurrency, earnedPercentage))
        views.setTextViewText(R.id.tv_average, String.format("%.3f%% current avg.", data!!.averageLendingRate))
        views.setTextViewText(R.id.tv_last_update, String.format("(last update $lastUpdateDate)"))
        views.setOnClickPendingIntent(R.id.btn_settings, LendingWidgetConfigureActivity.startIntent(context, widgetId))
    }

    private fun updateBpiViews(views: RemoteViews) {
        val bpiRate = context.getCoinDeskRate(widgetId)
        val currency = context.getCurrency(widgetId)
        val botlog = context.getBotlog(widgetId) ?: return
        val cryptoCurrency = context.getCryptoCurrency(widgetId)
        val data = botlog!!.raw_data[cryptoCurrency] ?: return

        views.setTextViewText(R.id.tv_currency_amount, String.format("%.0f $currency", bpiRate * data!!.maxToLend))
    }

}