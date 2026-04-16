package com.fcc.biblioteca

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fcc.biblioteca.databinding.ActivityMainBinding
import com.fcc.biblioteca.ui.CatalogFragment
import com.fcc.biblioteca.ui.StockFragment
import com.fcc.biblioteca.ui.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val prefs = getSharedPreferences("BibliotecaPrefs", MODE_PRIVATE)
        val role = prefs.getString("userRole", "usuario")
        
        if (role != "admin") {
            binding.bottomNavigation.menu.removeItem(R.id.nav_stock)
        }

        if (savedInstanceState == null) {
            loadFragment(CatalogFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_catalog
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> {
                    loadFragment(CatalogFragment())
                    true
                }
                R.id.nav_loans -> {
                    loadFragment(com.fcc.biblioteca.ui.MyLoansFragment())
                    true
                }
                R.id.nav_stock -> {
                    if (role == "admin") {
                        loadFragment(StockFragment())
                        true
                    } else {
                        false
                    }
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}