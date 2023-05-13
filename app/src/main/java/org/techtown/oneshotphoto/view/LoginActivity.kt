package org.techtown.oneshotphoto.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.techtown.oneshotphoto.AutoLoginSharedPrefs
import org.techtown.oneshotphoto.R
import org.techtown.oneshotphoto.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = Firebase.auth

        //자동 로그인 검사
        isAutoLogin()

        binding.autoLogin.isClickable = true
        binding.autoLogin.setOnClickListener {
            binding.autoLogin.toggle()
        }

    }

    private fun isAutoLogin(){
        if (AutoLoginSharedPrefs(this).autoLogin!!){
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }
    }

    private fun setAutoLogin(){
        if (binding.autoLogin.isChecked)
            AutoLoginSharedPrefs(this).autoLogin = true
    }

    fun btnLoginClicked(view: View){
        binding.btnLogin.setOnClickListener {
            if(binding.etEmail.text.toString().trim().isEmpty() || binding.etPassword.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
            } else{
                val email = binding.etEmail.text?.toString()
                val password = binding.etPassword.text?.toString()

                auth.signInWithEmailAndPassword(email!!, password!!)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            //자동 로그인 설정
                            setAutoLogin()
                            loginSuccess()
                        } else {
                            Toast.makeText(this, "이메일 또는 비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    fun btnJoinClicked(view: View){
        binding.btnJoin.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }
    }

    fun btnNonUserClicked(view: View){
        binding.btnNonUser.setOnClickListener {
            auth.signInAnonymously().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    loginSuccess()
                } else {
                    Toast.makeText(baseContext, "익명으로 로그인 실패.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loginSuccess(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}