package com.ilk.portfoliotracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.ilk.portfoliotracker.R
import com.ilk.portfoliotracker.databinding.MarketRecyclerRowBinding
import com.ilk.portfoliotracker.model.CoinGeckoData
import com.ilk.portfoliotracker.util.downloadImage
import com.ilk.portfoliotracker.util.makePlaceHolder
import com.ilk.portfoliotracker.view.MarketFragmentDirections
import androidx.navigation.findNavController

class FavoritesRecyclerAdapter(val favoritesList : ArrayList<CoinGeckoData>) : RecyclerView.Adapter<FavoritesRecyclerAdapter.FavoritesViewHolder>() {
    class FavoritesViewHolder(val binding: MarketRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val binding = MarketRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FavoritesViewHolder(binding)
    }

    fun updateFavoritesList(newList : List<CoinGeckoData>) {
        favoritesList.clear()
        favoritesList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        holder.binding.assetID.text = favoritesList[position].name!!
        holder.binding.priceAnd24HChangeText.text = "${favoritesList[position].current_price!!}"
        if(favoritesList[position].price_change_percentage_24h!! < 0)
            holder.binding.priceChangeText.setTextColor(holder.itemView.context.resources.getColor(R.color.red))
        else
            holder.binding.priceChangeText.setTextColor(holder.itemView.context.resources.getColor(R.color.green))

        holder.binding.priceChangeText.text = "%${String.format("%.2f",favoritesList[position].price_change_percentage_24h!!)}"
        holder.binding.marketImageView.downloadImage(favoritesList[position].image, makePlaceHolder(holder.itemView.context))
        holder.itemView.setOnClickListener {
            val action = MarketFragmentDirections.actionMarketFragmentToAssetDetailFragment(favoritesList[position].symbol,true)
            Navigation.findNavController(it).navigate(action)
        }
    }
}