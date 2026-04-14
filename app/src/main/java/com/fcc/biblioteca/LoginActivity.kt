package com.fcc.biblioteca

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fcc.biblioteca.databinding.ActivityLoginBinding
import com.fcc.biblioteca.db.MyDBHandler

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHandler: MyDBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dbHandler = MyDBHandler(this)
        
        binding.btnLogin.setOnClickListener {
            val emailOrUser = binding.etEmailLogin.text.toString().trim()
            val pass = binding.etPasswordLogin.text.toString()
            
            if (emailOrUser.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val valid = dbHandler.loginUsuario(emailOrUser, pass)
            if (valid) {
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.tvSwitchToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
