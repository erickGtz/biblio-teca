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
        }

        val adapter = MainPagerAdapter(this, role == "admin")
        binding.viewPager.adapter = adapter

        // Sync ViewPager swipe with BottomNavigation
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val menuId = adapter.getMenuIdForPosition(position)
                binding.bottomNavigation.selectedItemId = menuId
            }
        })

        // Sync BottomNavigation clicks with ViewPager
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val position = adapter.getPositionForMenuId(item.itemId)
            if (position != -1) {
                binding.viewPager.currentItem = position
                true
            } else {
                false
            }
        }
    }
}