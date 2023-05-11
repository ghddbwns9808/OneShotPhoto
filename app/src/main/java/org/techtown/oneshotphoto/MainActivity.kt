package org.techtown.oneshotphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import org.techtown.oneshotphoto.databinding.ActivityJoinBinding
import org.techtown.oneshotphoto.databinding.ActivityMainBinding


val TAG : String = "ghddbwns"
val FLAG_CALL: Int = 301

var CROP_FILTER: String = "filter_cropped"
val YCROP_FILTER:String = "filterYes"
val NCROP_FILTER:String = "filterNo"

var CROP_ORIGINAL:String = "original_cropped"
val YCROP_ORIGINAL:String = "originalYes"
val NCROP_ORIGINAL:String = "originalNo"

val NON: Int = 1000
val YES:Int = 2000
var AUTH: Int = 3000

lateinit var b64:String
val url:String = "http://ec2-52-204-253-11.compute-1.amazonaws.com:5000/"
//val url:String = "https://groomflaskserver-kxkhm.run.goorm.io/"

var MODE: Int= 10000
val CREATE: Int = 10001
val MYFILTER:Int = 10002
val BASICFILTER: Int = 10003

class MainActivity : AppCompatActivity() {

    var lastTimeBackPressed : Long = 0

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(AUTH == NON){
            Toast.makeText(this, "비회원으로 로그인 했습니다.", Toast.LENGTH_SHORT).show()
        } else if(AUTH == YES){
            Toast.makeText(this, "회원으로 로그인 했습니다.", Toast.LENGTH_SHORT).show()
        }


        binding.btnCreateFilter.setOnClickListener {
            val intent = Intent(this, CreateFilterActivity::class.java)
            startActivity(intent)
        }

        binding.btnBasicFilter.setOnClickListener {
            val intent = Intent(this, BasicFilterActivity::class.java)
            startActivity(intent)
        }

        binding.btnMyFilter.setOnClickListener {
            if(AUTH == NON){ // 비회원일 때 사용 불가
                Toast.makeText(this, "로그인이 필요한 기능입니다.", Toast.LENGTH_SHORT).show()
            }else{
                val intent = Intent(this, MyFilterActivity::class.java)
                startActivity(intent)
            }
        }

        binding.btnTip.setOnClickListener {
            val intent = Intent(this, TipActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() - lastTimeBackPressed >= 1500){
            lastTimeBackPressed = System.currentTimeMillis()
            Toast.makeText(this,"'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG).show() }
        else {
            ActivityCompat.finishAffinity(this)
            System.runFinalization()
            System.exit(0)
        }
    }
}