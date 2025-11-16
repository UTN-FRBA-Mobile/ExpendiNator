package ar.edu.utn.frba.ExpendinatorApp

import android.app.Application
import ar.edu.utn.frba.expendinator.data.remote.ApiClient

class ExpendinatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}