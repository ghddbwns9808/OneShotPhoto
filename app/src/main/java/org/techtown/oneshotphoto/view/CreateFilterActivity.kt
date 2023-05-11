package org.techtown.oneshotphoto.view

import org.techtown.oneshotphoto.util.RealPathUtil
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.techtown.oneshotphoto.*
import org.techtown.oneshotphoto.api.IRetrofit
import org.techtown.oneshotphoto.databinding.ActivityCreateFilterBinding
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit


class CreateFilterActivity : AppCompatActivity() {

    val STORAGE_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val FLAG_PERM_GALLERY = 91

    val FLAG_REQ_GALLERY = 101
    val FLAG_REQ_GALLERY2 = 102

    lateinit var binding: ActivityCreateFilterBinding

    lateinit var originalUri: Uri
    lateinit var filterUri: Uri

    var imgUri: String = "none"
    var imgUri2: String = "none"

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MODE = CREATE

        auth = Firebase.auth
        val userId = auth!!.uid.toString()

        Log.d(TAG, "onCreate ${userId}")

        val dialogView = layoutInflater.inflate(R.layout.done_dialog,null)
        val dialogBtn = dialogView.findViewById<MaterialButton>(R.id.btnDoneDlgSave)
        val builder = AlertDialog.Builder(dialogView.context).setView(dialogView).create()

        val dialogCropView = layoutInflater.inflate(R.layout.crop_dialog,null)
        val dialogBtnCrop = dialogCropView.findViewById<Button>(R.id.btnCropOk)
        val dialogIv = dialogCropView.findViewById<CropImageView>(R.id.ivCrop)
        val CropBuilder = AlertDialog.Builder(this).setView(dialogCropView).create()
        

        //Timeout 설정을 위한 OkHttpClient 설정
        val okHttpClient: OkHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        //Retrofit 객체 설정
        val retrofit = Retrofit.Builder().baseUrl(url).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val service = retrofit.create(IRetrofit::class.java)

        //필터 이미지 로드를 위한 갤러리 버튼
        binding.btnGallery.setOnClickListener {
            if(isPermitted(STORAGE_PERMISSION)){
                openGallery(FLAG_REQ_GALLERY)
            }else{
                ActivityCompat.requestPermissions(this, STORAGE_PERMISSION, FLAG_PERM_GALLERY)
            }
        }

        //원본이미지 로드를 위한 갤러리 버튼
        binding.btnGallery2.setOnClickListener {
            if(isPermitted(STORAGE_PERMISSION)){
                openGallery(FLAG_REQ_GALLERY2)
            }else{
                ActivityCompat.requestPermissions(this, STORAGE_PERMISSION, FLAG_PERM_GALLERY)
            }
        }

        //필터 생성 버튼
        binding.btnTraining.setOnClickListener {

            var uid = RequestBody.create(MediaType.parse("multipart/form-data"),userId)
            var uidBody = MultipartBody.Part.createFormData("uid", userId, uid)

            var crop = RequestBody.create(MediaType.parse("multipart/form-data"), CROP_FILTER)
            var cropBody = MultipartBody.Part.createFormData("crop", CROP_FILTER, crop)

            val file = File(imgUri)
            val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
            val filterImageBody = MultipartBody.Part.createFormData("filterImage", file.name, requestFile)

            Toast.makeText(this, "필터 생성이 시작되었습니다.약 50초의 시간이 소요됩니다.", Toast.LENGTH_SHORT).show()

            service.train(uidBody, cropBody, filterImageBody).enqueue(object : retrofit2.Callback<TrainingResult> {

                override fun onResponse(call: Call<TrainingResult>, response: Response<TrainingResult>) {
                    if (response.isSuccessful) {
                        var result: TrainingResult? = response.body()

                        if(result!!.responseCode == "success"){
                            //Toast.makeText(baseContext, "필터 생성을 완료했습니다. 필터를 적용해보세요!", Toast.LENGTH_SHORT).show()

                            builder.show()
                            dialogBtn.setOnClickListener {
                                builder.dismiss()
                            }
                            //Log.d(TAG, "onResponse: responseCode is success: $result")
                        } else if(result!!.responseCode == "error"){
                            //Log.d(TAG, "onResponse: responseCode is fail! ${result}")
                            Toast.makeText(baseContext, "얼굴 인식에 실패했습니다. \n다른 사진을 사용해보세요.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(baseContext ,"필터 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "onResponse is not success: ${response.body()}")
                    }
                }

                override fun onFailure(call: Call<TrainingResult>, t: Throwable) {
                    Toast.makeText(applicationContext, "onFailure", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onFailure")
                }
            })
        }

        //필터 적용 버튼
        binding.btnInference.setOnClickListener {

            var uid = RequestBody.create(MediaType.parse("multipart/form-data"),userId)
            var uidBody = MultipartBody.Part.createFormData("uid", userId, uid)

            var crop = RequestBody.create(MediaType.parse("multipart/form-data"), CROP_ORIGINAL)
            var cropBody = MultipartBody.Part.createFormData("crop", CROP_ORIGINAL, crop)


            val file = File(imgUri2)
            val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
            val originalImageBody = MultipartBody.Part.createFormData("originalImage", file.name, requestFile)

            Toast.makeText(this, "필터 적용이 시작되었습니다.약 15초의 시간이 소요됩니다.", Toast.LENGTH_SHORT).show()

            service.inference(uidBody, cropBody,  originalImageBody).enqueue(object : retrofit2.Callback<InferenceResult> {
                override fun onResponse(call: Call<InferenceResult>, response: Response<InferenceResult>) {
                    if(response.isSuccessful){
                        var result: InferenceResult? = response.body()

                        if(result!!.responseCode == "success"){
                            b64 = result!!.base64String.toString()
                            Log.d(TAG, "onResponse: responseCode is success $b64")
                            val resultIntent = Intent(applicationContext, ResultCreateActivity::class.java)
                            startActivityForResult(resultIntent, FLAG_CALL)
                        } else if(result!!.responseCode == "error"){
                           //Log.d(TAG, "onResponse: responseCode is fail! ${result}")
                            Toast.makeText(baseContext, "얼굴 인식에 실패했습니다. \n다른 사진을 사용해보세요.", Toast.LENGTH_LONG).show()
                        }

                    } else {
                        Log.d(TAG, "onResponse is not success: {response.body()}")
                    }
                }

                override fun onFailure(call: Call<InferenceResult>, t: Throwable) {
                    Toast.makeText(applicationContext, "onFailure", Toast.LENGTH_SHORT).show()
                }

            })
        }

        //필터 이미지 크롭 버튼
        binding.btnCrop.setOnClickListener {

            if(imgUri == "none"){
                Toast.makeText(this, "갤러리에서 사진을 선택해야 합니다.", Toast.LENGTH_SHORT).show()
            }else{
                CropBuilder.show()
                dialogIv.setImageUriAsync(filterUri!!)

                dialogBtnCrop.setOnClickListener {
                    val cropped: Bitmap? = dialogIv.croppedImage
                    imgUri = RealPathUtil.getRealPath(applicationContext, getImageUriFromBitmap(baseContext, cropped!!))!!

                    binding.imageView.setImageURI(getImageUriFromBitmap(baseContext, cropped!!))

                    CropBuilder.dismiss()
                    CROP_FILTER = YCROP_FILTER
                }
            }
        }

        //원본 이미지 크롭 버튼
        binding.btnCrop2.setOnClickListener {

            if(imgUri2 == "none"){
                Toast.makeText(this, "갤러리에서 사진을 선택해야 합니다.", Toast.LENGTH_SHORT).show()
            }else{
                CropBuilder.show()
                dialogIv.setImageUriAsync(originalUri)

                dialogBtnCrop.setOnClickListener {
                    val cropped: Bitmap? = dialogIv.croppedImage

                    imgUri2 = RealPathUtil.getRealPath(applicationContext, getImageUriFromBitmap(baseContext, cropped!!))!!

                    binding.imageView2.setImageURI(getImageUriFromBitmap(baseContext, cropped!!))

                    CropBuilder.dismiss()
                    CROP_ORIGINAL = YCROP_ORIGINAL
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                FLAG_REQ_GALLERY ->{
                    data?.data?.let { uri ->
                        imgUri = RealPathUtil.getRealPath(applicationContext, uri)!!
                        filterUri = uri
                        CROP_FILTER = NCROP_FILTER

                        binding.imageView.setImageURI(uri)

                    }
                }

                FLAG_REQ_GALLERY2 ->{
                    data?.data?.let { uri2 ->
                        imgUri2 = RealPathUtil.getRealPath(applicationContext, uri2)!!
                        originalUri = uri2
                        CROP_ORIGINAL = NCROP_ORIGINAL

                        binding.imageView2.setImageURI(uri2)
                    }
                }

                FLAG_CALL ->{
                    binding.imageView2.setImageResource(R.drawable.ic_baseline_add_photo_alternate_24)
                    Toast.makeText(this,"새 작업을 시작합니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        //val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        val path: String = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "IMG_${System.currentTimeMillis()}", null)
        return Uri.parse(path!!.toString())
    }


    private fun bitmapTOString(bitmap: Bitmap?): String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)

    }


    fun openGallery(flag: Int){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, flag)
    }

    fun isPermitted(permissions: Array<String>) : Boolean{

        for(permission in permissions){
            val result = ContextCompat.checkSelfPermission(this, permission)
            if(result != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }


}
