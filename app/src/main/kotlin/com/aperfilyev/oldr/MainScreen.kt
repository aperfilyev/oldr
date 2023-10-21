package com.aperfilyev.oldr

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val SHARED_PREFS_NAME = "oldr_prefs"
private const val SHARED_PREFS_KEY = "redirect_enabled"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) })

            val context = LocalContext.current
            val sharedPreferences = remember {
                context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            }
            val initialValue = remember { sharedPreferences.getBoolean(SHARED_PREFS_KEY, true) }
            val checked by sharedPreferences.changes().collectAsState(initial = initialValue)
            LaunchedEffect(key1 = checked, block = {
                val pm = context.packageManager
                val compName = ComponentName(context, EmptyActivity::class.java)
                val flag = if (checked) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                pm.setComponentEnabledSetting(compName, flag, PackageManager.DONT_KILL_APP)
            })
            val lambda = remember {
                { sharedPreferences.edit { putBoolean(SHARED_PREFS_KEY, !checked) } }
            }

            ThemedListItem(
                headlineText = {
                    Text(
                        text = "Enable redirect",
                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    )
                },
                trailingContent = { Switch(checked = checked, onCheckedChange = { lambda() }) },
                onClick = lambda
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Text(
                    modifier = Modifier
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    text = stringResource(R.string.android_s_warning)
                )
                Button(onClick = {
                    context.startActivity(getIntent(context))
                }) {
                    Text(text = "Open Settings")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedListItem(
    headlineText: @Composable () -> Unit,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ListItem(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        headlineText = headlineText,
        trailingContent = trailingContent
    )
}

@RequiresApi(Build.VERSION_CODES.S)
fun getIntent(context: Context): Intent {
    val uri = Uri.parse("package:${context.packageName}")
    val intent = if (Build.MANUFACTURER.contains("samsung", true)) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
    } else {
        Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, uri)
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
}

fun SharedPreferences.changes(): Flow<Boolean> = callbackFlow {
    val callback = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        trySend(sharedPreferences.getBoolean(key, true))
    }
    registerOnSharedPreferenceChangeListener(callback)
    awaitClose { unregisterOnSharedPreferenceChangeListener(callback) }
}