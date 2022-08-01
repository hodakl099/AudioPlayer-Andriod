package com.plcoding.audioplayer.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.plcoding.audioplayer.R
import com.plcoding.audioplayer.adapters.AudioAdapter
import com.plcoding.audioplayer.data.entities.other.Status
import com.plcoding.audioplayer.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var audioAdapter: AudioAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

    }

    private fun setupRecyclerView() = rvAllAudios.apply {
        adapter = audioAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        allAudiosProgressBar.isVisible = false
                        result.data?.let { audios ->
                            audioAdapter.audios = audios
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> allAudiosProgressBar.isVisible = true
                }

        }

    }

}