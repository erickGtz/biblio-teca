package com.fcc.biblioteca.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fcc.biblioteca.databinding.ItemBookCatalogBinding
import com.fcc.biblioteca.model.Libro

class BookAdapter(
    private var libros: List<Libro>,
    private val onItemClick: (Libro) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    fun updateList(newList: List<Libro>) {
        libros = newList
        notifyDataSetChanged()
    }

    inner class BookViewHolder(val binding: ItemBookCatalogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(libro: Libro) {
            binding.tvBookTitle.text = libro.titulo
            binding.tvBookAuthor.text = libro.autor ?: "Desconocido"
            binding.tvCategoryBadge.text = libro.categoria ?: "General"
            
            if (libro.estado == "disponible") {
                binding.tvStockStatus.text = "Disponible"
                binding.tvStockStatus.setTextColor(android.graphics.Color.parseColor("#16A34A")) // Green
            } else {
                binding.tvStockStatus.text = "No disponible"
                binding.tvStockStatus.setTextColor(android.graphics.Color.parseColor("#EA580C")) // Orange
            }
            
            binding.btnDetails.setOnClickListener { onItemClick(libro) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookCatalogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(libros[position])
    }

    override fun getItemCount(): Int = libros.size
}
