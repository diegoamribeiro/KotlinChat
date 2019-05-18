package com.android.dmribeiro87.kotlinmesseger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.dmribeiro87.kotlinmesseger.R
import com.android.dmribeiro87.kotlinmesseger.model.ChatMessage
import com.android.dmribeiro87.kotlinmesseger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val TAG = "ChatLogActivity"

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        recyclerview_chatlog.adapter = adapter
        //setupDummyData()
        listenForMessages()

        //val username = intent.getStringExtra(NewMessageActivity.USER_KEY)

        send_button_chatlog.setOnClickListener{
            Log.d(TAG, "Testing button")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)


                if (chatMessage!= null){
                   Log.d(TAG, chatMessage.text)

                   if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                       val currentUser = LatestMessageActivity.currentUser ?: return
                       adapter.add(ChatFromItem(chatMessage.text, currentUser))
                   }else{
                       adapter.add(ChatToItem(chatMessage.text, toUser!!))


                   }

               }
                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }


    private fun performSendMessage(){
        val text = edittext_chatlog.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromId == null) return

        //val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMesage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)


        reference.setValue(chatMesage)

            .addOnSuccessListener {
                Log.d(TAG, "Saved message : ${reference.key}")
                edittext_chatlog.text.clear()
                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }
        toReference.setValue(chatMesage)
        //SETA O VALOR DE chatMessage NO FIREBASE
        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMesage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMesage)
    }
}


class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_from_row
        Picasso.get().load(uri).into(targetImageView)


    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row

    }
}
class ChatToItem (val text: String, val user: User) : Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text = text
        //Load our image into chatlog
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_to_row
        Picasso.get().load(uri).into(targetImageView)


    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row

    }
}
