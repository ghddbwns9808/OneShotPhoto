package org.techtown.oneshotphoto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.techtown.oneshotphoto.databinding.ActivityJoinBinding

class JoinActivity : AppCompatActivity() {
    lateinit var binding: ActivityJoinBinding

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnStartJoin.setOnClickListener {
            if(binding.etJoinEmail.text.toString().trim().isEmpty() || binding.etJoinPassword.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
            } else{
                val email = binding.etJoinEmail.text?.toString()
                val password = binding.etJoinPassword.text?.toString()
                auth.createUserWithEmailAndPassword(email!!, password!!)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            val user = auth.currentUser
                            Toast.makeText(this, "회원가입 성공" , Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(baseContext, " 회원가입 실패", Toast.LENGTH_SHORT).show()

                        }
                    }
            }
        }
    }
}