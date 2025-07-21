package com.ilk.portfoliotracker.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilk.portfoliotracker.databinding.FragmentMyAssetBinding
import com.ilk.portfoliotracker.model.MyAsset
import androidx.navigation.findNavController
import kotlinx.coroutines.launch

class MyAssetFragment : Fragment() {
    private var _binding: FragmentMyAssetBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    private val assetList : ArrayList<MyAsset> = arrayListOf()
    private lateinit var currentDocument : DocumentSnapshot

    private var assetName : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            assetName = MyAssetFragmentArgs.fromBundle(it).coinName
        }

        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyAssetBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            getDataFromFirestore()
        } catch (e : Exception){
            Log.e("MyAssetFragment", "Error getting data from Firestore")
        }
        binding.deleteButton.setOnClickListener {
            auth.currentUser?.let {
                currentDocument.reference.delete().addOnSuccessListener {
                    Toast.makeText(requireContext(),"Asset has been deleted successfully.", Toast.LENGTH_LONG).show()
                    val action = MyAssetFragmentDirections.actionMyAssetFragmentToPortfolioFragment()
                    view.findNavController().navigate(action)
                }
            }
        }
        binding.editButton.setOnClickListener {
            handleEditButton(view)
        }
    }

    private fun handleEditButton(view: View){
        val newAmount = binding.assetAmount.text.toString().toDoubleOrNull()
        val newCost = binding.assetCost.text.toString().toDoubleOrNull()
        if (newAmount == null || newCost == null){
            Toast.makeText(requireContext(),"Please enter the valid cost and amount.", Toast.LENGTH_LONG).show()

        }
        else {
            val updatedData = hashMapOf<String,Any>("assetAmount" to newAmount,"assetCost" to newCost)
            currentDocument.reference.update(updatedData).addOnSuccessListener {
                Toast.makeText(requireContext(),"The asset has been updated.", Toast.LENGTH_LONG).show()
            }
            val action = MyAssetFragmentDirections.actionMyAssetFragmentToPortfolioFragment()
            view.findNavController().navigate(action)
        }
    }

    private fun getDataFromFirestore(){
        viewLifecycleOwner.lifecycleScope.launch {
            auth.currentUser?.let {
                db.collection("CryptoDB").document(it.uid).collection("Position")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .addSnapshotListener { value, error ->
                        if (view == null || !isAdded)
                            return@addSnapshotListener
                        if (error != null) {
                            Toast.makeText(
                                requireContext(),
                                error.localizedMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            if (value != null) {
                                if (value.isEmpty) {
                                    Toast.makeText(
                                        requireContext(),
                                        "No data found",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    val documents = value.documents
                                    assetList.clear()
                                    for (document in documents) {
                                        val amount = document.get("assetAmount") as Double
                                        val cost = document.get("assetCost") as Double
                                        val name = document.get("assetName") as String
                                        if (name == assetName) {
                                            binding.assetName.text = name
                                            binding.assetAmount.setText(amount.toString())
                                            binding.assetCost.setText(cost.toString())
                                            currentDocument = document
                                        }
                                    }

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