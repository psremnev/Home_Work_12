package com.example.home_work_12

import androidx.lifecycle.MutableLiveData

class Presenter {
    val loadedData: MutableLiveData<Array<Int>> = MutableLiveData<Array<Int>>()
    val message: String = "Presenter has initialized by Dagger"
}