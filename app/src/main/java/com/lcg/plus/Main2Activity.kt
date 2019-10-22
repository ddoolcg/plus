package com.lcg.plus

import android.app.Activity
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.lcg.annotation.AutoField

class Main2Activity : AppCompatActivity() {
    @AutoField("sadas")
    var c = 1
    @AutoField("ewrxx")
    var b = false
    @AutoField("SadaA")
    lateinit var list: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        println("$c-------$b----------${list.joinToString()}")
        val extras = intent.extras
        println(
            "${intent.getIntExtra("sadas", -1)}-------${extras?.getInt("sadas", -1)}"
        )
    }

    companion object {
        fun start(activity: Activity) {
            val list = arrayListOf("A", "B")
            val intent = IntentMain2ActivityBuilder(activity)
                .setB(true)
                .setC(1)
                .setList(arrayListOf("A", "B"))
                .build()
            intent.putStringArrayListExtra("SadaA", list)
            activity.startActivity(intent)
        }
    }

    class A : BaseObservable() {
        @get:Bindable
        var b = 1
    }
}
