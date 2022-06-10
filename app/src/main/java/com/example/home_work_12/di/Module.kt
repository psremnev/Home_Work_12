package com.example.home_work_12.di

import com.example.home_work_12.Presenter
import dagger.Module
import dagger.Provides

@Module
class Module {
    @Provides
    fun getPresenter(): Presenter {
        return Presenter()
    }
}