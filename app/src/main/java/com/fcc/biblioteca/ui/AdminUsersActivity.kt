package com.fcc.biblioteca.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fcc.biblioteca.R
import com.fcc.biblioteca.db.MyDBHandler
import com.fcc.biblioteca.model.Usuario

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var rvAdminUsers: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: AdminUsersAdapter
    private lateinit var dbHandler: MyDBHandler
    private var usuarios: List<Usuario> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        rvAdminUsers = findViewById(R.id.rvAdminUsers)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        
        dbHandler = MyDBHandler(this)

        rvAdminUsers.layoutManager = LinearLayoutManager(this)
        
        adapter = AdminUsersAdapter(emptyList(), dbHandler)
        rvAdminUsers.adapter = adapter

        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        usuarios = dbHandler.getAllUsuarios()
        if (usuarios.isEmpty()) {
            rvAdminUsers.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
        } else {
            rvAdminUsers.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
            adapter.updateData(usuarios)
        }
    }
}
