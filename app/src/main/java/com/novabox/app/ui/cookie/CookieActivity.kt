package com.novabox.app.ui.cookie

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.novabox.app.databinding.ActivityCookieBinding
import com.novabox.app.util.Prefs

class CookieActivity : AppCompatActivity() {

    private lateinit var b: ActivityCookieBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCookieBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setNavigationOnClickListener { finish() }
        b.input.setText(Prefs.cookie)

        b.btnSave.setOnClickListener {
            Prefs.cookie = b.input.text.toString().trim()
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
