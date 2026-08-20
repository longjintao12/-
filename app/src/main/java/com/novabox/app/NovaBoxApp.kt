package com.novabox.app

import android.app.Application
import com.novabox.app.data.repo.SourceRepo
import com.novabox.app.util.Prefs

class NovaBoxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        SourceRepo.init(this)
    }
}
