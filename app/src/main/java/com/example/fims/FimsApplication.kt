package com.example.fims

import android.app.Application
import com.example.fims.data.FimsDatabase

class FimsApplication : Application() {
    val database by lazy { FimsDatabase.getDatabase(this) }
}