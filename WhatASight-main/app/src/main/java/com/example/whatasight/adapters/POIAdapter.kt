package com.example.whatasight.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatasight.databinding.CardPoiBinding
import com.example.whatasight.models.POIModel
import com.squareup.picasso.Picasso


interface POIListener {
    fun onPOIClick(poi: POIModel)
}

class POIAdapter constructor(private var pois: List<POIModel>,
                                   private val listener: POIListener) :
        RecyclerView.Adapter<POIAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardPoiBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val poi = pois[holder.adapterPosition]
        holder.bind(poi, listener)
    }

    override fun getItemCount(): Int = pois.size

    class MainHolder(private val binding : CardPoiBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(poi: POIModel, listener: POIListener) {
            binding.poiTitle.text = poi.title
            binding.description.text = poi.description
            binding.rating.rating = poi.rating
            Picasso.get().load(poi.image).resize(100,100).into(binding.imageIcon)
            binding.root.setOnClickListener { listener.onPOIClick(poi) }
        }
    }
}
