package com.example.home_work_12

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.MainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class Fragment : Fragment() {
    @Inject lateinit var presenter: Presenter
    private lateinit var observable: Observable<Int>;
    private val loadDataSize = 14
    private val lastElem = loadDataSize - 1
    private var listData: Array<Int>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // инициализируем даггер компонент
        val diComponent = Application().getDIComponent()
        diComponent?.inject(this)
        val daggerInfo = view?.findViewById<TextView>(R.id.daggerInfo)
        daggerInfo?.text = presenter.message

        // загружаем данные
        observable = getObservable()
        loadData()

        // Подписка на кнопку загрузки данных
        view.findViewById<Button>(R.id.load).setOnClickListener {
            view.findViewById<TextView>(R.id.progressInfo)?.text = getString(R.string.loadStr)
            loadData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // очищаем даггер компонент
        Application().clearComponent()
    }

    /**
     * Загружаем данные
     */
    private fun loadData() {
        val observer = object: Observer<Int> {
            override fun onSubscribe(d: Disposable) {
                Log.d(getRxTag(), "Подписались")
            }

            override fun onNext(t: Int) {
                Thread.sleep(300)
                setProgress((t))
                Log.d(getRxTag(), "next value = $t")
            }

            override fun onError(e: Throwable) {
                Log.d(getRxTag(), e.message.toString())
            }

            override fun onComplete() {
                Log.d(getRxTag(), "БУМ! Закончили")
            }
        }
        observable.subscribe(observer)
    }

    /**
     * Установить прогресс загрузки
     */
    @SuppressLint("SetTextI18n")
    private fun setProgress(elemNum: Int) {
        val progress = view?.findViewById<TextView>(R.id.progress)
        val percent = (elemNum * 100) / lastElem
        progress?.post {
            progress.text = "${percent}%"
        }
    }

    /**
     * Получить объект Observable
     */
    private fun getObservable(): Observable<Int> {
        return Observable
            .fromArray(*getData(dataSize = loadDataSize))
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .map {
                if (it == lastElem ) {
                    // так сделано потому что из за подписки onErrorResumeNext он не идет в onError
                    Log.d(getRxTag(), "next value = $it")
                    setProgress(it)
                    Log.d(getRxTag(), "Ой, Ошибка начинай обратный отсчет")
                    view?.findViewById<TextView>(R.id.progressInfo)?.text = getString(R.string.revertLoad)
                    throw Throwable("Ой, Ошибка начинай обратный отсчет")
                }
                it
            }
            .onErrorResumeNext(
                Observable
                    .fromArray(*getData(dataSize = loadDataSize, reverse = true))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
            )
            .doFinally {
                // отписка происходит по окончанию получения данных
                Log.d(getRxTag(), "Потока больше нет, отписались")
            }
    }

    /**
     * Метод получения данных с БЛ
     */
    private fun getData(reverse: Boolean = false, dataSize: Int = 14): Array<Int> {
        val data = listData ?: Array(dataSize) { init -> init }
        // кешируем данные
        if (listData === null) {
            listData = data
        }
        if (reverse) {
            data.reverse()
        }
        return data
    }

    /**
     * Получить тег для логов
     */
    fun getRxTag(): String {
        return "RX_TAG[${Thread.currentThread().name}"
    }
}
