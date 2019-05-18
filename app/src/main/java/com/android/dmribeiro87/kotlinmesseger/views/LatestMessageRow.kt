package com.android.dmribeiro87.kotlinmesseger.views

import androidx.appcompat.app.AppCompatActivity
import com.android.dmribeiro87.kotlinmesseger.R
import com.android.dmribeiro87.kotlinmesseger.model.ChatMessage
import com.android.dmribeiro87.kotlinmesseger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_messages_row.view.*


class LatestMessageActivity : AppCompatActivity() {


    companion object{
        var currentUser: User? = null
    }


    //ADAPTER
    val adapter =  GroupAdapter<ViewHolder>()

    val latestMessagesMap = HashMap<String, ChatMessage>()

    //REFERNCIA O LAYOUT DE LATEST MESSAGES
    class  LatestMessageRow(val chatMessage: ChatMessage) : Item<ViewHolder>() {
        var chatPartnerUser: User? = null
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.message_textview.text = chatMessage.text

            val chatPartnerId: String
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                chatPartnerId = chatMessage.toId
            } else {
                chatPartnerId = chatMessage.toId
            }

            val reference = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            reference.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser = p0.getValue(User::class.java)
                    chatPartnerUser?.username
                    viewHolder.itemView.username_textview_latest_message.text = chatPartnerUser?.username
                    val targetImageView = viewHolder.itemView.imageview_latest_message
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
                }


                override fun onCancelled(p0: DatabaseError) {

                }
            })

            viewHolder.itemView.username_textview_latest_message.text = "Whatever"
        }

        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }
    }
}