package com.example.maptesting

import android.content.Context
import android.content.Intent
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.security.AccessController.getContext

class Adapter(private val context: Context, private val list: List<Item>, private val onChallengeClick: (Item) -> Unit) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        // sets the image to the imageview from our itemHolder class

        //TODO: change to image
        //item.photo into glide download image
        holder.imageView.setImageResource(R.drawable.challenger_light_mode_logo)
        Glide.with(context)
            .load(item.photo)
            .into(holder.imageView);
        // sets the text to the textview from our itemHolder class
        holder.textView.text = item.text

        holder.toChall.setOnClickListener {
            onChallengeClick(item) // Pass the clicked challenge
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return list.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val toChall: ImageButton = itemView.findViewById(R.id.toChall)
    }
}
