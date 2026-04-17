package com.fcc.biblioteca.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fcc.biblioteca.LoginActivity
import com.fcc.biblioteca.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val prefs = requireActivity().getSharedPreferences("BibliotecaPrefs", Context.MODE_PRIVATE)
        val name = prefs.getString("userName", "Usuario desconocido")
        val role = prefs.getString("userRole", "usuario")
        
        binding.tvUserName.text = name ?: "Usuario desconocido"
        binding.tvUserRole.text = role?.uppercase() ?: "USUARIO"
        
        // Simulate email
        val baseName = name?.split(" ")?.firstOrNull()?.lowercase() ?: "usuario"
        binding.tvUserEmail.text = "$baseName@biblioteca.com"
        
        val userId = prefs.getInt("userId", -1)
        if (userId != -1) {
            val dbHandler = com.fcc.biblioteca.db.MyDBHandler(requireContext())
            
            // Personal reserves count (for everyone)
            val personalCount = dbHandler.getContadorPrestamos(userId)
            binding.tvActiveReserves.text = personalCount.toString()
            
            // Global loan count (Admin only)
            if (role == "admin") {
                binding.containerBooksLoanedStat.visibility = View.VISIBLE
                val globalCount = dbHandler.getTotalContadorPrestamos()
                binding.tvBooksLoaned.text = globalCount.toString()
            } else {
                binding.containerBooksLoanedStat.visibility = View.GONE
            }
        }


        binding.btnLogout.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión y salir de la cuenta de biblioteca?")
                .setPositiveButton("Salir") { _, _ ->
                    prefs.edit().clear().apply()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
