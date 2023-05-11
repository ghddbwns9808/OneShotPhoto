package org.techtown.oneshotphoto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TipActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip)

        val btnBack = findViewById<FloatingActionButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }
}