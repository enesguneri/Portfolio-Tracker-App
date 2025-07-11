package com.ilk.portfoliotracker.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilk.portfoliotracker.databinding.FragmentAssetDetailBinding
import com.ilk.portfoliotracker.util.downloadImage
import com.ilk.portfoliotracker.util.makePlaceHolder
import com.ilk.portfoliotracker.viewmodel.MarketDataListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class AssetDetailFragment : Fragment() {
    private var _binding: FragmentAssetDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel : MarketDataListViewModel

    private var coinName : String? = null
    private var isNavigatedFromFavoritesList = false
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private var isFavorite = false

    private var favoritesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            coinName = AssetDetailFragmentArgs.fromBundle(it).coinName
            isNavigatedFromFavoritesList = AssetDetailFragmentArgs.fromBundle(it).isNavigatedFromFavoritesList
        }

        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssetDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MarketDataListViewModel::class.java]
        viewModel.refreshData()

        //favoriteslistten gelene farklı marketrecyclerviewden gelene farklı davranış yapılacak.
        binding.imageView.visibility = View.GONE
        binding.assetIDText.visibility = View.GONE
        binding.priceText.visibility = View.GONE
        binding.athText.visibility = View.GONE
        binding.athDateText.visibility = View.GONE
        binding.marketRankText.visibility = View.GONE
        binding.fdvText.visibility = View.GONE
        binding.addFavoriteButton.visibility = View.GONE
        binding.deleteFavoriteButton.visibility = View.GONE

        observeLiveData()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshDataFromAPI()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.addFavoriteButton.setOnClickListener {
            auth.currentUser?.let {
                val coinMap = hashMapOf<String,Any>()
                coinName?.let { it1 -> coinMap.put("coinName", it1) }
                coinMap.put("date",Timestamp.now())
                if (!isFavorite) {
                    coinName?.let { it1 ->
                        db.collection("CryptoDB").document(it.uid).collection("Favorites").document(it1)
                            .set(coinMap).addOnSuccessListener {
                            binding.addFavoriteButton.visibility = View.GONE
                            binding.deleteFavoriteButton.visibility = View.VISIBLE
                        }.addOnFailureListener { exception ->
                            Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        binding.deleteFavoriteButton.setOnClickListener {
            auth.currentUser?.let {
                if (isFavorite) {
                    coinName?.let { it1 ->
                        db.collection("CryptoDB").document(it.uid).collection("Favorites").document(
                            it1
                        ).delete().addOnSuccessListener {
                            binding.addFavoriteButton.visibility = View.VISIBLE
                            binding.deleteFavoriteButton.visibility = View.GONE
                        }.addOnFailureListener {
                            Log.e("Firestore","Can not delete")
                        }
                    }
                }
            }
        }


        observeFavorites()




    }

    private fun observeLiveData() {
        viewModel.coinGeckoDataList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val assetID = getCoinID()
                if (assetID != 999) {
                    binding.imageView.visibility = View.VISIBLE
                    binding.assetIDText.visibility = View.VISIBLE
                    binding.priceText.visibility = View.VISIBLE
                    binding.athText.visibility = View.VISIBLE
                    binding.athDateText.visibility = View.VISIBLE
                    binding.marketRankText.visibility = View.VISIBLE
                    binding.fdvText.visibility = View.VISIBLE




                    binding.assetIDText.text = it[assetID].name
                    binding.priceText.text = "Price: ${it[assetID].current_price.toString()}"
                    binding.athText.text = "ATH:  ${it[assetID].ath.toString()}"
                    binding.athDateText.text =
                        "ATH Date: ${formatDateTimeToDate(it[assetID].ath_date)}"
                    binding.marketRankText.text =
                        "Market Cap Rank: #${it[assetID].market_cap_rank.toString()}"
                    binding.fdvText.text =
                        "Fully Diluted Value: ${formatNumber(it[assetID].fully_diluted_valuation)}"

                    binding.imageView.downloadImage(
                        it[assetID].image,
                        makePlaceHolder(binding.imageView.context)
                    )
                }
            }
            else{
                Log.e("API","Api error")
            }
        }
    }

    private fun getCoinID() : Int{
        val coinList = viewModel.coinGeckoDataList.value
        coinList?.forEach {
            if (it.symbol == coinName) {
                return coinList.indexOf(it)
            }
        }
        return 999
    }


    fun formatDateTimeToDate(dateTime: String?): String {
        if(dateTime != null) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone =
                TimeZone.getTimeZone("UTC") // Z olan kısım UTC zamanını temsil eder.

            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val date = inputFormat.parse(dateTime)
            return outputFormat.format(date ?: Date())
        }
        return ""
    }

    private fun formatNumber(value : Long?): String {
        if (value != null) {
            return when {
                value >= 1_000_000_000_000 -> "${value / 1_000_000_000_000}T"
                value >= 1_000_000_000 -> "${value / 1_000_000_000}B"
                value >= 1_000_000 -> "${value / 1_000_000}M"
                value >= 1_000 -> "${value / 1_000}K"
                else -> value.toString()
            }
        }
        return 0.toString()
    }


    private fun observeFavorites() {
        auth.currentUser?.let { user ->
            val favoritesRef = db.collection("CryptoDB")
                .document(user.uid)
                .collection("Favorites")
                .orderBy("date", Query.Direction.ASCENDING)

            favoritesListener = favoritesRef.addSnapshotListener { snapshot, error ->
                // Fragment'ın view'i hâlâ aktif mi? binding'e güvenli erişim için bu şart.
                if (view == null || !isAdded) return@addSnapshotListener

                if (error != null) {
                    Log.e("Firestore", "Snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }

                isFavorite = snapshot?.documents?.any { doc ->
                    (doc.get("coinName") as? String) == coinName
                } == true

                // binding güvenli çünkü sadece aktif view varsa buraya giriyoruz
                if (isFavorite) {
                    binding.addFavoriteButton.visibility = View.GONE
                    binding.deleteFavoriteButton.visibility = View.VISIBLE
                } else {
                    binding.addFavoriteButton.visibility = View.VISIBLE
                    binding.deleteFavoriteButton.visibility = View.GONE
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        favoritesListener?.remove() // snapshot listener'ı kaldır
        favoritesListener = null
    }
}