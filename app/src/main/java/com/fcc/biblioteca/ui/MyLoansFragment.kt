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
        
        val prefs = requireActivity().getSharedPreferences("BibliotecaPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)
        
        val loans = if (userId != -1) dbHandler.getPrestamosPorUsuario(userId) else emptyList()
        
        adapter = MyLoansAdapter(loans) { prestamo ->
            // SIMULATING PDF OPENING
            Toast.makeText(requireContext(), "Abriendo PDF de '${prestamo.libro.titulo}'...", Toast.LENGTH_LONG).show()
        }
        
        binding.recyclerLoans.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLoans.adapter = adapter
        
        if (loans.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.tvEmptyState.visibility = View.GONE
        }
        
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
