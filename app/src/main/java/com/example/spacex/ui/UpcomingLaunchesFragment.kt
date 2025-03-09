package com.example.spacex.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spacex.R
import com.example.spacex.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpcomingLaunchesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LaunchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_launches_list, container, false)
    }

    // In UpcomingLaunchesFragment.kt
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = LaunchAdapter(emptyList()) { launchId ->
            Log.d("Navigation", "Attempting to navigate with launchId: $launchId")
            try {
                val bundle = Bundle().apply {
                    putString("launchId", launchId)
                    Log.d("Navigation", "Bundle created with launchId: $launchId")
                }
                findNavController().navigate(
                    R.id.launchDetailsFragment,
                    bundle
                )
                Log.d("Navigation", "Navigation completed")
            } catch (e: Exception) {
                Log.e("Navigation", "Navigation failed", e)
                e.printStackTrace()
            }
        }

        recyclerView.adapter = adapter
        Log.d("Fragment", "Adapter set, loading launches...")
        loadUpcomingLaunches()
    }

    private fun loadUpcomingLaunches() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val launches = withContext(Dispatchers.IO) {
                    RetrofitClient.spaceXApiService.getLaunches()
                }
                val upcomingLaunches = launches.filter { it.upcoming }
                adapter.updateLaunches(upcomingLaunches)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}