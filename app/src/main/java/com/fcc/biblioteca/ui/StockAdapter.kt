package com.fcc.biblioteca.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fcc.biblioteca.databinding.ItemBookStockBinding
import com.fcc.biblioteca.model.Libro

class StockAdapter(
    private var libros: List<Libro>,
    private val onUpdateStock: (Int, Int) -> Unit,
    private val onDeleteBook: (Int) -> Unit
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    fun updateList(newList: List<Libro>) {
        libros = newList
        notifyDataSetChanged()
    }

    inner class StockViewHolder(val binding: ItemBookStockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(libro: Libro) {
            binding.tvBookTitle.text = libro.titulo
            binding.tvAuthor.text = libro.autor ?: "Sin autor"
            binding.tvIsbn.text = "ISBN: ${libro.isbn ?: "N/A"}"
            binding.tvCategory.text = libro.categoria ?: "General"
            binding.tvStockCount.text = libro.stock.toString()

            binding.btnPlus.setOnClickListener {
                onUpdateStock(libro.id_libro, 1)
            }
            binding.btnMinus.setOnClickListener {
                if (libro.stock > 0) {
                    onUpdateStock(libro.id_libro, -1)
                }
            }
            binding.btnDelete.setOnClickListener {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(binding.root.context)
                    .setTitle("Eliminar Libro")
                    .setMessage("¿Estás de acuerdo en eliminar el libro '${libro.titulo}' del catálogo?")
                    .setPositiveButton("Eliminar") { _, _ -> onDeleteBook(libro.id_libro) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemBookStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(libros[position])
    }

    override fun getItemCount(): Int = libros.size
}
