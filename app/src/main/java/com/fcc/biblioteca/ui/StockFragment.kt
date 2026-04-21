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
    private var editingLibroId: Int? = null

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
            resetForm()
            binding.containerAddForm.visibility = if (binding.containerAddForm.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        
        binding.btnCloseForm.setOnClickListener {
            binding.containerAddForm.visibility = View.GONE
            resetForm()
        }
        
        binding.btnSaveBook.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val author = binding.etAuthor.text.toString().trim()
            val isbn = binding.etIsbn.text.toString().trim()
            val category = binding.etCategory.text.toString().trim()
            val sinopsis = binding.etSinopsis.text.toString().trim()
            val stockStr = binding.etStock.text.toString().trim()
            val stock = if (stockStr.isNotEmpty()) stockStr.toInt() else 0
            
            // Validaciones Robustas
            if (title.isEmpty()) {
                binding.etTitle.error = "Ingresa un título"
                return@setOnClickListener
            }
            if (isbn.isEmpty() || (isbn.length != 10 && isbn.length != 13 && isbn.length != 3)) { // 3 for mock compatibility
                binding.etIsbn.error = "ISBN inválido (3, 10 o 13 dígitos)"
                return@setOnClickListener
            }
            if (stock < 0) {
                binding.etStock.error = "El stock no puede ser negativo"
                return@setOnClickListener
            }

            val success = if (editingLibroId == null) {
                dbHandler.addLibro(title, author, isbn, category, stock, sinopsis)
            } else {
                dbHandler.updateLibro(editingLibroId!!, title, author, isbn, category, stock, sinopsis)
            }

            if (success) {
                resetForm()
                binding.containerAddForm.visibility = View.GONE
                val msg = if (editingLibroId == null) "Libro agregado" else "Libro actualizado"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                loadBooks()
            } else {
                Toast.makeText(requireContext(), "Error al guardar (ISBN duplicado)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = StockAdapter(emptyList(), this::onUpdateStock, this::onDeleteBook, this::onEditBook)
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

    private fun onEditBook(libro: Libro) {
        editingLibroId = libro.id_libro
        binding.tvFormTitle.text = "Editar Libro"
        binding.btnSaveBook.text = "Actualizar Libro"
        
        binding.etTitle.setText(libro.titulo)
        binding.etAuthor.setText(libro.autor)
        binding.etIsbn.setText(libro.isbn)
        binding.etCategory.setText(libro.categoria, false)
        binding.etSinopsis.setText(libro.sinopsis)
        binding.etStock.setText(libro.stock.toString())
        
        binding.containerAddForm.visibility = View.VISIBLE
        binding.etTitle.requestFocus()
    }

    private fun resetForm() {
        editingLibroId = null
        binding.tvFormTitle.text = "Nuevo Libro"
        binding.btnSaveBook.text = "Guardar Libro"
        
        binding.etTitle.text?.clear()
        binding.etAuthor.text?.clear()
        binding.etIsbn.text?.clear()
        binding.etCategory.text.clear()
        binding.etSinopsis.text?.clear()
        binding.etStock.text?.clear()
        
        binding.etTitle.error = null
        binding.etIsbn.error = null
        binding.etStock.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
