package com.example.home_work_12.di

import com.example.home_work_12.Fragment
import dagger.Component

@Component(modules = [Module::class])
interface Component {
    fun inject(frg: Fragment)
}