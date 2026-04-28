package com.fcc.biblioteca.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fcc.biblioteca.R
import com.fcc.biblioteca.db.MyDBHandler
import com.fcc.biblioteca.model.Prestamo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminBookLoansBottomSheet(
    private val idLibro: Int,
    private val tituloLibro: String,
    private val onLoansChanged: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var rvBookLoans: RecyclerView
    private lateinit var tvSheetTitle: TextView
    private lateinit var adapter: AdminLoansAdapter
    private lateinit var dbHandler: MyDBHandler
    private var prestamos: List<Prestamo> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_admin_book_loans, container, false)
        
        rvBookLoans = view.findViewById(R.id.rvBookLoans)
        tvSheetTitle = view.findViewById(R.id.tvSheetTitle)
        
        tvSheetTitle.text = "Préstamos: $tituloLibro"
        
        dbHandler = MyDBHandler(requireContext())
        
        rvBookLoans.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = AdminLoansAdapter(emptyList()) { prestamo ->
            mostrarOpcionesPrestamo(prestamo)
        }
        rvBookLoans.adapter = adapter
        
        cargarPrestamos()

        return view
    }

    private fun cargarPrestamos() {
        prestamos = dbHandler.getPrestamosPorLibro(idLibro)
        adapter.updateData(prestamos)
        if (prestamos.isEmpty()) {
            dismiss() // Cierra el bottom sheet si ya no hay préstamos
        }
    }

    private fun mostrarOpcionesPrestamo(prestamo: Prestamo) {
        val options = arrayOf("Marcar como devuelto", "Extender 3 días", "Extender 2 semanas", "Enviar recordatorio SMS", "Cancelar")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Opciones de Préstamo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> devolverPrestamo(prestamo)
                    1 -> extenderPrestamo(prestamo, 3)
                    2 -> extenderPrestamo(prestamo, 14)
                    3 -> {
                        Toast.makeText(requireContext(), "Esta funcionalidad usa el recordatorio por SMS", Toast.LENGTH_SHORT).show()
                    }
                    4 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun extenderPrestamo(prestamo: Prestamo, dias: Int) {
        val exito = dbHandler.extendPrestamo(prestamo.id_prestamo, dias)
        if (exito) {
            Toast.makeText(requireContext(), "Préstamo extendido por $dias días", Toast.LENGTH_SHORT).show()
            cargarPrestamos()
        } else {
            Toast.makeText(requireContext(), "Error al extender el préstamo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun devolverPrestamo(prestamo: Prestamo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar devolución")
            .setMessage("¿Estás seguro que deseas marcar este libro como devuelto por el usuario ${prestamo.usuarioNombre}?")
            .setPositiveButton("Devolver") { _, _ ->
                dbHandler.devolverLibro(prestamo.id_prestamo, prestamo.libro.id_libro)
                Toast.makeText(requireContext(), "Libro devuelto con éxito", Toast.LENGTH_SHORT).show()
                onLoansChanged()
                cargarPrestamos()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
