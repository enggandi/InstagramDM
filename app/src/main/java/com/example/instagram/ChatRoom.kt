package com.example.instagram

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatRoom : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_room)

        var loginUser=intent.getStringExtra("loginUser")
        var chatPartner=intent.getStringExtra("name")
        var personel=intent.getStringArrayListExtra("personel")!!

        var namaDM=findViewById<TextView>(R.id.namaDM)
        val fs = FirebaseFirestore.getInstance()
        namaDM.setText(chatPartner)

        val rvChat = findViewById<RecyclerView>(R.id.rvListChat)
        rvChat.layoutManager = LinearLayoutManager(this)
        var chatList = ArrayList<Chat>()
        var adapter = ChatAdapter(chatList,loginUser.toString(), personel)
        rvChat.adapter = adapter

        fs.collection("test").whereEqualTo("personel", personel).get().addOnSuccessListener {
            document ->
            if(document.size()>0){
                for (i in 0..document.size()-1){
                    var chat = document.documents[i].data?.getValue("chat") as ArrayList<String>?
                    if (chat != null) {
                        for (j in 0..chat.size-1){
                            var temp = chat.get(j) as Map<String,String>
                            chatList.add(Chat(temp.getValue("message"),temp.getValue("sender"),temp.getValue("reciever")))
                            rvChat.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
        fs.collection("test").whereEqualTo("personel",personel).addSnapshotListener(){
                value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) {
                Log.w("failed", "Listen failed.", error);
            }
            for (dc: DocumentChange in value!!.getDocumentChanges()){
                when(dc.type){
                    DocumentChange.Type.ADDED -> {
                        println("Added.................")
                    }
                    DocumentChange.Type.MODIFIED ->{
                        if(value.size()>0){
                            chatList.clear()
                            for (i in 0..value.size()-1){
                                var chat = value.documents[i].data?.getValue("chat") as ArrayList<String>?
                                if (chat != null) {
                                    for (j in 0..chat.size-1){
                                        var temp = chat.get(j) as Map<String,String>
                                        chatList.add(Chat(temp.getValue("message"),temp.getValue("sender"),temp.getValue("reciever")))
                                        rvChat.adapter?.notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        println("Removed.................")
                    }
                }
            }
        }

        var back=findViewById<Button>(R.id.back_toMain)
        back.setOnClickListener(){
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra("loginUser",loginUser)
            startActivity(intent)
        }

        var info=findViewById<MaterialButton>(R.id.infoBtn)
        var deleteChat=findViewById<LinearLayout>(R.id.deleteChat)
        var yesDelete=findViewById<Button>(R.id.Yes)
        var noDelete=findViewById<Button>(R.id.No)
        info.setOnClickListener(){
            deleteChat.setVisibility(View.VISIBLE)
        }
        yesDelete.setOnClickListener(){
            fs.collection("test").document(personel.get(0)+personel.get(1)).delete()
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra("loginUser",loginUser)
            startActivity(intent)
        }
        noDelete.setOnClickListener(){
            deleteChat.setVisibility(View.GONE)
        }

        var chatInput = findViewById<EditText>(R.id.chatInput)
        var kirim = findViewById<Button>(R.id.kirim)
        chatInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                kirim.setVisibility(View.VISIBLE)
            }
        })
        kirim.setOnClickListener(){
            var newChat = Chat(chatInput.text.toString(),loginUser.toString(),chatPartner.toString())
            val df = SimpleDateFormat("yyyy/M/dd hh:mm:ss")
            val currentDate = df.format(Date())
            fs.collection("test").document(personel.get(0)+personel.get(1)).update("chat", FieldValue.arrayUnion(newChat))
            fs.collection("test").document(personel.get(0)+personel.get(1)).update("lastChat", currentDate.toString())
            chatInput.setText("")
            kirim.setVisibility(View.GONE)
        }
    }
}

data class Chat(val message: String, val sender: String, val reciever: String)
class ChatAdapter(val listChat: ArrayList<Chat>,val loginUser:String, val personel: ArrayList<String>): RecyclerView.Adapter<ChatHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        return when(viewType){
            1 -> {
                var v = LayoutInflater.from(parent.context).inflate(R.layout.chat_text, parent, false)
                ChatHolder(v,personel)
            }
            2 -> {
                var v = LayoutInflater.from(parent.context).inflate(R.layout.friend_chat_text, parent, false)
                ChatHolder(v,personel)
            }
            else -> {
                var v = LayoutInflater.from(parent.context).inflate(R.layout.chat_text, parent, false)
                ChatHolder(v,personel)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(listChat.get(position).sender.equals(loginUser)){
            return 1
        }
        return 2
    }
    override fun getItemCount(): Int {
        return listChat.size
    }
    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        holder.bindView(listChat[position])
    }

}
class ChatHolder(val v: View, val personel: ArrayList<String>): RecyclerView.ViewHolder(v){
    var chat: Chat? = null
    fun bindView(Chat: Chat){
        this.chat = Chat
        v.findViewById<TextView>(R.id.isiChat).text = Chat.message
        if (v.findViewById<LinearLayout>(R.id.unsend)!=null) {
            v.findViewById<LinearLayout>(R.id.unsend).setOnClickListener() {
                var fs = FirebaseFirestore.getInstance()
                fs.collection("test").document(personel.get(0)+personel.get(1)).update("chat", FieldValue.arrayRemove(Chat))
            }
            v.setOnClickListener {
//            Toast.makeText(v.context, Chat?.isiText, Toast.LENGTH_SHORT)
                println("Clicked")
                if (v.findViewById<LinearLayout>(R.id.unsend).isVisible){
                    v.findViewById<LinearLayout>(R.id.unsend).setVisibility(View.GONE)
                }
                else{
                    v.findViewById<LinearLayout>(R.id.unsend).setVisibility(View.VISIBLE)
                }
            }
        }
    }
}