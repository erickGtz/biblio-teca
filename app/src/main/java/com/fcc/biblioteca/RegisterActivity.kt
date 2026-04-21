package com.fcc.biblioteca

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fcc.biblioteca.databinding.ActivityRegisterBinding
import com.fcc.biblioteca.db.MyDBHandler

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHandler: MyDBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dbHandler = MyDBHandler(this)
        
        binding.btnRegister.setOnClickListener {
            val nombre = binding.etName.text.toString().trim()
            val ap1 = binding.etApellidoPaterno.text.toString().trim()
            val ap2 = binding.etApellidoMaterno.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmailRegister.text.toString().trim()
            val pass = binding.etPasswordRegister.text.toString()
            val confPass = binding.etConfirmPasswordRegister.text.toString()
            
            if (nombre.isEmpty() || ap1.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (pass != confPass) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val exito = dbHandler.registerUsuario(nombre, ap1, ap2, email, phone, pass)
            if (exito) {
                Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
                
                // Fetch the user to save session and ID properly.
                val loggedUser = dbHandler.loginUsuario(email, pass)
                if (loggedUser != null) {
                    val prefs = getSharedPreferences("BibliotecaPrefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putInt("userId", loggedUser.id_usuario)
                        putString("userName", loggedUser.nombre)
                        putString("userRole", loggedUser.rol)
                        putString("userPhone", loggedUser.telefono)
                        apply()
                    }
                }
                
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al registrar: el correo o teléfono ya están en uso", Toast.LENGTH_LONG).show()
            }
        }
        
        binding.tvSwitchToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
