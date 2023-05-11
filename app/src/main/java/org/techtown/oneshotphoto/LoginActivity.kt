package org.techtown.oneshotphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.techtown.oneshotphoto.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding  = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth


        binding.btnLogin.setOnClickListener {
            if(binding.etEmail.text.toString().trim().isEmpty() || binding.etPassword.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
            } else{
                val email = binding.etEmail.text?.toString()
                val password = binding.etPassword.text?.toString()

                auth.signInWithEmailAndPassword(email!!, password!!)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            val user = auth.currentUser
                            //Toast.makeText(this,"로그인 성공 uid: ${user!!.uid}", Toast.LENGTH_SHORT).show()
                            AUTH = YES
                            val mainIntent = Intent(this, MainActivity::class.java)
                            startActivity(mainIntent)
                        } else {

                            Toast.makeText(this, "이메일 또는 비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.btnJoin.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        binding.btnNonUser.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //Toast.makeText(this, "익명으로 로그인 성공", Toast.LENGTH_SHORT).show()

                        AUTH = NON

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(baseContext, "익명으로 로그인 실패.", Toast.LENGTH_SHORT).show()

                    }
                }
        }

    }
}