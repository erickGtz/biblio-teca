package com.fcc.biblioteca.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.biblioteca.databinding.FragmentMyLoansBinding
import com.fcc.biblioteca.db.MyDBHandler

class MyLoansFragment : Fragment() {

    private var _binding: FragmentMyLoansBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHandler: MyDBHandler
    private lateinit var adapter: MyLoansAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLoansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHandler = MyDBHandler(requireContext())
        dbHandler.procesarDevolucionesExpiradas()
        
        val prefs = requireActivity().getSharedPreferences("BibliotecaPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)
        
        var loans = if (userId != -1) dbHandler.getPrestamosPorUsuario(userId) else emptyList()
        
        adapter = MyLoansAdapter(loans, { prestamo ->
            // SIMULATING PDF OPENING
            Toast.makeText(requireContext(), "Abriendo PDF de '${prestamo.libro.titulo}'...", Toast.LENGTH_LONG).show()
        }, { prestamo ->
            // VALIDACIÓN: No devolver el mismo día
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            if (prestamo.fechaInicio == today) {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Devolución no permitida")
                    .setMessage("Para asegurar un uso responsable, no se permiten devoluciones el mismo día del préstamo. Por favor, intenta de nuevo mañana.")
                    .setPositiveButton("Entendido", null)
                    .show()
                return@MyLoansAdapter
            }

            dbHandler.devolverLibro(prestamo.id_prestamo, prestamo.libro.id_libro)
            Toast.makeText(requireContext(), "Libro devuelto con éxito", Toast.LENGTH_SHORT).show()
            loans = dbHandler.getPrestamosPorUsuario(userId)
            adapter.updateList(loans)
            if (loans.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
            }
        })
        
        binding.recyclerLoans.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLoans.adapter = adapter
        
        if (loans.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cada vez que el fragmento es visible
        val prefs = requireActivity().getSharedPreferences("BibliotecaPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)
        if (userId != -1 && ::dbHandler.isInitialized) {
            val updatedLoans = dbHandler.getPrestamosPorUsuario(userId)
            if (::adapter.isInitialized) {
                adapter.updateList(updatedLoans)
                binding.tvEmptyState.visibility = if (updatedLoans.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
