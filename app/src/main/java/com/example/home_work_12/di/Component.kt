package com.example.home_work_12.di

import com.example.home_work_12.Fragment
import com.example.home_work_12.MainActivityScope
import dagger.Component

@Component(modules = [Module::class])
@MainActivityScope
interface Component {
    fun inject(frg: Fragment)
}