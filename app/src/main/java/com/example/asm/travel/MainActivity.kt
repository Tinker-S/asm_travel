package com.example.asm.travel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.asm.travel.target.Caller
import com.example.asm.travel.utils.ReflectUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.call_new_class).setOnClickListener {
            callNewClass()
        }

        findViewById<Button>(R.id.call_new_method).setOnClickListener {
            callNewMethod()
        }

        findViewById<Button>(R.id.call_modify_method).setOnClickListener {
            Caller().launch()
        }
    }

    /**
     * 使用 asm 给 Caller 类添加 log 方法
     */
    private fun callNewMethod() {
        ReflectUtil.callMethod(
            Caller(),
            ReflectUtil.findMethod(
                Caller::class.java,
                "log",
                String::class.java,
                String::class.java
            ),
            "MainActivity",
            "I'm a method which genereated by asm."
        )
    }

    /**
     * 使用 asm 新建 HelloAsm 类，并添加 showToast 方法
     */
    private fun callNewClass() {
        val clazz = Class.forName("com/example/asm/travel/autogen/HelloAsm".replace("/", "."))
        ReflectUtil.callStaticMethod(
            ReflectUtil.findMethod(
                clazz,
                "showToast",
                Context::class.java,
                String::class.java
            ),
            this,
            "I'm genereated by asm."
        )
    }
}