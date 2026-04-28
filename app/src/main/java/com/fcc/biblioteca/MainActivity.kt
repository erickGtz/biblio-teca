package com.fcc.biblioteca

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fcc.biblioteca.databinding.ActivityMainBinding
import com.fcc.biblioteca.ui.CatalogFragment
import com.fcc.biblioteca.ui.StockFragment
import com.fcc.biblioteca.ui.ProfileFragment
import com.fcc.biblioteca.ui.MainPagerAdapter

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
        } else {
            binding.bottomNavigation.menu.removeItem(R.id.nav_catalog)
            val loansItem = binding.bottomNavigation.menu.findItem(R.id.nav_loans)
            loansItem?.title = "Préstamos"
        }

        val adapter = MainPagerAdapter(this, role == "admin")
        binding.viewPager.adapter = adapter

        // Sync ViewPager swipe with BottomNavigation
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Cerrar detalle si está abierto al cambiar de pestaña por swipe
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
                
                val menuId = adapter.getMenuIdForPosition(position)
                binding.bottomNavigation.selectedItemId = menuId
            }
        })

        // Sync BottomNavigation clicks with ViewPager
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val position = adapter.getPositionForMenuId(item.itemId)
            if (position != -1) {
                // Si hay un detalle abierto (en fragment_container), cerrarlo
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
                
                binding.viewPager.currentItem = position
                true
            } else {
                false
            }
        }
    }

    fun selectLoansTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_loans
    }
}