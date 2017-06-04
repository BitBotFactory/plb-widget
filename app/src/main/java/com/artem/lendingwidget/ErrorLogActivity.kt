package com.artem.lendingwidget

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.TextView
import com.artem.lendingwidget.extensions.loadErrorLog


class ErrorLogActivity : AppCompatActivity() {

    internal lateinit var mErrorLogText : TextView

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.error_log)

        val actionBar = findViewById(R.id.toolbar_error_log) as Toolbar?
        setSupportActionBar(actionBar)

        mErrorLogText = findViewById(R.id.tv_error_log) as TextView
    }

    override fun onResume() {
        super.onResume()
        val log = this.loadErrorLog()
        mErrorLogText.text = log.reduce({ s1: String, s2: String -> s1 + s2})
    }
}