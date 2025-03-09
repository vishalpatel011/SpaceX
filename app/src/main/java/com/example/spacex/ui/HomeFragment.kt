package com.example.spacex.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spacex.R
import com.example.spacex.api.RetrofitClient
import com.example.spacex.model.Launch
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var countdownTimer: CountDownTimer? = null
    private var nextLaunchDate: Date? = null
    private lateinit var upcomingAdapter: LaunchAdapter
    private lateinit var pastAdapter: LaunchAdapter
    private var currentTab = 0 // 0 for upcoming, 1 for past
    private lateinit var viewFlipper: ViewFlipper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewFlipper to switch between screens
        viewFlipper = view.findViewById(R.id.viewFlipper)

        // Left panel (countdown) views
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val rocketNameText = view.findViewById<TextView>(R.id.rocketName)
        val countdownText = view.findViewById<TextView>(R.id.countdownTimer)
        val rocketImage = view.findViewById<ImageView>(R.id.rocketImage)

        // Right panel (launches) views
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val launchesRecyclerView = view.findViewById<RecyclerView>(R.id.launchesRecyclerView)

        // Current launch details
        val currentLaunchImage = view.findViewById<ImageView>(R.id.launchImage)
        val launchDate = view.findViewById<TextView>(R.id.launchDate)
        val launchLocationDate = view.findViewById<TextView>(R.id.launchLocationDate)
        val launchLocation = view.findViewById<TextView>(R.id.launchLocation)

        // Bottom navigation
        val homeIcon = view.findViewById<ImageView>(R.id.homeIcon)
        val launchesIcon = view.findViewById<ImageView>(R.id.launchesIcon)

        // Setup bottom navigation click listeners
        homeIcon.setOnClickListener {
            viewFlipper.displayedChild = 0 // Show home/countdown screen
            updateBottomNavIcons(homeIcon, launchesIcon, 0)
        }

        launchesIcon.setOnClickListener {
            viewFlipper.displayedChild = 1 // Show launches screen
            updateBottomNavIcons(homeIcon, launchesIcon, 1)
        }



        // Setup RecyclerView
        launchesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapters
        upcomingAdapter = LaunchAdapter(emptyList()) { launchId ->
            navigateToLaunchDetails(launchId)
        }

        pastAdapter = LaunchAdapter(emptyList()) { launchId ->
            navigateToLaunchDetails(launchId)
        }

        // Set initial adapter
        launchesRecyclerView.adapter = upcomingAdapter

        // Setup tab selection listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                when (tab.position) {
                    0 -> launchesRecyclerView.adapter = upcomingAdapter
                    1 -> launchesRecyclerView.adapter = pastAdapter
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Load all launches data
        loadAllLaunchesData(
            progressBar,
            rocketNameText,
            countdownText,
            rocketImage,
            currentLaunchImage,
            launchDate,
            launchLocationDate,
            launchLocation
        )
    }

    private fun updateBottomNavIcons(homeIcon: ImageView, launchesIcon: ImageView, activeIndex: Int) {
        // Reset all icons to secondary color
        homeIcon.setColorFilter(resources.getColor(R.color.spacex_text_secondary, null))
        launchesIcon.setColorFilter(resources.getColor(R.color.spacex_text_secondary, null))

        // Set active icon to primary color
        when (activeIndex) {
            0 -> homeIcon.setColorFilter(resources.getColor(R.color.spacex_text_primary, null))
            1 -> launchesIcon.setColorFilter(resources.getColor(R.color.spacex_text_primary, null))
        }
    }
    private fun navigateToLaunchDetails(launchId: String) {
        try {
            val bundle = bundleOf("launchId" to launchId)
            findNavController().navigate(R.id.launchDetailsFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed: ${e.message}", e)
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAllLaunchesData(
        progressBar: ProgressBar,
        rocketNameText: TextView,
        countdownText: TextView,
        rocketImage: ImageView,
        currentLaunchImage: ImageView,
        launchDate: TextView,
        launchLocationDate: TextView,
        launchLocation: TextView
    ) {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE

                // Fetch all launches
                val allLaunches = RetrofitClient.spaceXApiService.getLaunches()

                // Split into upcoming and past launches
                val upcomingLaunches = allLaunches.filter { it.upcoming }.sortedBy { it.date_utc }
                val pastLaunches = allLaunches.filter { !it.upcoming }.sortedByDescending { it.date_utc }

                // Update adapters
                upcomingAdapter.updateLaunches(upcomingLaunches)
                pastAdapter.updateLaunches(pastLaunches)

                // Find next upcoming launch
                val nextLaunch = upcomingLaunches.firstOrNull()

                if (nextLaunch != null) {
                    processNextLaunch(
                        nextLaunch,
                        rocketNameText,
                        countdownText,
                        rocketImage,
                        currentLaunchImage,
                        launchDate,
                        launchLocationDate,
                        launchLocation
                    )
                } else {
                    // Handle case with no upcoming launches
                    rocketNameText.text = "NO UPCOMING LAUNCHES"
                    countdownText.text = "--:--:--"
                    launchDate.text = "--:--:--"
                    launchLocationDate.text = "TBD"
                    launchLocation.text = "Location unavailable"
                }

                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error loading launches: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun processNextLaunch(
        nextLaunch: Launch,
        rocketNameText: TextView,
        countdownText: TextView,
        rocketImage: ImageView,
        currentLaunchImage: ImageView,
        launchDate: TextView,
        launchLocationDate: TextView,
        launchLocation: TextView
    ) {
        try {
            // Store the launch ID for navigation
            val launchId = nextLaunch.id

            // Add click listeners to both rocket images
            rocketImage.setOnClickListener {
                navigateToLaunchDetails(launchId)
            }

            currentLaunchImage.setOnClickListener {
                navigateToLaunchDetails(launchId)
            }

            // Make images appear clickable
            rocketImage.isClickable = true
            currentLaunchImage.isClickable = true

            // Update rocket name
            rocketNameText.text = nextLaunch.name?.uppercase() ?: "UNNAMED MISSION"

            // Parse and handle date
            val dateString = nextLaunch.date_utc
            if (!dateString.isNullOrEmpty()) {
                nextLaunchDate = parseDate(dateString)

                if (nextLaunchDate != null) {
                    // Format date for UI
                    val displayFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                    val dateOnlyFormat = SimpleDateFormat("MMMM dd", Locale.US)

                    // Check if launch has already occurred
                    // Check if launch has already occurred
                    val now = Date()
                    if (nextLaunchDate!!.before(now)) {
                        // Launch already occurred - show LAUNCH COMPLETED followed by 00:00:00
                        countdownText.text = "LAUNCH COMPLETED\n00:00:00"
                    } else {
                        // Launch is in future, set up countdown
                        setupCountdownTimer(countdownText)
                    }

                    launchDate.text = displayFormat.format(nextLaunchDate!!)
                    launchLocationDate.text = dateOnlyFormat.format(nextLaunchDate!!)
                } else {
                    countdownText.text = "--:--:--"
                    launchDate.text = "--:--:--"
                    launchLocationDate.text = "TBD"
                }
            } else {
                countdownText.text = "--:--:--"
                launchDate.text = "--:--:--"
                launchLocationDate.text = "TBD"
            }

            // Rest of your existing code...
            // Set location and load images
            lifecycleScope.launch {
                try {
                    if (nextLaunch.launchpad != null) {
                        Log.d(TAG, "Launchpad ID: ${nextLaunch.launchpad}")
                        launchLocation.text = "Launchpad: ${nextLaunch.launchpad}"
                    } else {
                        launchLocation.text = "Cape Canaveral, Florida" // Default location
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error with launchpad: ${e.message}", e)
                    launchLocation.text = "Location unavailable"
                }
            }

            loadRocketImage(nextLaunch, rocketImage)

            // Load patch for current launch card
            val patchUrl = nextLaunch.links?.patch?.small
            if (!patchUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(patchUrl)
                    .placeholder(R.drawable.rocket_placeholder)
                    .error(R.drawable.rocket_placeholder)
                    .into(currentLaunchImage)
            } else {
                currentLaunchImage.setImageResource(R.drawable.rocket_placeholder)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing next launch: ${e.message}", e)
            // Set fallback values in case of error
            rocketNameText.text = "UPCOMING MISSION"
            countdownText.text = "--:--:--"
            launchDate.text = "--:--:--"
            launchLocationDate.text = "TBD"
            launchLocation.text = "Location unavailable"
            rocketImage.setImageResource(R.drawable.rocket_placeholder)
            currentLaunchImage.setImageResource(R.drawable.rocket_placeholder)
        }
    }

    private fun parseDate(dateString: String): Date? {
        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssX"
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(dateString)
            } catch (e: Exception) {
                continue
            }
        }

        return null
    }

    private fun loadRocketImage(nextLaunch: Launch, rocketImage: ImageView) {
        lifecycleScope.launch {
            try {
                if (!nextLaunch.rocket.isNullOrEmpty()) {
                    try {
                        val rocket = RetrofitClient.spaceXApiService.getRocketById(nextLaunch.rocket)
                        val images = rocket.flickr_images

                        if (!images.isNullOrEmpty()) {
                            Picasso.get()
                                .load(images[0])
                                .placeholder(R.drawable.rocket_placeholder)
                                .error(R.drawable.rocket_placeholder)
                                .into(rocketImage)
                            return@launch
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading rocket: ${e.message}", e)
                        // Continue to fallback options
                    }
                }

                // Try to use patch image as fallback
                val imageUrl = nextLaunch.links?.patch?.small
                if (!imageUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.rocket_placeholder)
                        .error(R.drawable.rocket_placeholder)
                        .into(rocketImage)
                } else {
                    rocketImage.setImageResource(R.drawable.rocket_placeholder)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading rocket image: ${e.message}", e)
                rocketImage.setImageResource(R.drawable.rocket_placeholder)
            }
        }
    }

    private fun setupCountdownTimer(countdownText: TextView) {
        try {
            // Cancel any existing timer
            countdownTimer?.cancel()

            if (nextLaunchDate == null) {
                countdownText.text = "--:--:--"
                return
            }

            val currentTime = System.currentTimeMillis()
            val launchTime = nextLaunchDate!!.time

            // Calculate time difference
            val difference = launchTime - currentTime

            if (difference <= 0) {
                // If launch time has passed
                countdownText.text = "LAUNCHED"
                return
            }

            // Create a new countdown timer
            countdownTimer = object : CountDownTimer(difference, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    try {
                        // Format remaining time
                        val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                        // Update UI
                        val formattedTime = if (days > 0) {
                            String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds)
                        } else {
                            String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        }

                        countdownText.text = formattedTime
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating countdown: ${e.message}", e)
                    }
                }

                override fun onFinish() {
                    countdownText.text = "LAUNCHED"
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up countdown: ${e.message}", e)
            countdownText.text = "--:--:--"
        }
    }

    override fun onDestroyView() {
        // Clean up timer to prevent memory leaks
        countdownTimer?.cancel()
        countdownTimer = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}