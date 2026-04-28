package com.fcc.biblioteca.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
    private var selectedImageName: String? = null

    // Launcher para seleccionar imagen de la galería
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Tomamos permiso persistente para que la imagen se vea después de reiniciar la app
            try {
                val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, flag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            selectedImageName = uri.toString()
            binding.ivFormPreview.setImageURI(uri)
            Toast.makeText(requireContext(), "Imagen seleccionada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var allBooks: List<Libro> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHandler = MyDBHandler(requireContext())
        setupRecyclerView()
        
        val categories = dbHandler.getUniqueCategories()
        val catAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.etCategory.setAdapter(catAdapter)
        
        binding.etSearchStock.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBooks(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.btnAddNew.setOnClickListener {
            resetForm()
            binding.containerAddForm.visibility = if (binding.containerAddForm.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        
        binding.btnCloseForm.setOnClickListener {
            binding.containerAddForm.visibility = View.GONE
            resetForm()
        }

        binding.btnSelectImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

    private fun filterBooks(query: String) {
        if (query.isEmpty()) {
            adapter.updateList(allBooks)
        } else {
            val q = query.lowercase()
            val filtered = allBooks.filter {
                it.titulo.lowercase().contains(q) || 
                it.autor?.lowercase()?.contains(q) == true || 
                it.isbn?.lowercase()?.contains(q) == true
            }
            adapter.updateList(filtered)
        }
    }

    private fun validarFormulario(): Boolean {
        var esValido = true
        val title = binding.etTitle.text.toString().trim()
        val isbn = binding.etIsbn.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()
        val stockStr = binding.etStock.text.toString().trim()

        // Limpiar errores previos
        binding.tilTitle.error = null
        binding.tilAuthor.error = null
        binding.tilIsbn.error = null
        binding.tilCategory.error = null
        binding.tilStock.error = null

        if (title.isEmpty()) {
            binding.tilTitle.error = "El título es obligatorio"
            esValido = false
        }
        if (author.isEmpty()) {
            binding.tilAuthor.error = "El autor es obligatorio"
            esValido = false
        }
        if (isbn.isEmpty() || (isbn.length != 10 && isbn.length != 13 && isbn.length != 3)) {
            binding.tilIsbn.error = "ISBN inválido (3, 10 o 13 dígitos)"
            esValido = false
        }
        if (category.isEmpty()) {
            binding.tilCategory.error = "La categoría es obligatoria"
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
            dbHandler.addLibro(title, author, isbn, category, stock, sinopsis, selectedImageName)
        } else {
            dbHandler.updateLibro(editingLibroId!!, title, author, isbn, category, stock, sinopsis, selectedImageName)
        }

        if (success) {
            resetForm()
            binding.containerAddForm.visibility = View.GONE
            val msg = if (editingLibroId == null) "¡Libro guardado exitosamente!" else "Información actualizada correctamente"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            loadBooks()
            binding.etSearchStock.text?.clear()
        } else {
            binding.tilIsbn.error = "Este ISBN ya existe en el sistema"
        }
    }

    private fun setupRecyclerView() {
        adapter = StockAdapter(emptyList(), this::onDeleteBook, this::onEditBook)
        binding.recyclerStock.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStock.adapter = adapter
        loadBooks()
    }

    private fun loadBooks() {
        allBooks = dbHandler.getLibros()
        filterBooks(binding.etSearchStock.text.toString())
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
        selectedImageName = libro.imagen
        val ctx = requireContext()
        val imgStr = libro.imagen ?: "bg_book_cover"
        
        if (imgStr.startsWith("content://") || imgStr.startsWith("file://")) {
            binding.ivFormPreview.setImageURI(Uri.parse(imgStr))
        } else {
            val resId = ctx.resources.getIdentifier(imgStr, "drawable", ctx.packageName)
            if (resId != 0) binding.ivFormPreview.setImageResource(resId)
            else binding.ivFormPreview.setImageResource(com.fcc.biblioteca.R.drawable.bg_book_cover)
        }
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
        binding.tilAuthor.error = null
        binding.tilIsbn.error = null
        binding.tilCategory.error = null
        binding.tilStock.error = null
        selectedImageName = null
        binding.ivFormPreview.setImageResource(com.fcc.biblioteca.R.drawable.bg_book_cover)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
