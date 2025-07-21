package com.ilk.portfoliotracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ilk.portfoliotracker.R
import com.ilk.portfoliotracker.databinding.MarketRecyclerRowBinding
import com.ilk.portfoliotracker.model.CoinGeckoData
import com.ilk.portfoliotracker.util.downloadImage
import com.ilk.portfoliotracker.util.makePlaceHolder
import com.ilk.portfoliotracker.view.MarketFragmentDirections
import androidx.navigation.findNavController

class MarketRecyclerAdapter(val assetList : ArrayList<CoinGeckoData>) : RecyclerView.Adapter<MarketRecyclerAdapter.MarketViewHolder>() {

    class MarketViewHolder(val binding : MarketRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
    }
    private var displayList = ArrayList<CoinGeckoData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketViewHolder {
        val binding = MarketRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MarketViewHolder(binding)
    }

    fun updateAssetList(newList : List<CoinGeckoData>) {
        assetList.clear()
        assetList.addAll(newList)
        displayList.clear()
        displayList.addAll(newList)
        notifyDataSetChanged()
    }

    fun filterList(query: String) {
        displayList.clear()
        if (query.isEmpty() || query.length < 2) {
            displayList.addAll(assetList)
        } else {
            val filteredList = assetList.filter { it.name?.contains(query, ignoreCase = true) == true
                    || it.symbol?.contains(query, ignoreCase = true) == true
            }
            displayList.addAll(filteredList)
        }
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return displayList.size
    }

    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        holder.binding.assetID.text = displayList[position].name
        holder.binding.priceAnd24HChangeText.text = displayList[position].current_price?.let {
            formatPrice(it)
        }
        if(displayList[position].price_change_percentage_24h != null && displayList[position].price_change_percentage_24h!! < 0) {
            holder.binding.priceChangeText.setTextColor(holder.itemView.context.resources.getColor(R.color.red))
        } else
            holder.binding.priceChangeText.setTextColor(holder.itemView.context.resources.getColor(R.color.green))

        holder.binding.priceChangeText.text = "%${String.format("%.2f",displayList[position].price_change_percentage_24h)}"
        holder.binding.marketImageView.downloadImage(displayList[position].image, makePlaceHolder(holder.itemView.context))
        holder.itemView.setOnClickListener {
            val action = MarketFragmentDirections.actionMarketFragmentToAssetDetailFragment(displayList[position].symbol,false)
            it.findNavController().navigate(action)
        }
    }


    fun formatPrice(price: Double): String {
        return when {
            price >= 1 -> String.format("%.2f", price)       // 2 basamak göster
            price >= 0.01 -> String.format("%.4f", price)     // 4 basamak göster
            price >= 0.000001 -> String.format("%.8f", price) // çok küçükse 6 basamak
            price >= 0.00000001 -> String.format("%.10f", price) // çok küçükse 8 basamak
            price >= 0.0000000001 -> String.format("%.12f", price) // çok küçükse 10 basamak
            else -> String.format("%.15f",price)
        }
    }


}