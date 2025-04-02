package com.example.maptesting

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter(private val context: Context, private val list: List<Item>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

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
        holder.imageView.setImageResource(item.image)

        // sets the text to the textview from our itemHolder class
        holder.textView.text = item.text

        holder.toChall.setOnClickListener {
            val intent = Intent(context, CurrentChallengePage::class.java) // Replace with your target activity
            intent.putExtra("itemText", item.text) // Pass any necessary data
            context.startActivity(intent)
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
