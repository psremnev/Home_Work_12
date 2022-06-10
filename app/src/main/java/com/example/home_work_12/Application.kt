package com.example.home_work_12

import android.app.Application
import com.example.home_work_12.di.Component
import com.example.home_work_12.di.DaggerComponent

class Application: Application() {
    private var diComponent: Component? = null;

    fun getDIComponent(): Component? {
        return DaggerComponent.create()
    }

    fun clearComponent() {
        diComponent = null
    }
}