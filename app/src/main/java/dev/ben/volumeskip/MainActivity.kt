package dev.ben.volumeskip

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.enable_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        val enabled = isServiceEnabled()
        findViewById<TextView>(R.id.status_text).text =
            getString(if (enabled) R.string.status_enabled else R.string.status_disabled)
        findViewById<Button>(R.id.enable_button).text =
            getString(if (enabled) R.string.open_settings else R.string.enable_service)
    }

    private fun isServiceEnabled(): Boolean {
        val target = ComponentName(this, VolumeSkipService::class.java)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(':')
            .any { ComponentName.unflattenFromString(it) == target }
    }
}
