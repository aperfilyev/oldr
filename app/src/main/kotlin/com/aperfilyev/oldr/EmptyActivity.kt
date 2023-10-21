package com.aperfilyev.oldr

import android.content.Intent
import android.os.Bundle
import android.provider.Browser
import androidx.activity.ComponentActivity

private const val REPLACEMENT = "old.reddit.com"
private const val PATTERN = "(.*\\.)?reddit\\.com"

class EmptyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        val uri = intent.data
        if (uri != null) {
            val authority = uri.authority!!
            val uri = uri.buildUpon()
                .authority(authority.replaceFirst(PATTERN.toRegex(), REPLACEMENT))
                .build()
            val intent = Intent(Intent.ACTION_VIEW, uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Browser.EXTRA_APPLICATION_ID, extras?.getString(Browser.EXTRA_APPLICATION_ID))
            startActivity(intent)
        }
        finish()
    }
}