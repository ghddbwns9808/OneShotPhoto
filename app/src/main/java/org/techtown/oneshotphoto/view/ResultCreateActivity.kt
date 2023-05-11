package org.techtown.oneshotphoto.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.techtown.oneshotphoto.api.IRetrofit
import org.techtown.oneshotphoto.R
import org.techtown.oneshotphoto.TrainingResult
import org.techtown.oneshotphoto.databinding.ActivityResultCreateBinding
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern

class ResultCreateActivity : AppCompatActivity() {

    val FLAG_PRERM_DOWNLOAD = 201

    lateinit var binding: ActivityResultCreateBinding
    lateinit var photoUri: Uri

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val userID = auth.uid

        val dialogView = layoutInflater.inflate(R.layout.save_dialog,null)
        val dialogText = dialogView.findViewById<TextInputEditText>(R.id.etDlgName)
        val dialogBtn = dialogView.findViewById<MaterialButton>(R.id.btnDlgSave)

        val builder = AlertDialog.Builder(this).setView(dialogView).create()

        dialogText.filters


        var bitmapDecode = stringToDecode(b64)
        binding.imgRC.setImageBitmap(bitmapDecode)

        val retrofit = Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build()
        val service = retrofit.create(IRetrofit::class.java)


        //다운로드 버튼 구현
        binding.btnRCDownload.setOnClickListener {
            //권한 체크
            if(!checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                !checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                return@setOnClickListener
            }
            //그림 저장
            if(!imageExternalSave(applicationContext, bitmapDecode, applicationContext.getString(R.string.app_name))){
                Toast.makeText(applicationContext, "사진 저장을 실패하였습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(applicationContext, "사진이 갤러리에 저장되었습니다", Toast.LENGTH_SHORT).show()
        }


        //공유 버튼 구현
        binding.btnRCShare.setOnClickListener {
            photoUri = getImageUri(applicationContext, bitmapDecode)!!
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = ("image/*")
            intent.putExtra(Intent.EXTRA_STREAM, photoUri)
            startActivity(Intent.createChooser(intent, "Share Image"))
        }


        //새작업 버튼 구현
        binding.btnRCNew.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        //필터저장 버튼 구현
        binding.btnRCSave.setOnClickListener {
            if(MODE == CREATE) {
                if (AUTH == NON) {
                    Toast.makeText(this, "로그인이 필요한 기능입니다.", Toast.LENGTH_SHORT).show()
                } else {

                    builder.show()

                    dialogBtn.setOnClickListener {
                        val name = dialogText.text.toString()
                        if (name.isEmpty()) {
                            Toast.makeText(this, "올바른 필터 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        } else {

                            if (!Pattern.matches("^[a-zA-Z0-9_-]*$", name)) {
                                Toast.makeText(
                                    this,
                                    "필터 이름은 영어,숫자,-,_ 의 조합으로 입력해주세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val uid = userID!!
                                val filterName = dialogText.text.toString()
                                service.save(uid, filterName)
                                    .enqueue(object : retrofit2.Callback<TrainingResult> {
                                        override fun onResponse(
                                            call: Call<TrainingResult>,
                                            response: Response<TrainingResult>
                                        ) {
                                            Log.d(TAG, "onResponse: ${response.body().toString()}")
                                            Toast.makeText(baseContext, "$filterName 필터 저장", Toast.LENGTH_SHORT).show()
                                            Handler().postDelayed({
                                                builder.dismiss()
                                            }, 700)
                                        }

                                        override fun onFailure(call: Call<TrainingResult>, t: Throwable) {
                                            Log.d(TAG, "onFailure: ")
                                        }

                                    })
                            }
                        }
                    }
                }
            }else{
                Toast.makeText(baseContext, "이미 저장된 필터입니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //base64를 Bitmap으로 디코딩
    private fun stringToDecode(base64: String?): Bitmap {
        val encodeByte = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    }


    //Bitmap을 Uri로
    private fun getImageUri(context: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }


    //이미지 외부 저장소에 저장
    fun imageExternalSave(context: Context, bitmap: Bitmap, path: String): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {

            val rootPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val dirName = "/" + path
            val fileName = System.currentTimeMillis().toString() + ".png"
            val savePath = File(rootPath + dirName)
            savePath.mkdirs()

            val file = File(savePath, fileName)

            photoUri = Uri.parse(savePath.toString())

            if (file.exists()) file.delete()

            try {
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                //갤러리 갱신
                context.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())
                    )
                )

                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }


    //권한 체크
    fun checkPermission(activity: Activity, permission: String): Boolean {
        val permissionChecker =
            ContextCompat.checkSelfPermission(activity.applicationContext, permission)

        //권한이 없으면 권한 요청
        if (permissionChecker == PackageManager.PERMISSION_GRANTED) return true
        ActivityCompat.requestPermissions(activity, arrayOf(permission), FLAG_PRERM_DOWNLOAD)
        return false
    }
}