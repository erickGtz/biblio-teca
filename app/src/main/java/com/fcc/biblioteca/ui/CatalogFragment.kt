package com.fcc.biblioteca.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.biblioteca.R
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
    private var categoryFilter = "Todos"
    
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
        
        setupDynamicChips()
        
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterList() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    
    private fun setupDynamicChips() {
        binding.chipGroupCategories.removeAllViews()
        val categories = mutableListOf("Todos")
        categories.addAll(dbHandler.getUniqueCategories())
        
        for (category in categories) {
            val chip = layoutInflater.inflate(R.layout.item_chip_category, binding.chipGroupCategories, false) as Chip
            chip.id = View.generateViewId()
            chip.text = category
            chip.isChecked = category == "Todos"
            binding.chipGroupCategories.addView(chip)
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                val text = chip?.text?.toString()
                if (text != null) {
                    categoryFilter = text
                    filterList()
                }
            }
        }
    }
    
    private fun filterList() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        
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
