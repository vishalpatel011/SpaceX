package com.example.spacex.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spacex.R
import com.example.spacex.model.Launch
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

// Modify your LaunchAdapter to include image loading:
class LaunchAdapter(
    private var launches: List<Launch>,
    private val onLaunchClick: (String) -> Unit
) : RecyclerView.Adapter<LaunchAdapter.LaunchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaunchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_launch, parent, false)
        return LaunchViewHolder(view)
    }

    override fun onBindViewHolder(holder: LaunchViewHolder, position: Int) {
        val launch = launches[position]
        holder.bind(launch)

        holder.itemView.setOnClickListener {
            launch.id?.let { id -> onLaunchClick(id) }
        }
    }

    override fun getItemCount(): Int = launches.size

    fun updateLaunches(newLaunches: List<Launch>) {
        launches = newLaunches
        notifyDataSetChanged()
    }

    class LaunchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val launchName: TextView = itemView.findViewById(R.id.launchName)
        private val launchDate: TextView = itemView.findViewById(R.id.launchDate)
        private val launchImage: ImageView = itemView.findViewById(R.id.launchImage)

        fun bind(launch: Launch) {
            launchName.text = launch.name
            launchDate.text = launch.date_utc

            // Load image from URL
            launch.links?.patch?.small?.let { imageUrl ->
                loadImageFromUrl(imageUrl)
            } ?: run {
                launchImage.setImageResource(R.drawable.rocket_placeholder)
            }
        }

        private fun loadImageFromUrl(imageUrl: String) {
            itemView.context?.let { context ->
                try {
                    // Use Kotlin coroutines for non-UI blocking
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val url = URL(imageUrl)
                            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                            withContext(Dispatchers.Main) {
                                launchImage.setImageBitmap(bitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                launchImage.setImageResource(R.drawable.rocket_placeholder)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    launchImage.setImageResource(R.drawable.rocket_placeholder)
                }
            }
        }
    }
}