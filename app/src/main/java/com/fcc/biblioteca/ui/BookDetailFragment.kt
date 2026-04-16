package com.fcc.biblioteca.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fcc.biblioteca.databinding.FragmentBookDetailBinding
import com.fcc.biblioteca.db.MyDBHandler
import com.fcc.biblioteca.model.Libro

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHandler: MyDBHandler
    private var libro: Libro? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHandler = MyDBHandler(requireContext())
        libro = arguments?.getSerializable("libro") as? Libro
        
        libro?.let {
            binding.tvDetailTitle.text = it.titulo
            binding.tvDetailAuthor.text = it.autor ?: "Autor Desconocido"
            binding.tvDetailCategory.text = it.categoria ?: "General"
            binding.tvDetailSinopsis.text = it.sinopsis ?: "Sin descripción disponible."
            binding.tvDetailIsbn.text = "ISBN: ${it.isbn ?: "N/A"}"
            binding.tvDetailStock.text = "Stock disponible: ${it.stock}"
            
            if (it.stock > 0) {
                binding.btnReserveBook.isEnabled = true
                binding.btnReserveBook.text = "Solicitar Préstamo"
            } else {
                binding.btnReserveBook.isEnabled = false
                binding.btnReserveBook.text = "Sin stock"
            }
        }
        
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.executePendingTransactions()
        }
        
        binding.btnReserveBook.setOnClickListener {
            val prefs = requireActivity().getSharedPreferences("BibliotecaPrefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("userId", -1)
            
            if (userId != -1 && libro != null) {
                val success = dbHandler.prestarLibro(userId, libro!!.id_libro)
                if (success) {
                    Toast.makeText(requireContext(), "Préstamo registrado exitosamente", Toast.LENGTH_SHORT).show()
                    libro!!.stock -= 1
                    binding.tvDetailStock.text = "Stock disponible: ${libro!!.stock}"
                    if (libro!!.stock <= 0) {
                        binding.btnReserveBook.isEnabled = false
                        binding.btnReserveBook.text = "Sin stock"
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al registrar el préstamo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Error de sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
