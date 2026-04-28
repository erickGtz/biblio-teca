package com.fcc.biblioteca.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fcc.biblioteca.R
import com.fcc.biblioteca.db.MyDBHandler
import com.fcc.biblioteca.model.Usuario

class AdminUsersAdapter(
    private var usuarios: List<Usuario>,
    private val dbHandler: MyDBHandler
) : RecyclerView.Adapter<AdminUsersAdapter.AdminUserViewHolder>() {

    class AdminUserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvActiveLoans: TextView = view.findViewById(R.id.tvActiveLoans)
        val tvUserRole: TextView = view.findViewById(R.id.tvUserRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_user, parent, false)
        return AdminUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminUserViewHolder, position: Int) {
        val usuario = usuarios[position]
        
        val nombreCompleto = "${usuario.nombre} ${usuario.apellido1} ${usuario.apellido2 ?: ""}".trim()
        holder.tvUserName.text = nombreCompleto
        
        val activeLoans = dbHandler.getContadorPrestamos(usuario.id_usuario)
        holder.tvActiveLoans.text = "Préstamos activos: $activeLoans"
        
        holder.tvUserRole.text = usuario.rol.uppercase()
    }

    override fun getItemCount() = usuarios.size

    fun updateData(newUsuarios: List<Usuario>) {
        usuarios = newUsuarios
        notifyDataSetChanged()
    }
}
