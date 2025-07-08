package com.ilk.portfoliotracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ilk.portfoliotracker.R
import com.ilk.portfoliotracker.databinding.MarketRecyclerRowBinding
import com.ilk.portfoliotracker.model.BinanceData
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
        holder.binding.priceAnd24HChangeText.text = "${displayList[position].current_price}"
        if(displayList[position].price_change_percentage_24h != null && displayList[position].price_change_percentage_24h!! < 0) {
            holder.binding.priceChangeText.setTextColor(holder.itemView.context.resources.getColor(R.color.red))
        } else
            holder.binding.priceChangeText.setTextColor(holder.itemView.context.resources.getColor(R.color.green))

        holder.binding.priceChangeText.text = "%${String.format("%.2f",displayList[position].price_change_percentage_24h)}"
        holder.binding.marketImageView.downloadImage(displayList[position].image, makePlaceHolder(holder.itemView.context))
        holder.itemView.setOnClickListener {
            val action = MarketFragmentDirections.actionMarketFragmentToAssetDetailFragment(displayList[position].symbol,false)
            Navigation.findNavController(it).navigate(action)
        }
    }


}