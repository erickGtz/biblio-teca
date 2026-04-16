package com.fcc.biblioteca.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.biblioteca.databinding.FragmentStockBinding
import com.fcc.biblioteca.db.MyDBHandler
import com.fcc.biblioteca.model.Libro

class StockFragment : Fragment() {

    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dbHandler: MyDBHandler
    private lateinit var adapter: StockAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHandler = MyDBHandler(requireContext())
        setupRecyclerView()
        
        // Setup categories autocomplete
        val categories = dbHandler.getUniqueCategories()
        val catAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.etCategory.setAdapter(catAdapter)
        
        binding.btnAddNew.setOnClickListener {
            binding.containerAddForm.visibility = if (binding.containerAddForm.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        
        binding.btnCloseForm.setOnClickListener {
            binding.containerAddForm.visibility = View.GONE
        }
        
        binding.btnSaveBook.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val author = binding.etAuthor.text.toString().trim()
            val isbn = binding.etIsbn.text.toString().trim()
            val category = binding.etCategory.text.toString().trim()
            val stockStr = binding.etStock.text.toString().trim()
            val stock = if (stockStr.isNotEmpty()) stockStr.toInt() else 0
            
            if (title.isNotEmpty() && isbn.isNotEmpty()) {
                val success = dbHandler.addLibro(title, author, isbn, category, stock)
                if (success) {
                    binding.etTitle.text?.clear()
                    binding.etAuthor.text?.clear()
                    binding.etIsbn.text?.clear()
                    binding.etCategory.text.clear()
                    binding.etStock.text?.clear()
                    binding.containerAddForm.visibility = View.GONE
                    Toast.makeText(requireContext(), "Libro agregado", Toast.LENGTH_SHORT).show()
                    loadBooks()
                } else {
                    Toast.makeText(requireContext(), "Error al agregar (ISBN duplicado)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Título e ISBN obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = StockAdapter(emptyList(), this::onUpdateStock, this::onDeleteBook)
        binding.recyclerStock.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStock.adapter = adapter
        loadBooks()
    }

    private fun loadBooks() {
        val list = dbHandler.getLibros()
        adapter.updateList(list)
    }

    private fun onUpdateStock(id: Int, change: Int) {
        dbHandler.updateLibroStock(id, change)
        loadBooks()
    }

    private fun onDeleteBook(id: Int) {
        dbHandler.deleteLibro(id)
        loadBooks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
