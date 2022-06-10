package com.example.home_work_12

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class Fragment : Fragment() {
    @Inject
    lateinit var presenter: Presenter
    private val observable: Observable<Int> = Observable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeOnDataLoad()
        view.findViewById<Button>(R.id.load).setOnClickListener {
            subscribeOnDataLoad()
        }
    }

    override fun onStart() {
        super.onStart()

        // инициализируем даггер компонент
        val diComponent = Application().getDIComponent()
        diComponent?.inject(this)
        val daggerInfo = view?.findViewById<TextView>(R.id.daggerInfo)
        daggerInfo?.text = presenter.message
    }

    override fun onDestroy() {
        super.onDestroy()
        // очищаем даггер компонент
        Application().clearComponent()
    }

    fun subscribeOnDataLoad() {
        val progress = view?.findViewById<TextView>(R.id.progress)
        val lastElem = 13
        val observer = object: Observer<Int> {
            override fun onSubscribe(d: Disposable) {
                Log.d("subscribe", "Подписались на получение данных")
            }

            override fun onNext(t: Int) {
                progress?.text = "${(lastElem/t) * 100}%"
                Log.d("data", t.toString())
                /*if (t == lastElem) {
                    throw Throwable("Случилась ошибка, но вы не переживайте")
                }*/
            }

            override fun onError(e: Throwable) {
                Log.d("error", e.message.toString())
            }

            override fun onComplete() {
                Log.d("complete", "Сори, Данные закончились")
            }

        }
        observable
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }
}