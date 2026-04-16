package com.fcc.biblioteca.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fcc.biblioteca.databinding.ItemMyLoanBinding
import com.fcc.biblioteca.model.Prestamo

class MyLoansAdapter(
    private var prestamos: List<Prestamo>,
    private val onPdfClick: (Prestamo) -> Unit,
    private val onReturnClick: (Prestamo) -> Unit
) : RecyclerView.Adapter<MyLoansAdapter.ViewHolder>() {

    fun updateList(newList: List<Prestamo>) {
        prestamos = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMyLoanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(prestamo: Prestamo) {
            binding.tvBookTitle.text = prestamo.libro.titulo
            binding.tvLoanDates.text = "Inicio: ${prestamo.fechaInicio}\nVencimiento: ${prestamo.fechaFin}"
            
            val ctx = binding.root.context
            val resId = ctx.resources.getIdentifier(prestamo.libro.imagen ?: "bg_book_cover", "drawable", ctx.packageName)
            if (resId != 0 && prestamo.libro.imagen != null) {
                binding.ivCover.setImageResource(resId)
            } else {
                binding.ivCover.setImageResource(com.fcc.biblioteca.R.drawable.bg_book_cover)
            }
            
            binding.btnReadPdf.setOnClickListener {
                onPdfClick(prestamo)
            }
            binding.btnReturn.setOnClickListener {
                onReturnClick(prestamo)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyLoanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(prestamos[position])
    }

    override fun getItemCount(): Int = prestamos.size
}
