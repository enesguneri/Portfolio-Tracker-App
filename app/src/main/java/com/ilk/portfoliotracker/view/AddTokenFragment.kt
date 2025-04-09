package com.ilk.portfoliotracker.view

import android.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilk.portfoliotracker.databinding.FragmentAddTokenBinding
import com.ilk.portfoliotracker.model.CoinGeckoData
import com.ilk.portfoliotracker.viewmodel.MarketDataListViewModel

class AddTokenFragment : Fragment() {

    private var _binding: FragmentAddTokenBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel : MarketDataListViewModel
    private lateinit var selectedAsset : CoinGeckoData

    private lateinit var auth : FirebaseAuth
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
        _binding = FragmentAddTokenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MarketDataListViewModel::class.java]
        viewModel.refreshData()

        viewModel.coinGeckoDataList.observe(viewLifecycleOwner){
            val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, viewModel.coinGeckoDataList.value!!.map { it.name })
            binding.assetAutoCompleteTextView.setAdapter(adapter)
            binding.assetAutoCompleteTextView.threshold = 3
        }

        binding.assetAutoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            selectedAsset = viewModel.coinGeckoDataList.value!!.find { it.name == selectedItem }!!

        }

        binding.saveButton.setOnClickListener {
            val amount = binding.amountText.text.toString().toDoubleOrNull()
            val assetMap = hashMapOf<String, Any>()
            assetMap.put("date",Timestamp.now())
            if(selectedAsset != null){
                assetMap.put("assetName",selectedAsset.name.toString())
            }
            if(amount != null){
                assetMap.put("assetAmount",amount)
            }
            val price = binding.costText.text.toString().toDoubleOrNull()
            if(price != null){
                assetMap.put("assetCost",price)
            }
            assetMap.put("userID",auth.currentUser?.uid.toString())
            auth.currentUser?.let { it1 ->
                db.collection("CryptoDB").document(it1.uid).collection("Position").add(assetMap).addOnSuccessListener { documentReference ->
                    val action = AddTokenFragmentDirections.actionAddTokenFragmentToPortfolioFragment()
                    Navigation.findNavController(view).navigate(action)
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }

            //coini aldığın fiyatı girmek yerine kaç dolarlık aldığını da girebilme eklenebilir.(Kullanıcı hangisni girmek istediğini seçsin.)

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}