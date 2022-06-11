package com.example.home_work_12

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
            // Меняем статус загрузки
            view.findViewById<TextView>(R.id.progressInfo)?.text = getString(R.string.loadStr)
            // Инициазируем ракету
            val img = view.findViewById<ImageView>(R.id.rocket)
            img?.rotation = 0f
            img?.visibility = View.VISIBLE
            // Грузим данные
            loadData()
        }
    }

    /**
     * Переместить ракету
     */
    private fun imgTransition(reverse: Boolean = false) {
        val img = view?.findViewById<ImageView>(R.id.rocket)
        val layout = view?.findViewById<ConstraintLayout>(R.id.constrLayout)
        val destination = if (reverse) { 0f } else { -(layout?.height?.toFloat())!! }
        val animator = ObjectAnimator.ofFloat(img, "translationY", destination)
        animator.duration = 4000
        img?.post {
            animator.start()
        }
    }

    /**
     * Повернуть ракету
     */
    private fun imgRotate() {
        val img = view?.findViewById<ImageView>(R.id.rocket)
        val animator = ObjectAnimator.ofFloat(img, "rotation", 0f, 180f)
        img?.post {
            animator.start()
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
        // Устанавливаем доступность загрузки данных
        val loadBtn = view?.findViewById<Button>(R.id.load)
        loadBtn?.isEnabled = false
        val observer = object: Observer<Int> {
            override fun onSubscribe(d: Disposable) {
                view?.findViewById<ImageView>(R.id.rocket)?.post {
                    imgTransition()
                }
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
                loadBtn?.post {
                    // Устанавливаем доступность загрузки данных
                    loadBtn.isEnabled = true
                    // Устанавливаем статус загрузки
                    view?.findViewById<TextView>(R.id.progressInfo)?.text = getString(R.string.loadFinish)
                    view?.findViewById<TextView>(R.id.progress)?.text = ""
                    view?.findViewById<ImageView>(R.id.rocket)?.visibility = View.GONE
                }
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
                    // логируем
                    Log.d(getRxTag(), "next value = $it")
                    Log.d(getRxTag(), "Ой, Ошибка начинай обратный отсчет")
                    // Меняем направление ракеты и задаем движение
                    imgRotate()
                    imgTransition(true)
                    // Устанавливаем статус загрузки
                    view?.findViewById<TextView>(R.id.progressInfo)?.text = getString(R.string.revertLoad)
                    setProgress(it)
                    // Бросаем ошибку
                    throw Throwable("Ой, Ошибка начинай обратный отсчет")
                }
                it
            }
            .onErrorResumeNext(
                Observable
                    .fromArray(*getData(dataSize = loadDataSize, reverse = true))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
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
