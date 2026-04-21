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
    private var sortType = "A-Z"
    
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
        
        adapter = BookAdapter(allBooks) { libro ->
            val bundle = Bundle().apply {
                putSerializable("libro", libro)
            }
            val detailFragment = BookDetailFragment().apply {
                arguments = bundle
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }
        binding.recyclerBooks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBooks.adapter = adapter
        
        binding.btnFilterCategory.setOnClickListener { showCategoryMenu() }
        binding.btnSort.setOnClickListener { showSortMenu() }
        
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterAndSortList() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        filterAndSortList()
    }
    
    private fun showCategoryMenu() {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), binding.btnFilterCategory)
        val categories = mutableListOf("Todos")
        categories.addAll(dbHandler.getUniqueCategories())
        
        categories.forEachIndexed { index, category ->
            popup.menu.add(0, index, index, category)
        }
        
        popup.setOnMenuItemClickListener { item ->
            categoryFilter = categories[item.itemId]
            binding.btnFilterCategory.text = "Categoría: $categoryFilter"
            filterAndSortList()
            true
        }
        popup.show()
    }
    
    private fun showSortMenu() {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), binding.btnSort)
        popup.menu.add(0, 0, 0, "Título (A-Z)")
        popup.menu.add(0, 1, 1, "Título (Z-A)")
        popup.menu.add(0, 2, 2, "Autor")
        
        popup.setOnMenuItemClickListener { item ->
            sortType = when (item.itemId) {
                0 -> "A-Z"
                1 -> "Z-A"
                2 -> "Autor"
                else -> "A-Z"
            }
            filterAndSortList()
            true
        }
        popup.show()
    }
    
    private fun filterAndSortList() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        
        var filtered = allBooks.filter {
            val matchesSearch = it.titulo.lowercase().contains(query) || 
                               (it.autor?.lowercase()?.contains(query) ?: false)
            val matchesCategory = categoryFilter == "Todos" || 
                                 it.categoria.equals(categoryFilter, ignoreCase = true)
            matchesSearch && matchesCategory
        }
        
        filtered = when (sortType) {
            "A-Z" -> filtered.sortedBy { it.titulo }
            "Z-A" -> filtered.sortedByDescending { it.titulo }
            "Autor" -> filtered.sortedBy { it.autor ?: "" }
            else -> filtered
        }
        
        adapter.updateList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
