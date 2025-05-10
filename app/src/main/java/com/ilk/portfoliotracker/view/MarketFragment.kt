package com.ilk.portfoliotracker.view

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ilk.portfoliotracker.R
import com.ilk.portfoliotracker.adapter.MarketRecyclerAdapter
import com.ilk.portfoliotracker.databinding.FragmentMarketBinding
import com.ilk.portfoliotracker.util.PrivateSharedPreferences
import com.ilk.portfoliotracker.viewmodel.MarketDataListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MarketFragment : Fragment() {

    private var _binding: FragmentMarketBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel : MarketDataListViewModel
    private val marketRecyclerAdapter = MarketRecyclerAdapter(arrayListOf())

    private lateinit var privateSharedPreferences : PrivateSharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMarketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MarketDataListViewModel::class.java]
        if (isNetworkAvailable(requireContext()))
            viewModel.refreshData()
        else
            viewModel.getDataFromRoomDB()

        binding.marketRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.marketRecyclerView.adapter = marketRecyclerAdapter

        observeLiveData()


        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.marketRecyclerView.visibility = View.GONE
            binding.assetExceptionAlert.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            viewModel.refreshDataFromAPI()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isNetworkAvailable(requireContext())) {
                    delay(1000) // 1 saniye bekle
                    viewModel.refreshDataFromAPI()
                }
            }
        }




        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.doOnLayout {
            binding.marketRecyclerView.setPadding(
                binding.marketRecyclerView.paddingLeft,
                binding.marketRecyclerView.paddingTop,
                binding.marketRecyclerView.paddingRight,
                bottomNav.height
            )
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    private fun observeLiveData() {
        viewModel.coinGeckoDataList.observe(viewLifecycleOwner) {
            marketRecyclerAdapter.updateAssetList(it)
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
            binding.marketRecyclerView.visibility = View.VISIBLE
        }


        viewModel.dataExceptionAlert.observe(viewLifecycleOwner) {
            if(it && isNetworkAvailable(requireContext())) {
                binding.assetExceptionAlert.visibility = View.VISIBLE
                binding.marketRecyclerView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            } else {
                binding.assetExceptionAlert.visibility = View.GONE
            }
        }

        viewModel.dataLoading.observe(viewLifecycleOwner) {
            if(it) {
                binding.shimmerLayout.startShimmer()
                binding.shimmerLayout.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.marketRecyclerView.visibility = View.GONE
                binding.assetExceptionAlert.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
