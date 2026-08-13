package com.lcg.plus

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lcg.annotation.AutoField

class MainActivity : AppCompatActivity() {
    @AutoField
    var a = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tv = findViewById<TextView>(R.id.tv)
        tv.setOnClickListener {
            a++
            try {
                tv.text =
                    a.toString() + "\n" + Class.forName("com.lcg.plus.MainActivityAutoFieldExtras").declaredMethods.joinToString(
                        separator = "\n"
                    )
            } catch (e: Exception) {
                e.printStackTrace()
                tv.text = e.message
            }
        }
        tv.performClick()
        tv.setOnLongClickListener {
            Main2Activity.start(this,1)
            Main3Activity.start(this)
            true
        }
    }
}
