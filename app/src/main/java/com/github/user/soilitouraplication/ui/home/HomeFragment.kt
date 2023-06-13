package com.github.user.soilitouraplication.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.user.soilitouraplication.api.Campaign
import com.github.user.soilitouraplication.database.HistoryDao
import com.github.user.soilitouraplication.databinding.FragmentHomeBinding
import com.github.user.soilitouraplication.ui.fullcampaign.FullCampaign
import com.github.user.soilitouraplication.ui.fullcampaign.DetailCampaign
import com.github.user.soilitouraplication.utils.DateUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class HomeFragment(private val historyDao: HistoryDao) : Fragment(), CampaignAdapter.OnItemClickListener {
    private lateinit var campaignAdapter: CampaignAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    private var recyclerViewState: Parcelable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        campaignAdapter = CampaignAdapter()
        campaignAdapter.setOnItemClickListener(this)
        viewModel.getCampaigns().observe(this) { campaigns ->
            campaignAdapter.setList(campaigns)
        }

        // Retrieve the saved state from the ViewModel
        savedInstanceState?.let { viewModel.restoreState(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupRecyclerView()
        binding.seeall.setOnClickListener {
            val intent = Intent(requireContext(), FullCampaign::class.java)
            startActivity(intent)
        }
        
        binding.goToProfileFragment.setOnClickListener {
//            TODO: Go to profile fragment
        }
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchCampaigns()

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.campaignloader.visibility = View.VISIBLE
            } else {
                binding.campaignloader.visibility = View.GONE
            }
        }

        // Observe changes in the history table
        observeHistoryChanges()
    }

    private fun setupRecyclerView() {
        binding.rvcampaign.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = campaignAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeHistoryChanges() {
        lifecycleScope.launch {
            historyDao.getAllHistory().collectLatest { historyList ->
                if (historyList.isNotEmpty()) {
                    val latestHistory = historyList[0]
                    binding.valueTemperature.text = latestHistory.soil_temperature.toString()
                    binding.valueMoisture.text = latestHistory.soil_moisture.toString()
                    binding.valueSoilcondition.text = latestHistory.soil_condition
                    binding.soiltype.text = latestHistory.soil_type
                    binding.datehistory.text = DateUtils.formatDateTime(latestHistory.created_at)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the state to the ViewModel
        viewModel.saveState(outState)
        // Save the state of the RecyclerView
        recyclerViewState = binding.rvcampaign.layoutManager?.onSaveInstanceState()
        outState.putParcelable(KEY_RECYCLER_VIEW_STATE, recyclerViewState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // Restore the state of the RecyclerView
        recyclerViewState = savedInstanceState?.getParcelable(KEY_RECYCLER_VIEW_STATE)
        recyclerViewState?.let {
            binding.rvcampaign.layoutManager?.onRestoreInstanceState(it)
        }
    }

    override fun onItemClick(campaign: Campaign) {
        val intent = Intent(requireContext(), DetailCampaign::class.java)
        intent.putExtra("campaignId", campaign.id)
        intent.putExtra("campaignTitle", campaign.name)
        intent.putExtra("campaignImage", campaign.image)
        intent.putExtra("campaignDescription", campaign.description)
        intent.putExtra("campaignDate", campaign.created_at)
        intent.putExtra("campaignImage", campaign.image)
        startActivity(intent)
    }

    companion object {
        private const val KEY_RECYCLER_VIEW_STATE = "recycler_view_state"
    }
}
