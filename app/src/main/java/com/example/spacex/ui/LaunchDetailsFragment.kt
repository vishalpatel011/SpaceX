package com.example.spacex.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spacex.R
import com.example.spacex.api.RetrofitClient
import com.example.spacex.model.Launch
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class LaunchDetailsFragment : Fragment() {
    private val TAG = "LaunchDetailsFragment"

    private lateinit var progressBar: ProgressBar
    private lateinit var launchName: TextView
    private lateinit var launchDate: TextView
    private lateinit var launchDetails: TextView
    private lateinit var launchPatch: ImageView
    private lateinit var launchStatus: TextView
    private lateinit var launchLocation: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_launch_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        progressBar = view.findViewById(R.id.progress_bar)
        launchName = view.findViewById(R.id.launch_name)
        launchDate = view.findViewById(R.id.launch_date)
        launchDetails = view.findViewById(R.id.launch_details)
        launchPatch = view.findViewById(R.id.launch_patch)

        // Get these if they exist in your layout
        try {
            launchStatus = view.findViewById(R.id.launch_status)
            launchLocation = view.findViewById(R.id.launch_location)
        } catch (e: Exception) {
            Log.e(TAG, "Views not found: ${e.message}")
        }

        // Debug all arguments
        Log.d(TAG, "Arguments received: ${arguments?.toString()}")
        arguments?.keySet()?.forEach { key ->
            Log.d(TAG, "Argument key: $key, value: ${arguments?.get(key)}")
        }

        // Get launch ID from arguments - check all possible parameter names
        val launchId = arguments?.getString("launchId")
            ?: arguments?.getString("launch_id")
            ?: arguments?.getString("id")
            ?: arguments?.getString("item_id") // Add more potential keys

        Log.d(TAG, "Received launch ID: $launchId")

        if (launchId != null) {
            fetchLaunchDetails(launchId)
        } else {
            Toast.makeText(context, "Error: Launch ID not found", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Launch ID not found in arguments")
        }
    }

    private fun fetchLaunchDetails(launchId: String) {
        progressBar.visibility = View.VISIBLE
        Log.d(TAG, "Fetching launch details for ID: $launchId")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Attempt to fetch launch details
                var launch: Launch? = null

                try {
                    Log.d(TAG, "Attempting to fetch launch by ID directly...")
                    launch = withContext(Dispatchers.IO) {
                        RetrofitClient.spaceXApiService.getLaunchById(launchId)
                    }
                    Log.d(TAG, "Fetch successful, launch data: $launch")
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching launch by ID: ${e.message}", e)

                    try {
                        Log.d(TAG, "Falling back to fetching all launches...")
                        val launches = withContext(Dispatchers.IO) {
                            RetrofitClient.spaceXApiService.getLaunches()
                        }
                        Log.d(TAG, "Fetched ${launches.size} launches")

                        launch = launches.find { it.id == launchId }
                        Log.d(TAG, "Found matching launch in list: ${launch != null}")
                    } catch (e2: Exception) {
                        Log.e(TAG, "Error in fallback method: ${e2.message}", e2)
                    }
                }

                if (launch != null) {
                    Log.d(TAG, "Launch found: ${launch.name}")
                    updateUIWithLaunchData(launch)
                } else {
                    Log.e(TAG, "Launch not found for ID: $launchId")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Launch not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in fetchLaunchDetails: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error loading launch details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUIWithLaunchData(launch: Launch) {
        try {
            // Update UI with launch details
            launchName.text = launch.name ?: "Unknown Mission"

            // Format the date nicely
            val formattedDate = launch.date_utc?.let { formatDate(it) } ?: "Date unavailable"
            launchDate.text = formattedDate

            // Set details with fallback
            launchDetails.text = launch.details ?: "No details available for this mission."

            // Set launch status if view exists
            if (::launchStatus.isInitialized) {
                when (launch.success) {
                    true -> {
                        launchStatus.text = "LAUNCH SUCCESSFUL"
                        launchStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                    }
                    false -> {
                        launchStatus.text = "LAUNCH FAILED"
                        launchStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                    }
                    else -> {
                        val isUpcoming = launch.upcoming ?: false
                        launchStatus.text = if (isUpcoming) "UPCOMING" else "STATUS UNKNOWN"
                        launchStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    }
                }
            }

            // Fetch and set launchpad info if available
            if (::launchLocation.isInitialized && !launch.launchpad.isNullOrEmpty()) {
                fetchLaunchpadInfo(launch.launchpad)
            }

            // Load patch image
            loadPatchImage(launch)

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI with launch data: ${e.message}", e)
        }
    }

    private fun loadPatchImage(launch: Launch) {
        // Get patch URL
        val patchUrl = launch.links?.patch?.large ?: launch.links?.patch?.small

        // Debug info about the patch URL
        Log.d(TAG, "Patch URL retrieved: $patchUrl")

        if (!patchUrl.isNullOrEmpty()) {
            // Try to load the image
            Picasso.get()
                .load(patchUrl)
                .placeholder(R.drawable.rocket_placeholder)
                .error(R.drawable.rocket_placeholder)
                .into(launchPatch, object : Callback {
                    override fun onSuccess() {
                        Log.d(TAG, "Patch image loaded successfully")
                    }

                    override fun onError(e: Exception?) {
                        Log.e(TAG, "Error loading patch image: ${e?.message}", e)
                        launchPatch.setImageResource(R.drawable.rocket_placeholder)
                    }
                })
        } else {
            Log.d(TAG, "No patch URL available")
            launchPatch.setImageResource(R.drawable.rocket_placeholder)
        }
    }

    private fun fetchLaunchpadInfo(launchpadId: String) {
        Log.d(TAG, "Fetching launchpad info for ID: $launchpadId")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val launchpad = withContext(Dispatchers.IO) {
                    RetrofitClient.spaceXApiService.getLaunchpadById(launchpadId)
                }

                Log.d(TAG, "Launchpad data received: $launchpad")

                if (::launchLocation.isInitialized) {
                    val locationText = when {
                        !launchpad.full_name.isNullOrEmpty() -> launchpad.full_name
                        !launchpad.name.isNullOrEmpty() -> launchpad.name
                        else -> "Unknown Location"
                    }

                    // Add locality/region if available
                    val additionalInfo = when {
                        !launchpad.locality.isNullOrEmpty() && !launchpad.region.isNullOrEmpty() ->
                            "${launchpad.locality}, ${launchpad.region}"
                        !launchpad.locality.isNullOrEmpty() -> launchpad.locality
                        !launchpad.region.isNullOrEmpty() -> launchpad.region
                        else -> null
                    }

                    val finalText = if (additionalInfo != null) {
                        "$locationText\n$additionalInfo"
                    } else {
                        locationText
                    }

                    // Update UI on main thread
                    withContext(Dispatchers.Main) {
                        launchLocation.text = finalText
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching launchpad info: ${e.message}", e)
            }
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm:ss z", Locale.US)
            outputFormat.timeZone = TimeZone.getDefault()

            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: return dateStr)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date with primary format: ${e.message}")
            try {
                // Try alternative format
                val altInputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                altInputFormat.timeZone = TimeZone.getTimeZone("UTC")

                val date = altInputFormat.parse(dateStr)
                if (date != null) {
                    val outputFormat = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm:ss z", Locale.US)
                    outputFormat.timeZone = TimeZone.getDefault()
                    return outputFormat.format(date)
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Error parsing date with alternative format: ${e2.message}")
            }
            dateStr
        }
    }
}