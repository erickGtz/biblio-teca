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
            binding.tvDetailCategory.text = libro!!.categoria ?: "Sin categoría"
            binding.tvDetailSinopsis.text = it.sinopsis ?: "Sin descripción disponible."
            binding.tvDetailIsbn.text = "ISBN: ${libro!!.isbn}"
            binding.tvDetailStock.text = "Stock disponible: ${libro!!.stock}"
            
            val ctx = requireContext()
            val resId = ctx.resources.getIdentifier(libro!!.imagen ?: "bg_book_cover", "drawable", ctx.packageName)
            if (resId != 0 && libro!!.imagen != null) {
                binding.ivCover.setImageResource(resId)
            } else {
                binding.ivCover.setImageResource(com.fcc.biblioteca.R.drawable.bg_book_cover)
            }
            
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
                val status = dbHandler.prestarLibro(userId, libro!!.id_libro)
                if (status == 1) {
                    Toast.makeText(requireContext(), "Préstamo registrado exitosamente", Toast.LENGTH_SHORT).show()
                    libro!!.stock -= 1
                    binding.tvDetailStock.text = "Stock disponible: ${libro!!.stock}"
                    if (libro!!.stock <= 0) {
                        binding.btnReserveBook.isEnabled = false
                        binding.btnReserveBook.text = "Sin stock"
                    }
                } else if (status == 0) {
                    Toast.makeText(requireContext(), "Límite: Ya tienes un ejemplar de este libro reservado", Toast.LENGTH_LONG).show()
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
