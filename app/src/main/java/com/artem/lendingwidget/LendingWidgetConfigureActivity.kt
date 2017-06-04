package com.artem.lendingwidget


import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.artem.lendingwidget.data.Currency
import com.artem.lendingwidget.extensions.getCurrency
import com.artem.lendingwidget.extensions.getUrl
import com.artem.lendingwidget.extensions.storeTargetCurrency
import com.artem.lendingwidget.extensions.storeUrl
import com.artem.lendingwidget.network.LendingNetworkService



class LendingWidgetConfigureActivity : AppCompatActivity() {
    internal var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    internal lateinit var mUrlEditText: EditText
    internal lateinit var mConnectButton: Button
    internal lateinit var mProgressBar: ProgressBar
    internal lateinit var mTargetSpinner: Spinner

    internal val mBroadCastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
           this@LendingWidgetConfigureActivity.onReceive(context, intent)
        }
    }
    internal val mSpinnerSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {
        }

        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            this@LendingWidgetConfigureActivity.storeTargetCurrency(mAppWidgetId, Currency.values()[p2].name)
            LendingNetworkService.updateBpi(this@LendingWidgetConfigureActivity, false, mAppWidgetId)
        }

    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.lending_widget_configure)
        setSupportActionBar(findViewById(R.id.toolbar_config) as Toolbar?)

        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        mUrlEditText = findViewById(R.id.et_url) as EditText
        mConnectButton = findViewById(R.id.btn_connect) as Button
        mProgressBar = findViewById(R.id.progressBar) as ProgressBar
        mTargetSpinner = findViewById(R.id.spinner_target_currency) as Spinner

        mTargetSpinner.adapter = ArrayAdapter<Currency>(this, android.R.layout.select_dialog_item, Currency.values())
        mTargetSpinner.setSelection(this.getCurrency(mAppWidgetId).ordinal)
        mTargetSpinner.onItemSelectedListener = mSpinnerSelectedListener


        mConnectButton.setOnClickListener { tryConnection()}
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastReceiver, IntentFilter(LendingNetworkService.ACTION_RESULT))

        var url = this.getUrl(mAppWidgetId)
        mUrlEditText.setText(url)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.config_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.item_error_log -> {
                val intent = Intent(this@LendingWidgetConfigureActivity, ErrorLogActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleViews(active: Boolean) {
        if (active) {
            mUrlEditText.isEnabled = true
            mConnectButton.isEnabled = true
            mProgressBar.visibility = View.GONE
        } else {
            mUrlEditText.isEnabled = false
            mConnectButton.isEnabled = false
            mProgressBar.visibility = View.VISIBLE
        }
    }

    internal fun tryConnection() {
        toggleViews(false)

        // TODO handle https
        var url = mUrlEditText.text.toString()
        if (url.isBlank()) {
            return
        }
        if (url.startsWith("http://")) {
            url = url.substring(7)
        }
        this.storeUrl("${url}", mAppWidgetId)
        LendingNetworkService.updateBotlog(this, true, mAppWidgetId)
        LendingNetworkService.updateBpi(this, false, mAppWidgetId)
    }

    internal fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) {
            return
        }

        toggleViews(true)

        val success = intent.getBooleanExtra(LendingNetworkService.RESULT_SUCCESS, false)
        val originalAction = intent.getStringExtra(LendingNetworkService.RESULT_ACTION)
        val widgetId = intent.getIntExtra(LendingNetworkService.RESULT_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        if ((originalAction != LendingNetworkService.ACTION_BOTLOG) || (widgetId != mAppWidgetId)) {
            return
        }

        if (success) {
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            this.finish()
        } else {
            Toast.makeText(this, getString(R.string.error_connection, mUrlEditText.text), Toast.LENGTH_LONG).show()
        }
    }

    companion object {

        internal fun startIntent(context: Context, widgetId: Int): PendingIntent {
            val intent = Intent()
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    .setClass(context, LendingWidgetConfigureActivity::class.java)
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}

