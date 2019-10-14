package com.quockhanhng.training.messenger.adapter

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.quockhanhng.training.messenger.R
import com.quockhanhng.training.messenger.model.Message
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(query: Query, private var userId: String, private var context: Context) :
    FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder>(
        FirestoreRecyclerOptions.Builder<Message>().setQuery(
            query,
            Message::class.java
        ).build()
    ) {

    companion object {
        private const val MESSAGE_IN_VIEW_TYPE = 1
        private const val MESSAGE_OUT_VIEW_TYPE = 2
    }

    private val requestOptions = RequestOptions().placeholder(R.drawable.ic_account_circle_black_24dp)
    private var storageReference: StorageReference = FirebaseStorage.getInstance().reference
        .child("profile_images")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val view = if (viewType == MESSAGE_IN_VIEW_TYPE) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.messenger_in, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.messenger_out, parent, false)
        }
        return MessageHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).messageUserId == userId) {
            MESSAGE_OUT_VIEW_TYPE
        } else MESSAGE_IN_VIEW_TYPE
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int, model: Message) {
        val mText = holder.mText
        val mUsername = holder.mUsername
        val mTime = holder.mTime
        val mLikesCount = holder.mLikesCount
        val imgProfile = holder.imgProfile
        val imgDropdown = holder.imgDropdown
        val imgLikes = holder.imgLikes

        mUsername.text = model.messageUser
        mText.text = model.messageText
        mTime.text = DateFormat.format("dd MMM  (h:mm a)", model.messageTime)
        mLikesCount.text = model.messageLikesCount.toString()
        if (model.messageLikes != null) {
            if ((model.messageLikes!!).contains(userId)) {
                imgLikes.setImageResource(R.drawable.ic_favorite_red_24dp)
            } else {
                imgLikes.setImageResource(R.drawable.ic_favorite_black_24dp)
            }
        }

        imgLikes.setOnClickListener {
            val callBack = context as MessageClickListener

            // Check if user has liked this message
            val status = callBack.onClickLike(model.messageId)
            if (status == 1) {
                imgLikes.setImageResource(R.drawable.ic_favorite_black_24dp)
            } else if (status == 0) {
                imgLikes.setImageResource(R.drawable.ic_favorite_red_24dp)
            }
        }

        imgDropdown.setOnClickListener {

            val popUpMenu = PopupMenu(context, imgDropdown)
            popUpMenu.menuInflater.inflate(R.menu.message_popup_menu, popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener {
                if (it.itemId == R.id.mi_delete_message) {
                    val callBack = context as MessageClickListener
                    callBack.onClickDropdownButton(model.messageId)
                }
                true
            }
            popUpMenu.show()
        }

        Glide.with(context)
            .setDefaultRequestOptions(requestOptions)
            .load(storageReference.child(model.messageUserId))
            .into(imgProfile)
    }

    class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mText: TextView = itemView.findViewById(R.id.message_text)
        var mUsername: TextView = itemView.findViewById(R.id.message_user)
        var mTime: TextView = itemView.findViewById(R.id.message_time)
        var mLikesCount: TextView = itemView.findViewById(R.id.message_Likes)
        var imgProfile: CircleImageView = itemView.findViewById(R.id.imgDps)
        var imgDropdown: ImageView = itemView.findViewById(R.id.imgDropdown)
        var imgLikes: ImageView = itemView.findViewById(R.id.imgLikes)
    }

    interface MessageClickListener {
        fun onClickLike(id: String): Int
        fun onClickDropdownButton(id: String)
    }
}