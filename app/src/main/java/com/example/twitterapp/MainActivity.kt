package com.example.twitterapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StreamDownloadTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.UploadTask.TaskSnapshot
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private var databse=FirebaseDatabase.getInstance()
    private var myRef=databse.reference
    var listTweet=ArrayList<Ticket>()
    var myEmail:String?=null
    var userUID:String?=null
    var adapter:MyTweetAdapter?=null
    var downLoadURL:String?=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var b:Bundle=intent.extras!!
        myEmail=b.getString ("email")
        userUID=b.getString("uid")
        listTweet.add(Ticket("0","him","url","Add"))
        //listTweet.add(Ticket("1","sdfgrger","url","post"))
        adapter= MyTweetAdapter(listTweet,this)
        //adapter!!.myRef=myRef
        //adapter!!.currentUser=FirebaseAuth.getInstance().currentUser
        var rv=findViewById<RecyclerView>(R.id.rvTweet)
        rv.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        rv.adapter=adapter
        loadPost()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==123 && data!=null && resultCode== RESULT_OK){
            val selectImage=data.data
            val filePathColum= arrayOf(MediaStore.Images.Media.DATA)
            val cursor=contentResolver.query(selectImage!!,filePathColum,null,null,null)
            cursor!!.moveToFirst()
            val coulomIndex=cursor!!.getColumnIndex(filePathColum[0])
            val picturePath=cursor!!.getString(coulomIndex)
            cursor!!.close()
            uploadImage(BitmapFactory.decodeFile(picturePath))
        }
    }
    var downloadURL:String?=""
    private fun uploadImage(bitmap: Bitmap?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://tictactoe-e2ce8.appspot.com")
        val df = SimpleDateFormat("ddMMyyHHmmss")
        val dataobj = Date()
        val imagePath = spliteString(myEmail!!) + "." + df.format(dataobj) + ".jpg"
        val imageRef = storageRef.child("imagePost/" + imagePath)
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener{
                adapter!!.downLoadURL = it.toString()
            }
        }
    }

    private fun spliteString(email: String): String {
        val split=email.split("@")
        return split[0]
    }

    private fun loadPost() {
        myRef.child("posts")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        listTweet.clear()
                        listTweet.add(Ticket("0","","","Add"))
                        var td=snapshot.value as HashMap<String,Any>
                        for(key in td.keys){
                            var post=td[key] as HashMap<String,Any>
                            listTweet.add(
                                Ticket(key,
                            post["text"] as String,
                            post["postImage"] as String,
                            post["userUID"] as String)
                            )
                        }

                        adapter!!.notifyDataSetChanged()
                    }catch (ex:Exception){}
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}