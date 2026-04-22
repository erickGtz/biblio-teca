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
            
            // Limpiar errores previos
            binding.tilName.error = null
            binding.tilApellidoPaterno.error = null
            binding.tilPhone.error = null
            binding.tilEmail.error = null
            binding.tilPassword.error = null
            binding.tilConfirmPassword.error = null

            var isValid = true

            if (nombre.isEmpty()) {
                binding.tilName.error = "Ingresa tu nombre"
                isValid = false
            }
            if (ap1.isEmpty()) {
                binding.tilApellidoPaterno.error = "Ingresa tu apellido"
                isValid = false
            }
            if (phone.isEmpty() || phone.length < 10) {
                binding.tilPhone.error = "Teléfono inválido (mín. 10 dígitos)"
                isValid = false
            }
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Correo electrónico inválido"
                isValid = false
            }
            if (pass.isEmpty()) {
                binding.tilPassword.error = "Ingresa una contraseña"
                isValid = false
            } else if (pass.length < 6) {
                binding.tilPassword.error = "Mínimo 6 caracteres"
                isValid = false
            }
            if (pass != confPass) {
                binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                isValid = false
            }

            if (!isValid) {
                Toast.makeText(this, "Corrige los errores marcados en rojo", Toast.LENGTH_SHORT).show()
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
