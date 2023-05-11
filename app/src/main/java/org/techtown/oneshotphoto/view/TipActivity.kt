package org.techtown.oneshotphoto.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.techtown.oneshotphoto.R

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