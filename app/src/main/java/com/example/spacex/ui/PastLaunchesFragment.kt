package com.example.spacex.ui

import android.os.Bundle
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

class PastLaunchesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LaunchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_launches_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LaunchAdapter(emptyList()) { launchId ->
            try {
                val bundle = Bundle().apply { putString("launchId", launchId) }
                findNavController().navigate(
                    R.id.launchDetailsFragment,  // Navigate directly to the destination
                    bundle
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        recyclerView.adapter = adapter
        loadPastLaunches()
    }

    private fun loadPastLaunches() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val launches = withContext(Dispatchers.IO) {
                    RetrofitClient.spaceXApiService.getLaunches()
                }
                val pastLaunches = launches.filter { !it.upcoming }
                adapter.updateLaunches(pastLaunches)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}