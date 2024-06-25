package com.uct.listadecompras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ComprasAdapter : ListAdapter<Compras, ComprasAdapter.ComprasViewHolder>(ComprasDiffCallback()) {

    inner class ComprasViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val name = item.findViewById<TextView>(R.id.viewName)

        fun bind(compra: Compras) {
            name.text = compra.nombre
        }
    }

    class ComprasDiffCallback : DiffUtil.ItemCallback<Compras>() {
        override fun areItemsTheSame(oldItem: Compras, newItem: Compras): Boolean {
            return oldItem.nombre == newItem.nombre
        }

        override fun areContentsTheSame(oldItem: Compras, newItem: Compras): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComprasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.compras_list, parent, false)
        return ComprasViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComprasViewHolder, position: Int) {
        val producto = getItem(position)
        holder.bind(producto)
        }
}