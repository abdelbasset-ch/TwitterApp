package com.example.twitterapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {
    var ivProfile:ImageView?=null
    var etEmail:EditText?=null
    var etPass:EditText?=null
    var myAuth:FirebaseAuth= FirebaseAuth.getInstance()
    var myRef=FirebaseDatabase.getInstance().reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        etEmail=findViewById(R.id.etEmail)
        etPass=findViewById(R.id.etPassword)
        ivProfile=findViewById(R.id.ivProfile)
        ivProfile!!.setOnClickListener {
            checkPermission()
        }
    }
    val extStroragePermission=111
    fun checkPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),extStroragePermission)
            }
        }else{
            loadProfilePic()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            extStroragePermission->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    loadProfilePic()
                }else{
                    Toast.makeText(this, "storage access deneid", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    val picImageCode=123
    private fun loadProfilePic() {
        var intent=Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,picImageCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==picImageCode && data!=null && resultCode== RESULT_OK){
            val selectImage=data.data
            val filePathColumn= arrayOf(MediaStore.Images.Media.DATA)
            val cursor=contentResolver.query(selectImage!!,filePathColumn,null,null,null)
            cursor!!.moveToFirst()
            val columnIndex=cursor!!.getColumnIndex((filePathColumn[0]))
            val picturePath=cursor!!.getString(columnIndex)
            cursor!!.close()
            ivProfile!!.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }

    fun logIn(view: View) {
        loginToFirebase(etEmail!!.text.toString(),etPass!!.text.toString())
    }

    private fun loginToFirebase(email: String, password: String) {
        myAuth!!.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){task->
                if(task.isSuccessful){
                    Toast.makeText(applicationContext, "successful login", Toast.LENGTH_SHORT).show()
                    saveImageInFirebase()
                }else{
                    Toast.makeText(applicationContext, "fail login", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveImageInFirebase() {
        var currentUser=myAuth!!.currentUser
        var email:String=currentUser!!.email.toString()
        val storage=FirebaseStorage.getInstance()
        val storageRef=storage.getReferenceFromUrl("gs://tictactoe-e2ce8.appspot.com/")
        val df=SimpleDateFormat("ddMMyyHHmmss")
        val dataobj=Date()
        val imagePath=splitString(email)+"."+df.format(dataobj)+".jpg"
        val imageRef=storageRef.child("images/"+imagePath)
        ivProfile!!.isDrawingCacheEnabled=true
        ivProfile!!.buildDrawingCache()
        val drawable=ivProfile!!.drawable as BitmapDrawable
        val bitmap=drawable.bitmap
        val baos=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data=baos.toByteArray()
        val uploadTask=imageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext, "fail to upload", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot->
            var downloadUrl=taskSnapshot.storage.downloadUrl.toString()!!
            myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("Users").child(currentUser.uid).child("profileImage").setValue(downloadUrl)
            loadTweets()
        }
    }

    private fun loadTweets() {
        var currentUser=myAuth.currentUser
        if(currentUser!=null){
            var intent=Intent(this,MainActivity::class.java)
            intent.putExtra("email",currentUser.email)
            intent.putExtra("uid",currentUser.uid)
            //intent.putExtra("baseurl",myRef.root.toString())
            startActivity(intent)
        }
    }

    fun splitString(email: String):String{
        val split=email.split("@")
        return split[0]
    }
}