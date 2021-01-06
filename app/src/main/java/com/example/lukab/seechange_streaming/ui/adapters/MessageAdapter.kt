package com.example.lukab.seechange_streaming.ui.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lukab.seechange_streaming.service.model.Message
import android.widget.TextView
import com.example.lukab.seechange_streaming.R


class MessageAdapter(private var context: Context, private var messages: List<Message>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.row_chatmessage, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message: Message = messages[position]

        val displayname = if (message.fromStreamer) {
            "You"
        } else {
            message.username
        }

        holder.username!!.text = displayname
        holder.message!!.text = message.message
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var username: TextView? = itemView!!.findViewById(R.id.chat_username)
        var message: TextView? = itemView!!.findViewById(R.id.chat_message)
    }
}