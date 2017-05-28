package com.artem.lendingwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.artem.lendingwidget.extensions.deleteWidgetData
import com.artem.lendingwidget.network.LendingNetworkService

class LendingWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            LendingNetworkService.updateBotlog(context, false, appWidgetId)
            LendingNetworkService.updateBpi(context, false, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            context.deleteWidgetData(appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {

    }

    override fun onDisabled(context: Context) {

    }

}

