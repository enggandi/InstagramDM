package com.example.instagram
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var loginUser=intent.getStringExtra("loginUser")
        var fs = FirebaseFirestore.getInstance()

        val rvPerson = findViewById<RecyclerView>(R.id.rvListPerson)
        rvPerson.layoutManager = LinearLayoutManager(this)
        var personList = ArrayList<Person>()
        var adapter : PersonAdapter

        var loginBlock=findViewById<RelativeLayout>(R.id.loginBlock)
        var login=findViewById<Button>(R.id.login)
        var loginName=findViewById<EditText>(R.id.loginName)

        if (loginUser.isNullOrEmpty()){
            findViewById<TextView>(R.id.blurView).setVisibility(View.VISIBLE)
            loginBlock.setVisibility(View.VISIBLE)
        }
        else{
            findViewById<TextView>(R.id.blurView).setVisibility(View.GONE)
            loginBlock.setVisibility(View.GONE)
            adapter = PersonAdapter(personList, loginUser.toString())
            rvPerson.adapter = adapter
            fs.collection("test").whereArrayContains("personel",loginUser.toString()).orderBy("lastChat", com.google.firebase.firestore.Query.Direction.DESCENDING).get().addOnSuccessListener {
                document ->
                personList.clear()
                for (i in 0..document.size()-1){
                    var dummy : ArrayList<Map<String,String>>? = null
                    var person = Person(document.documents[i].data?.getValue("lastChat").toString(), Integer.parseInt(document.documents[i].data?.getValue("foto").toString()), document.documents[i].data?.getValue("personel") as ArrayList<String>, dummy)
                    personList.add(person)
                }
                rvPerson.adapter?.notifyDataSetChanged()
            }
        }
        login.setOnClickListener(){
            loginUser=loginName.text.toString()
            adapter = PersonAdapter(personList, loginUser.toString())
            rvPerson.adapter = adapter
            fs.collection("test").whereArrayContains("personel",loginUser.toString()).orderBy("lastChat", com.google.firebase.firestore.Query.Direction.DESCENDING).get().addOnSuccessListener {
                document ->
                personList.clear()
                var dummy : ArrayList<Map<String,String>>? = null
                for (i in 0..document.size()-1){
                    var person = Person(document.documents[i].data?.getValue("lastChat").toString(), Integer.parseInt(document.documents[i].data?.getValue("foto").toString()), document.documents[i].data?.getValue("personel") as ArrayList<String>, dummy)
                    personList.add(person)
                }
                rvPerson.adapter?.notifyDataSetChanged()
            }
            findViewById<TextView>(R.id.blurView).setVisibility(View.GONE)
            loginBlock.setVisibility(View.GONE)
        }

        var back = findViewById<Button>(R.id.back)

        var add = findViewById<Button>(R.id.video)
        var addBlock = findViewById<RelativeLayout>(R.id.addBlock)
        var close = findViewById<Button>(R.id.close)
        var next = findViewById<Button>(R.id.next)

        add.setOnClickListener(){
            addBlock.setVisibility(View.VISIBLE)
        }
        close.setOnClickListener(){
            addBlock.setVisibility(View.GONE)
        }
        next.setOnClickListener(){
            var add = findViewById<TextInputEditText>(R.id.addPerson)
            var dummy : ArrayList<Map<String,String>>? = null
            val df = SimpleDateFormat("yyyy/M/dd hh:mm:ss")
            val currentDate = df.format(Date())
            var person = Person(currentDate.toString(),R.mipmap.ic_launcher, arrayListOf(loginUser.toString(),add.text.toString()), dummy)
            fs.collection("test").document(loginUser.toString()+add.text.toString()).set(person)
            add.setText("")
            adapter = PersonAdapter(personList, loginUser.toString())
            rvPerson.adapter = adapter
            fs.collection("test").whereArrayContains("personel",loginUser.toString()).orderBy("lastChat", com.google.firebase.firestore.Query.Direction.DESCENDING).get().addOnSuccessListener {
                document ->
                personList.clear()
                for (i in 0..document.size()-1){
                    var dummy : ArrayList<Map<String,String>>? = null
                    var person = Person(document.documents[i].data?.getValue("lastChat").toString(), Integer.parseInt(document.documents[i].data?.getValue("foto").toString()), document.documents[i].data?.getValue("personel") as ArrayList<String>, dummy)
                    personList.add(person)
                }
                rvPerson.adapter?.notifyDataSetChanged()
            }
            addBlock.setVisibility(View.GONE)
        }
        back.setOnClickListener(){
            loginBlock.setVisibility(View.VISIBLE)
            findViewById<TextView>(R.id.blurView).setVisibility(View.VISIBLE)
        }
    }
}

data class Person(val lastChat: String, val foto: Int, val personel: ArrayList<String>, val chat: ArrayList<Map<String,String>>?)
class PersonAdapter(val listPerson: ArrayList<Person>, val loginUser: String): RecyclerView.Adapter<PersonHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.chat_block, parent, false)
        return PersonHolder(v, loginUser)
    }
    override fun getItemCount(): Int {
        return listPerson.size
    }
    override fun onBindViewHolder(holder: PersonHolder, position: Int) {
        holder.bindView(listPerson[position])
    }
}
class PersonHolder(val v: View, val loginUser: String): RecyclerView.ViewHolder(v){
    var person: Person? = null
    fun bindView(Person: Person){
        this.person = Person
        var chatPartner:String
        if(!Person.personel.get(0).equals(loginUser)){
            chatPartner=Person.personel.get(0)
        }
        else{
            chatPartner=Person.personel.get(1)
        }

        v.findViewById<TextView>(R.id.name).text = chatPartner
        v.findViewById<TextView>(R.id.lastChat).text = Person.lastChat.toString()
        v.findViewById<ImageView>(R.id.foto).setImageResource(Person.foto)
        v.setOnClickListener {
//                Toast.makeText(v.context, "${Person?.lastChat} - ${Person?.name}", Toast.LENGTH_SHORT)
            var intent = Intent(v.context,ChatRoom::class.java)
            intent.putExtra("name", chatPartner)
            intent.putExtra("loginUser", loginUser)
            intent.putExtra("personel", Person.personel)
            v.context.startActivity(intent)
        }
    }
}

