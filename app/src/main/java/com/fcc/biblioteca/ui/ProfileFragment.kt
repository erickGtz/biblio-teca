package com.fcc.biblioteca.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fcc.biblioteca.LoginActivity
import com.fcc.biblioteca.SmsHelper
import com.fcc.biblioteca.databinding.FragmentProfileBinding
import com.fcc.biblioteca.db.MyDBHandler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.fcc.biblioteca.R

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHandler: MyDBHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHandler = MyDBHandler(requireContext())
        
        cargarDatosPerfil()

        binding.btnLogout.setOnClickListener {
            val prefs = requireActivity().getSharedPreferences("BibliotecaPrefs", Context.MODE_PRIVATE)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión y salir de la cuenta de biblioteca?")
                .setPositiveButton("Salir") { _, _ ->
                    prefs.edit().clear().apply()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.btnSimulateExpiry.setOnClickListener {
            val prefs = requireActivity().getSharedPreferences("BibliotecaPrefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("userId", -1)
            if (userId != -1) {
                dbHandler.insertMockLibrosIfEmpty()
                dbHandler.insertPrestamoDePrueba(userId)
                Toast.makeText(requireContext(), "Préstamo simulado (expira mañana).", Toast.LENGTH_SHORT).show()
                cargarDatosPerfil()
            }
        }
    }

    private fun cargarDatosPerfil() {
        val prefs = requireActivity().getSharedPreferences("BibliotecaPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)
        val role = prefs.getString("userRole", "usuario")
        
        if (userId != -1) {
            val user = dbHandler.getUsuario(userId)
            
            if (user != null) {
                binding.tvUserName.text = "${user.nombre} ${user.apellido1} ${user.apellido2 ?: ""}".trim()
                binding.tvUserEmail.text = user.correo
                binding.tvUserRole.text = user.rol.uppercase()
            }

            // Stats
            val personalCount = dbHandler.getContadorPrestamos(userId)
            binding.tvActiveReserves.text = personalCount.toString()
            
            val expiring = dbHandler.getPrestamosPorExpirar(userId)
            val userPhone = user?.telefono ?: ""

            if (expiring.isNotEmpty()) {
                binding.cardNotifications.visibility = View.VISIBLE
                binding.containerAlerts.removeAllViews()
                expiring.forEach { p ->
                    val layout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 0, 0, 16)
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val textView = TextView(requireContext()).apply {
                        text = "• '${p.libro.titulo}' expira el ${p.fechaFin}"
                        setTextColor(Color.DKGRAY)
                        textSize = 14f
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val btnSms = com.google.android.material.button.MaterialButton(requireContext()).apply {
                        text = ""
                        setIcon(ContextCompat.getDrawable(requireContext(), android.R.drawable.sym_action_email))
                        setIconSize((24 * resources.displayMetrics.density).toInt())
                        iconPadding = 0
                        iconGravity = com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
                        layoutParams = LinearLayout.LayoutParams(
                            (48 * resources.displayMetrics.density).toInt(),
                            (48 * resources.displayMetrics.density).toInt()
                        ).apply {
                            marginStart = (8 * resources.displayMetrics.density).toInt()
                        }
                        setPadding(0, 0, 0, 0)
                        setOnClickListener {
                            checkAndSendSms(userPhone, p.libro.titulo, p.fechaFin)
                        }
                    }

                    layout.addView(textView)
                    layout.addView(btnSms)
                    binding.containerAlerts.addView(layout)
                }
            } else {
                binding.cardNotifications.visibility = View.GONE
            }

            if (role == "admin") {
                binding.containerBooksLoanedStat.visibility = View.VISIBLE
                val globalLoanedCount = dbHandler.getTotalContadorPrestamos()
                val totalUsers = dbHandler.getTotalUsuarios()
                val totalLibros = dbHandler.getTotalLibros()
                
                binding.tvBooksLoaned.text = globalLoanedCount.toString()
                
                // Hack: We can repurpose some fields or just update the text of the labels if we want more stats.
                // For now, let's keep it simple but accurate.
                // Adjusting the label "Libros prestados" to "Préstamos Totales" if admin.
                val parentStat1 = binding.containerBooksLoanedStat.getChildAt(0) as? TextView
                parentStat1?.text = "Préstamos Totales"

                // Maybe show total users in the Active Reserves card for admin?
                // Let's customize the second stat for admin too.
                val parentStat2 = binding.cardActiveReserves.getChildAt(0) as? TextView
                parentStat2?.text = "Usuarios Totales"
                binding.tvActiveReserves.text = totalUsers.toString()
                
            } else {
                binding.containerBooksLoanedStat.visibility = View.GONE
                val parentStat2 = binding.cardActiveReserves.getChildAt(0) as? TextView
                parentStat2?.text = "Reservas activas"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosPerfil()
    }

    private fun checkAndSendSms(phone: String, title: String, date: String) {
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "No hay teléfono registrado para este usuario", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 101)
            Toast.makeText(requireContext(), "Por favor concede permiso de SMS e intenta de nuevo", Toast.LENGTH_LONG).show()
        } else {
            SmsHelper.sendExpirySms(phone, title, date)
            Toast.makeText(requireContext(), "SMS de recordatorio enviado a $phone", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
