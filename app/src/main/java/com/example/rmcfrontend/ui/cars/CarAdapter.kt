package com.example.rmcfrontend.ui.cars

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rmcfrontend.R
import com.example.rmcfrontend.api.models.Car
import com.bumptech.glide.Glide

class CarAdapter : ListAdapter<Car, CarAdapter.CarViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Car>() {
            override fun areItemsTheSame(old: Car, new: Car) = old.id == new.id
            override fun areContentsTheSame(old: Car, new: Car) = old == new
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val image: ImageView = itemView.findViewById(R.id.item_car_image)
        private val title: TextView = itemView.findViewById(R.id.item_car_title)
        private val subtitle: TextView = itemView.findViewById(R.id.item_car_subtitle)
        private val price: TextView = itemView.findViewById(R.id.item_car_price)

        fun bind(car: Car) {
            title.text = "${car.make.orEmpty()} ${car.model.orEmpty()}".trim()
            subtitle.text = "Year: ${car.modelYear ?: "-"} • ${car.color ?: "-"}"

            price.text = car.price?.let { "€ %.2f".format(it) } ?: ""

            if (car.imageFileNames.isNotEmpty()) {
                val url = "http://10.0.2.2:8080/images/${car.imageFileNames[0]}"

                Glide.with(itemView)
                    .load(url)
                    .centerCrop()
                    .into(image)
            } else {
                image.setImageResource(R.drawable.car)
            }
        }
    }
}
