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
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.techtown.oneshotphoto.*
import org.techtown.oneshotphoto.api.IRetrofit
import org.techtown.oneshotphoto.databinding.ActivityMyFilterBinding
import org.techtown.oneshotphoto.util.RealPathUtil
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

class MyFilterActivity : AppCompatActivity() {

    lateinit var binding: ActivityMyFilterBinding
    private lateinit var auth: FirebaseAuth

    val STORAGE_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val FLAG_PERM_GALLERY = 91
    val FLAG_REQ_GALLERY2 = 102

    var imgUri2: String = "none"
    lateinit var originalUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MODE = MYFILTER

        auth = Firebase.auth
        val userId = auth!!.uid.toString()
        var selectedFilter:String = "non"

        val dialogCropView = layoutInflater.inflate(R.layout.crop_dialog,null)
        val dialogBtnCrop = dialogCropView.findViewById<Button>(R.id.btnCropOk)
        val dialogIv = dialogCropView.findViewById<CropImageView>(R.id.ivCrop)
        val CropBuilder = AlertDialog.Builder(this).setView(dialogCropView).create()

        var filterNameArray = arrayListOf<String>("필터 선택")

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

        service.getFilterList(userId).enqueue(object : retrofit2.Callback<filterResult>{
            override fun onResponse(call: Call<filterResult>, response: Response<filterResult>) {
                var result = response.body()!!.filterLists
                Log.d(TAG, "onResponse: ${response.body()!!.filterLists}")
                filterNameArray.addAll(result)

                var adapter2 = ArrayAdapter(baseContext, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, filterNameArray)

                adapter2.setDropDownViewResource(R.layout.spinner_item)
                binding.spnMyfilter.adapter = adapter2
            }

            override fun onFailure(call: Call<filterResult>, t: Throwable) {
                Log.d(TAG, "onFailure: $call ")
            }
        })

        //스피너에서 값 읽어오기 구현
        binding.spnMyfilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                
                if(binding.spnMyfilter.getItemAtPosition(p2) == "필터 선택"){
                    Toast.makeText(baseContext, "필터 목록에서 필터를 선택해주세요.", Toast.LENGTH_SHORT).show()
                }else{
                    selectedFilter = binding.spnMyfilter.getItemAtPosition(p2).toString()
                    Log.d(TAG, "onItemSelected: $selectedFilter")
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.d(TAG, "onNothingSelected: ")
            }
        }

        //필터 불러오기 버튼 구현
        binding.btnMyFilterTraining.setOnClickListener { 
            if(selectedFilter == "non"){
                Toast.makeText(baseContext, "필터가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }else{
                service.useMyFilter(userId, selectedFilter).enqueue(object : retrofit2.Callback<InferenceResult>{
                    override fun onResponse(call: Call<InferenceResult>, response: Response<InferenceResult>) {
                        var result: InferenceResult? = response.body()
                        if(result!!.responseCode == "success"){
                            var b64:String = result!!.base64String.toString()
                            var bitmapDecode = stringToDecode(b64)
                            binding.imageMyFilterView.setImageBitmap(bitmapDecode)
                            Toast.makeText(baseContext, "필터를 불러왔습니다. 필터를 이미지에 적용해보세요!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<InferenceResult>, t: Throwable) {
                        Log.d(TAG, "onFailure: 필터 적용 실패")
                    }
                })
            }
        }

        // 원본이미지 갤러리 버튼 구현
        binding.btnMyFilterGallery2.setOnClickListener {
            if(isPermitted(STORAGE_PERMISSION)){
                openGallery(FLAG_REQ_GALLERY2)
            }else{
                ActivityCompat.requestPermissions(this, STORAGE_PERMISSION, FLAG_PERM_GALLERY)
            }
        }

        binding.btnMyFilterInference.setOnClickListener {

            var uid = RequestBody.create(MediaType.parse("multipart/form-data"),userId)
            var uidBody = MultipartBody.Part.createFormData("uid", userId, uid)

            var crop = RequestBody.create(MediaType.parse("multipart/form-data"), CROP_ORIGINAL)
            var cropBody = MultipartBody.Part.createFormData("crop", CROP_ORIGINAL, crop)

            val file = File(imgUri2)
            val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
            val originalImageBody = MultipartBody.Part.createFormData("originalImage", file.name, requestFile)

            Toast.makeText(this, "필터 적용이 시작되었습니다.약 15초의 시간이 소요됩니다.", Toast.LENGTH_SHORT).show()

            service.inference(uidBody, cropBody, originalImageBody).enqueue(object : retrofit2.Callback<InferenceResult> {
                override fun onResponse(call: Call<InferenceResult>, response: Response<InferenceResult>) {
                    if(response.isSuccessful){
                        var result: InferenceResult? = response.body()

                        if(result!!.responseCode == "success"){
                            b64 = result!!.base64String.toString()
                            Log.d(TAG, "onResponse: responseCode is success ${b64}")
                            val resultIntent = Intent(applicationContext, ResultCreateActivity::class.java)
                            startActivityForResult(resultIntent, FLAG_CALL)
                        } else if(result!!.responseCode == "error"){
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

        binding.btnMyFilterCrop.setOnClickListener {
            if(imgUri2 == "none"){
                Toast.makeText(this, "갤러리에서 사진을 선택해야 합니다.", Toast.LENGTH_SHORT).show()
            }else{
                CropBuilder.show()
                dialogIv.setImageUriAsync(originalUri!!)

                dialogBtnCrop.setOnClickListener {
                    val cropped: Bitmap? = dialogIv.croppedImage

                    imgUri2 = RealPathUtil.getRealPath(applicationContext, getImageUriFromBitmap(baseContext, cropped!!))!!

                    binding.imageMyFilterView2.setImageURI(getImageUriFromBitmap(baseContext, cropped!!))

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

                FLAG_REQ_GALLERY2 ->{
                    data?.data?.let { uri2 ->
                        imgUri2 = RealPathUtil.getRealPath(applicationContext, uri2)!!
                        originalUri = uri2

                        CROP_ORIGINAL = NCROP_ORIGINAL
                        binding.imageMyFilterView2.setImageURI(uri2)
                    }
                }

                FLAG_CALL ->{
                    binding.imageMyFilterView2.setImageResource(R.drawable.ic_baseline_add_photo_alternate_24)
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
        return Uri.parse(path.toString())
    }

    //base64를 Bitmap으로 디코딩
    private fun stringToDecode(base64: String?): Bitmap {
        val encodeByte = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
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