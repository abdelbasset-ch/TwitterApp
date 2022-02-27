package com.example.twitterapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.PicassoProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList

class MyTweetAdapter(var ticketList:ArrayList<Ticket>, var context: Context):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var currentUser=FirebaseAuth.getInstance().currentUser

    var myRef=FirebaseDatabase.getInstance().reference
    val postTicket=1;
    val tweetTicket=2;
    public var downLoadURL:String?=null

    class ViewHolder1(itemView:View):RecyclerView.ViewHolder(itemView) {

        var ivUser:ImageView?=null
        var tvUser:TextView?=null
        var tvDesc:TextView?=null
        var ivPic:ImageView?=null
        var tvDate:TextView?=null
        var ivLike:ImageView?=null
        init {
            ivUser=itemView.findViewById(R.id.ivUser)
            tvUser=itemView.findViewById(R.id.tvUser)
            tvDesc=itemView.findViewById(R.id.tvDesc)
            ivPic=itemView.findViewById(R.id.ivPic)
            tvDate=itemView.findViewById(R.id.tvDate)
            ivLike=itemView.findViewById(R.id.ivLike)
        }
    }
    class ViewHolder2(itemView:View):RecyclerView.ViewHolder(itemView) {
        var etDesc:EditText?=null
        var ivSend:ImageView?=null
        var ivAttach:ImageView?=null

        init {
            etDesc=itemView.findViewById(R.id.etDesc)
            ivSend=itemView.findViewById(R.id.ivSend)
            ivAttach=itemView.findViewById(R.id.ivAttatch)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(this.ticketList[position].tweetPersonUID=="Add"){
            return tweetTicket
        }else if (this.ticketList[position].tweetPersonUID!="Add") {
            return postTicket
        }
        return 0
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view:View
        if(viewType==postTicket){
            view=LayoutInflater.from(parent.context).inflate(R.layout.post_ticket,parent,false)
            return ViewHolder1(view)
        }else{
            view=LayoutInflater.from(parent.context).inflate(R.layout.tweet_ticket,parent,false)
            return ViewHolder2(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ViewHolder1){
            holder.tvDesc!!.text=ticketList[position].tweetText
            myRef.child("Users").child(ticketList[position].tweetPersonUID.toString()).child("email").addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    holder.tvUser!!.text=snapshot.value.toString().split('@')[0]
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            val formatted = current.format(formatter)
            holder.tvDate!!.text=formatted.toString()

            Picasso.get().load(ticketList[position].tweetImageUrl).into(holder.ivPic)

        }else if(holder is ViewHolder2){
            holder.ivSend!!.setOnClickListener(View.OnClickListener {
                Toast.makeText(context, myRef.toString(), Toast.LENGTH_SHORT).show()
                    myRef.child("posts").push().setValue(
                        PostInfo(currentUser!!.uid,
                            holder.etDesc!!.text.toString(),downLoadURL!!)
                    )
                    holder!!.etDesc!!.setText("")


            })
            holder.ivAttach!!.setOnClickListener(View.OnClickListener{
                loadImage()


            })
        }
    }

    override fun getItemCount(): Int {
        return ticketList.size
    }

    val PICk_IMAGE_CODE=123
    fun loadImage(){
        var intent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        (context as Activity).startActivityForResult(intent,PICk_IMAGE_CODE, Bundle())
        (context as Activity).onActivityReenter(PICk_IMAGE_CODE,intent)
    }

}