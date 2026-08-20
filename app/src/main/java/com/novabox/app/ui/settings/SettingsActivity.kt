package com.novabox.app.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.novabox.app.R
import com.novabox.app.data.db.AppDbHelper
import com.novabox.app.databinding.ActivitySettingsBinding
import com.novabox.app.util.Prefs

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            findPreference<Preference>("clear_data")?.setOnPreferenceClickListener {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("确认清除")
                    .setMessage("将清除所有收藏、历史、设置数据，确定？")
                    .setPositiveButton("确定") { _, _ ->
                        val db = AppDbHelper.get(requireContext())
                        db.clearHistory()
                        db.clearFavorites()
                        Prefs.autoPlay = true
                        Prefs.bannerEnable = true
                        Prefs.userAgent = Prefs.DEFAULT_UA
                        Prefs.referer = ""
                        Prefs.cookie = ""
                        Toast.makeText(requireContext(), "已清除", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("取消", null)
                    .show()
                true
            }

            findPreference<Preference>("about")?.let { pref ->
                pref.summary = "版本 1.0.0 · MIT License"
            }
        }
    }
}
