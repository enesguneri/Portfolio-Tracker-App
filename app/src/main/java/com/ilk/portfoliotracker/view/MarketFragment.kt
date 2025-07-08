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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilk.portfoliotracker.R
import com.ilk.portfoliotracker.adapter.FavoritesRecyclerAdapter
import com.ilk.portfoliotracker.adapter.MarketRecyclerAdapter
import com.ilk.portfoliotracker.databinding.FragmentMarketBinding
import com.ilk.portfoliotracker.model.CoinGeckoData
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
    private val favoritesRecyclerAdapter = FavoritesRecyclerAdapter(arrayListOf())
    private var isFavoriteSwitchOn = false

    private lateinit var privateSharedPreferences : PrivateSharedPreferences

    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }


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



        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesRecyclerView.adapter = favoritesRecyclerAdapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.marketRecyclerView.visibility = View.GONE
            binding.allCoinsText.visibility = View.GONE
            binding.favoritesRecyclerView.visibility = View.GONE
            binding.favoritesText.visibility = View.GONE
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

        binding.switchFavorites.setOnCheckedChangeListener { _, isChecked ->
            isFavoriteSwitchOn = isChecked
            if (isFavoriteSwitchOn) {
                binding.favoritesRecyclerView.visibility = View.VISIBLE
                binding.favoritesText.visibility = View.VISIBLE
                binding.allCoinsText.visibility = View.GONE
                binding.marketRecyclerView.visibility = View.GONE
            } else {
                binding.favoritesRecyclerView.visibility = View.GONE
                binding.favoritesText.visibility = View.GONE
                binding.allCoinsText.visibility = View.VISIBLE
                binding.marketRecyclerView.visibility = View.VISIBLE
            }
        }
        observeLiveData()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun observeFavorites(coinList: List<CoinGeckoData>){
        val favoritesList : ArrayList<CoinGeckoData> = arrayListOf()
        auth.currentUser?.let {
            db.collection("CryptoDB").document(it.uid).collection("Favorites").get().addOnSuccessListener { value ->
                if (value != null) {
                    for (document in value) {
                        if (coinList.isNotEmpty()) {
                            for (coin in coinList) {
                                if (document.get("coinName") == coin.symbol) {
                                    favoritesList.add(coin)
                                }
                            }
                        }
                    }
                }

                favoritesRecyclerAdapter.updateFavoritesList(favoritesList)

                if (favoritesList.isEmpty()) {
                    binding.favoritesText.visibility = View.GONE
                    binding.favoritesRecyclerView.visibility = View.GONE
                    binding.allCoinsText.visibility = View.VISIBLE
                    binding.marketRecyclerView.visibility = View.VISIBLE
                }
                else if (isFavoriteSwitchOn) {
                    binding.favoritesText.visibility = View.VISIBLE
                    binding.favoritesRecyclerView.visibility = View.VISIBLE
                    binding.allCoinsText.visibility = View.GONE
                    binding.marketRecyclerView.visibility = View.GONE
                }
                else {
                    binding.favoritesText.visibility = View.GONE
                    binding.favoritesRecyclerView.visibility = View.GONE
                    binding.allCoinsText.visibility = View.VISIBLE
                    binding.marketRecyclerView.visibility = View.VISIBLE
                }

            }
        }

    }

    private fun observeLiveData() {
        viewModel.coinGeckoDataList.observe(viewLifecycleOwner) {
            marketRecyclerAdapter.updateAssetList(it)
            observeFavorites(it)

            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
            if (!isFavoriteSwitchOn) {
                binding.marketRecyclerView.visibility = View.VISIBLE
                binding.allCoinsText.visibility = View.VISIBLE
            }
            binding.progressBar.visibility = View.GONE
            binding.assetExceptionAlert.visibility = View.GONE

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
                binding.allCoinsText.visibility = View.GONE

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
