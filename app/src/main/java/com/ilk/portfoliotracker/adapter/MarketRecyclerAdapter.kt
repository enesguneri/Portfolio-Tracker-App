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

class MarketRecyclerAdapter(val assetList : ArrayList<CoinGeckoData>) : RecyclerView.Adapter<MarketRecyclerAdapter.MarketViewHolder>() {

    class MarketViewHolder(val binding : MarketRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketViewHolder {
        val binding = MarketRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MarketViewHolder(binding)
    }

    fun updateAssetList(newList : List<CoinGeckoData>) {
        assetList.clear()
        assetList.addAll(newList)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return assetList.size
    }

    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        holder.binding.assetID.text = assetList[position].name!!
        holder.binding.priceAnd24HChangeText.text = "${assetList[position].current_price!!}"
        if(assetList[position].price_change_percentage_24h!! < 0)
            holder.binding.priceChangeText.setTextColor(holder.itemView.context.resources.getColor(R.color.red))
        else
            holder.binding.priceChangeText.setTextColor(holder.itemView.context.resources.getColor(R.color.green))

        holder.binding.priceChangeText.text = "%${String.format("%.2f",assetList[position].price_change_percentage_24h!!)}"
        holder.binding.marketImageView.downloadImage(assetList[position].image, makePlaceHolder(holder.itemView.context))
        holder.itemView.setOnClickListener {
            val action = MarketFragmentDirections.actionMarketFragmentToAssetDetailFragment(position)
            Navigation.findNavController(it).navigate(action)
        }
    }


}