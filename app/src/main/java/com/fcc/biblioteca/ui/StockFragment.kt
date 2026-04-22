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

        binding.btnSelectImage.setOnClickListener {
            // En una app real usaríamos ActivityResultLauncher para abrir la galería.
            // Para este prototipo, simulamos la selección exitosa.
            Toast.makeText(requireContext(), "Selector de galería abierto...", Toast.LENGTH_SHORT).show()
            binding.ivFormPreview.setImageResource(android.R.drawable.ic_menu_gallery)
            binding.ivFormPreview.tag = "new_image" // Marcador para saber que se cambió
        }
        
        binding.btnSaveBook.setOnClickListener {
            if (validarFormulario()) {
                if (editingLibroId != null) {
                    // WARNING al actualizar
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Confirmar Cambios")
                        .setMessage("¿Estás seguro de que deseas actualizar la información de este libro? Esta acción no se puede deshacer.")
                        .setPositiveButton("Actualizar") { _, _ -> ejecutarGuardado() }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    ejecutarGuardado()
                }
            }
        }
    }

    private fun validarFormulario(): Boolean {
        var esValido = true
        val title = binding.etTitle.text.toString().trim()
        val isbn = binding.etIsbn.text.toString().trim()
        val stockStr = binding.etStock.text.toString().trim()

        // Limpiar errores previos
        binding.tilTitle.error = null
        binding.tilIsbn.error = null
        binding.tilStock.error = null

        if (title.isEmpty()) {
            binding.tilTitle.error = "El título es obligatorio"
            esValido = false
        }
        if (isbn.isEmpty() || (isbn.length != 10 && isbn.length != 13 && isbn.length != 3)) {
            binding.tilIsbn.error = "ISBN inválido (3, 10 o 13 dígitos)"
            esValido = false
        }
        if (stockStr.isEmpty()) {
            binding.tilStock.error = "Ingresa el stock inicial"
            esValido = false
        } else if (stockStr.toInt() < 0) {
            binding.tilStock.error = "El stock no puede ser negativo"
            esValido = false
        }

        if (!esValido) {
            Toast.makeText(requireContext(), "Por favor corrige los errores en rojo", Toast.LENGTH_SHORT).show()
        }
        return esValido
    }

    private fun ejecutarGuardado() {
        val title = binding.etTitle.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()
        val isbn = binding.etIsbn.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()
        val sinopsis = binding.etSinopsis.text.toString().trim()
        val stock = binding.etStock.text.toString().toInt()

        val success = if (editingLibroId == null) {
            dbHandler.addLibro(title, author, isbn, category, stock, sinopsis)
        } else {
            dbHandler.updateLibro(editingLibroId!!, title, author, isbn, category, stock, sinopsis)
        }

        if (success) {
            resetForm()
            binding.containerAddForm.visibility = View.GONE
            val msg = if (editingLibroId == null) "¡Libro guardado exitosamente!" else "Información actualizada correctamente"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            loadBooks()
        } else {
            binding.tilIsbn.error = "Este ISBN ya existe en el sistema"
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

        // Cargar imagen actual
        val ctx = requireContext()
        val resId = ctx.resources.getIdentifier(libro.imagen ?: "bg_book_cover", "drawable", ctx.packageName)
        if (resId != 0) binding.ivFormPreview.setImageResource(resId)
        else binding.ivFormPreview.setImageResource(com.fcc.biblioteca.R.drawable.bg_book_cover)
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
        
        binding.tilTitle.error = null
        binding.tilIsbn.error = null
        binding.tilStock.error = null
        binding.ivFormPreview.setImageResource(com.fcc.biblioteca.R.drawable.bg_book_cover)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
