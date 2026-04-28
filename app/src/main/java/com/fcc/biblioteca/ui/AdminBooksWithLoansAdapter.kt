package com.fcc.biblioteca.ui

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.fcc.biblioteca.R
import com.fcc.biblioteca.model.LibroConPrestamos

class AdminBooksWithLoansAdapter(
    private var list: List<LibroConPrestamos>,
    private val onBookClick: (LibroConPrestamos) -> Unit
) : RecyclerView.Adapter<AdminBooksWithLoansAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardSummary: CardView = view.findViewById(R.id.cardSummary)
        val clSummaryContent: ConstraintLayout = view.findViewById(R.id.clSummaryContent)
        val ivBookCover: ImageView = view.findViewById(R.id.ivBookCover)
        val tvBookTitle: TextView = view.findViewById(R.id.tvBookTitle)
        val tvBookAuthor: TextView = view.findViewById(R.id.tvBookAuthor)
        val tvActiveLoansCount: TextView = view.findViewById(R.id.tvActiveLoansCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_book_loan_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val libro = item.libro
        
        holder.tvBookTitle.text = libro.titulo
        holder.tvBookAuthor.text = libro.autor

        // Load image
        val ctx = holder.itemView.context
        val imgStr = libro.imagen ?: "bg_book_cover"
        if (imgStr.startsWith("content://") || imgStr.startsWith("file://")) {
            holder.ivBookCover.setImageURI(Uri.parse(imgStr))
        } else {
            val resId = ctx.resources.getIdentifier(imgStr, "drawable", ctx.packageName)
            if (resId != 0) holder.ivBookCover.setImageResource(resId)
            else holder.ivBookCover.setImageResource(R.drawable.bg_book_cover)
        }

        if (item.prestamosActivos > 0) {
            holder.tvActiveLoansCount.text = "${item.prestamosActivos} Préstamos Activos"
            holder.tvActiveLoansCount.visibility = View.VISIBLE
            holder.clSummaryContent.alpha = 1.0f
            holder.cardSummary.setCardBackgroundColor(ctx.getColor(R.color.cardBg))
        } else {
            holder.tvActiveLoansCount.text = "0 Préstamos"
            holder.tvActiveLoansCount.visibility = View.VISIBLE
            holder.clSummaryContent.alpha = 0.5f
            holder.cardSummary.setCardBackgroundColor(Color.parseColor("#F5F5F5")) // Grayed out
        }

        holder.itemView.setOnClickListener {
            onBookClick(item)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<LibroConPrestamos>) {
        list = newList
        notifyDataSetChanged()
    }
}
