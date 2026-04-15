package com.fcc.biblioteca.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.biblioteca.databinding.FragmentCatalogBinding
import com.fcc.biblioteca.db.MyDBHandler
import com.fcc.biblioteca.model.Libro
import com.google.android.material.chip.Chip

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dbHandler: MyDBHandler
    private lateinit var adapter: BookAdapter
    private var allBooks: List<Libro> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHandler = MyDBHandler(requireContext())
        dbHandler.insertMockLibrosIfEmpty()
        
        allBooks = dbHandler.getLibros()
        
        adapter = BookAdapter(allBooks)
        binding.recyclerBooks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBooks.adapter = adapter
        
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterList() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            filterList()
        }
    }
    
    private fun filterList() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val checkedChipId = binding.chipGroupCategories.checkedChipId
        var categoryFilter = "Todos"
        
        when (checkedChipId) {
            binding.chipTodos.id -> categoryFilter = "Todos"
            binding.chipFiccion.id -> categoryFilter = "Ficción"
            binding.chipHistoria.id -> categoryFilter = "Historia"
            binding.chipMisterio.id -> categoryFilter = "Misterio"
        }
        
        val filtered = allBooks.filter {
            val matchesSearch = it.titulo.lowercase().contains(query) || 
                               (it.autor?.lowercase()?.contains(query) ?: false)
            val matchesCategory = categoryFilter == "Todos" || 
                                 it.categoria.equals(categoryFilter, ignoreCase = true)
            matchesSearch && matchesCategory
        }
        
        adapter.updateList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
