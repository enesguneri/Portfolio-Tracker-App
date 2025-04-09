package com.ilk.portfoliotracker.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilk.portfoliotracker.adapter.MyAssetAdapter
import com.ilk.portfoliotracker.databinding.FragmentPortfolioBinding
import com.ilk.portfoliotracker.model.MyAsset
import com.ilk.portfoliotracker.viewmodel.MarketDataListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    private val assetList : ArrayList<MyAsset> = arrayListOf()

    private lateinit var adapter : MyAssetAdapter

    private lateinit var viewModel : MarketDataListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            getDataFromFirestore()
            if (assetList != null)
                adapter = MyAssetAdapter(assetList, arrayListOf())

            binding.portfolioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.portfolioRecyclerView.adapter = adapter


            binding.floatingActionButton.setOnClickListener {
                val action = PortfolioFragmentDirections.actionPortfolioFragmentToAddTokenFragment()
                Navigation.findNavController(view).navigate(action)
            }

            viewModel = ViewModelProvider(this)[MarketDataListViewModel::class.java]
            viewModel.refreshData()
            viewModel.coinGeckoDataList.observe(viewLifecycleOwner) {
                adapter.updateAssetList(it)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    while (true) {
                        delay(1000) // 1 saniye bekle
                        viewModel.refreshDataFromAPI()
                    }
                }
            }
        }catch (e : Exception){
            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
        }

    }

    private fun getDataFromFirestore(){
        auth.currentUser?.let {
            db.collection("CryptoDB").document(it.uid).collection("Position").orderBy("date",Query.Direction.ASCENDING).addSnapshotListener{ value, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                }else {
                    if (value != null) {
                        if (value.isEmpty) {
                            Toast.makeText(requireContext(), "No data found", Toast.LENGTH_LONG).show()
                        } else {
                            val documents = value.documents
                            assetList.clear()
                            for (document in documents) {
                                val amount = document.get("assetAmount") as Double
                                val cost = document.get("assetCost") as Double
                                val name = document.get("assetName") as String
                                val userID = auth.currentUser?.uid

                                val myAsset = MyAsset(amount, cost, name, userID)
                                assetList.add(myAsset)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}