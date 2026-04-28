package com.fcc.biblioteca.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fcc.biblioteca.R
import com.fcc.biblioteca.db.MyDBHandler
import com.fcc.biblioteca.model.LibroConPrestamos

class AdminLoansFragment : Fragment() {

    private lateinit var rvAdminLoans: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: AdminBooksWithLoansAdapter
    private lateinit var dbHandler: MyDBHandler
    private var librosConPrestamos: List<LibroConPrestamos> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_loans, container, false)

        rvAdminLoans = view.findViewById(R.id.rvAdminLoans)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        
        dbHandler = MyDBHandler(requireContext())

        rvAdminLoans.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = AdminBooksWithLoansAdapter(emptyList()) { item ->
            if (item.prestamosActivos > 0) {
                val bottomSheet = AdminBookLoansBottomSheet(item.libro.id_libro, item.libro.titulo) {
                    cargarDatos() // Recargar datos cuando haya un cambio (ej. devolución)
                }
                bottomSheet.show(parentFragmentManager, "AdminBookLoansBottomSheet")
            }
        }
        rvAdminLoans.adapter = adapter

        cargarDatos()

        return view
    }

    private fun cargarDatos() {
        librosConPrestamos = dbHandler.getLibrosWithPrestamosCount()
        if (librosConPrestamos.isEmpty()) {
            rvAdminLoans.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = "No hay libros en la biblioteca"
        } else {
            rvAdminLoans.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
            adapter.updateData(librosConPrestamos)
        }
    }
}
