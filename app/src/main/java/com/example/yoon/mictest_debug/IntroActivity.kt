package com.example.yoon.mictest_debug

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        btn_200.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("interval",200)
            startActivity(intent)
            finish()
        }
        btn_400.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("interval",400)
            startActivity(intent)
            finish()
        }
        btn_600.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("interval",600)
            startActivity(intent)
            finish()
        }
        btn_800.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("interval",800)
            startActivity(intent)
            finish()
        }
        btn_1000.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("interval",1000)
            startActivity(intent)
            finish()
        }
    }
}
