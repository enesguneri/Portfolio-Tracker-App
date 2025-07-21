package com.ilk.portfoliotracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ilk.portfoliotracker.R
import com.ilk.portfoliotracker.databinding.MyassetRecyclerRowBinding
import com.ilk.portfoliotracker.model.CoinGeckoData
import com.ilk.portfoliotracker.model.MyAsset
import com.ilk.portfoliotracker.util.downloadImage
import com.ilk.portfoliotracker.util.makePlaceHolder
import com.ilk.portfoliotracker.view.PortfolioFragmentDirections
import androidx.navigation.findNavController

class MyAssetAdapter(private val assetList : ArrayList<MyAsset>, private val dataList : ArrayList<CoinGeckoData>) : RecyclerView.Adapter<MyAssetAdapter.AssetHolder>() {
    class AssetHolder(val binding : MyassetRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetHolder {
        val binding = MyassetRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AssetHolder(binding)
    }

    override fun getItemCount(): Int {
        return assetList.size
    }

    fun updateAssetList(newList : List<CoinGeckoData>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: AssetHolder, position: Int) {
        val coinID = dataList.find { it.name == assetList[position].name }?.market_cap_rank?.minus(
            1
        )

        if(coinID != null && assetList[position] != null) {
            try {
                holder.binding.assetID.text = dataList[coinID].name
                holder.binding.marketImageView.downloadImage(dataList[coinID].image,makePlaceHolder(holder.itemView.context))

                val currentValue = assetList[position].amount * dataList[coinID].current_price!!
                holder.binding.valueText.text = "Current value: $${String.format("%.2f", currentValue)}"
                holder.binding.amountText.text = "Amount: ${assetList[position].amount.toString()}"

                holder.binding.assetPriceText.text = "Price: ${formatPrice(dataList[coinID].current_price!!)}"

                val exactValue = assetList[position].amount * assetList[position].cost
                holder.binding.investedAmountText.text = "Invested value: $${String.format("%.2f", exactValue)}"


                holder.binding.pnlText.text = "PNL: $${String.format("%.2f", currentValue - exactValue)} (%${String.format("%.2f", (currentValue - exactValue) / exactValue * 100)})"
                if (currentValue > exactValue) {
                    holder.binding.pnlText.setTextColor(holder.itemView.context.resources.getColor(R.color.green))
                } else {
                    holder.binding.pnlText.setTextColor(holder.itemView.context.resources.getColor(R.color.red))
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            }

        }

        holder.itemView.setOnClickListener {
            if(dataList[coinID!!].name != null) {
                val action = PortfolioFragmentDirections.actionPortfolioFragmentToMyAssetFragment(dataList[coinID].name!!)
                it.findNavController().navigate(action)
            }
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