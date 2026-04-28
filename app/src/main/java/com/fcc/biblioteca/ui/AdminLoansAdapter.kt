package com.fcc.biblioteca.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fcc.biblioteca.R
import com.fcc.biblioteca.model.Prestamo

class AdminLoansAdapter(
    private var prestamos: List<Prestamo>,
    private val onItemClick: (Prestamo) -> Unit
) : RecyclerView.Adapter<AdminLoansAdapter.AdminLoanViewHolder>() {

    class AdminLoanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBookTitle: TextView = view.findViewById(R.id.tvBookTitle)
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        val tvUserInfo: TextView = view.findViewById(R.id.tvUserInfo)
        val tvLoanDates: TextView = view.findViewById(R.id.tvLoanDates)
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminLoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_loan, parent, false)
        return AdminLoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminLoanViewHolder, position: Int) {
        val prestamo = prestamos[position]
        val libro = prestamo.libro

        holder.tvBookTitle.text = libro.titulo
        holder.tvAuthor.text = libro.autor
        
        holder.tvUserInfo.text = "Usuario: ${prestamo.usuarioNombre} (ID: ${prestamo.idUsuario})"
        holder.tvLoanDates.text = "Desde: ${prestamo.fechaInicio}\nHasta: ${prestamo.fechaFin}"

        val ctx = holder.itemView.context
        val resId = ctx.resources.getIdentifier(libro.imagen, "drawable", ctx.packageName)
        if (resId != 0) {
            holder.ivCover.setImageResource(resId)
        } else {
            holder.ivCover.setImageResource(R.drawable.bg_book_cover)
        }

        holder.itemView.setOnClickListener {
            onItemClick(prestamo)
        }
    }

    override fun getItemCount() = prestamos.size

    fun updateData(newPrestamos: List<Prestamo>) {
        prestamos = newPrestamos
        notifyDataSetChanged()
    }
}
