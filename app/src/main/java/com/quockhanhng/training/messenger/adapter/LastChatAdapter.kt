package com.quockhanhng.training.messenger.adapter

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quockhanhng.training.messenger.R
import com.quockhanhng.training.messenger.model.LastMessageResponse
import kotlinx.android.synthetic.main.last_chat_row.view.*

class LastChatAdapter(
    private var lastMessageResponse: ArrayList<LastMessageResponse>,
    private val itemClickListener: (LastMessageResponse) -> Unit
) :
    RecyclerView.Adapter<LastChatAdapter.LastChatHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LastChatHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.last_chat_row, parent, false)

        return LastChatHolder(view)
    }

    override fun onBindViewHolder(holder: LastChatHolder, position: Int) {
        holder.bindView(lastMessageResponse[position], itemClickListener)
    }

    override fun getItemCount() = lastMessageResponse.size

    class LastChatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindView(
            lastMessage: LastMessageResponse,
            itemClickListener: (LastMessageResponse) -> Unit
        ) {

            itemView.tv_lastChat_Name.text = lastMessage.user.surname + " " + lastMessage.user.name
            itemView.tv_lastChat_MessageDate.text = DateFormat.format("dd MMM  (h:mm a)", lastMessage.lastMessageTime)
            itemView.setOnClickListener { itemClickListener(lastMessage) }

        }
    }
}