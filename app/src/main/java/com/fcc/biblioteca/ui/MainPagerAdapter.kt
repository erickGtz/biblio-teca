package com.fcc.biblioteca.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(
    activity: FragmentActivity,
    private val isAdmin: Boolean
) : FragmentStateAdapter(activity) {

    // Define the list of fragments based on user role
    private val fragments = mutableListOf<Fragment>().apply {
        add(CatalogFragment())
        if (isAdmin) {
            add(StockFragment())
        }
        add(MyLoansFragment())
        add(ProfileFragment())
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    /**
     * Helper function to get the correct menu ID for a ViewPager position
     */
    fun getMenuIdForPosition(position: Int): Int {
        return if (isAdmin) {
            when (position) {
                0 -> com.fcc.biblioteca.R.id.nav_catalog
                1 -> com.fcc.biblioteca.R.id.nav_stock
                2 -> com.fcc.biblioteca.R.id.nav_loans
                3 -> com.fcc.biblioteca.R.id.nav_profile
                else -> com.fcc.biblioteca.R.id.nav_catalog
            }
        } else {
            when (position) {
                0 -> com.fcc.biblioteca.R.id.nav_catalog
                1 -> com.fcc.biblioteca.R.id.nav_loans
                2 -> com.fcc.biblioteca.R.id.nav_profile
                else -> com.fcc.biblioteca.R.id.nav_catalog
            }
        }
    }

    /**
     * Helper function to get the position for a menu ID
     */
    fun getPositionForMenuId(menuId: Int): Int {
        return if (isAdmin) {
            when (menuId) {
                com.fcc.biblioteca.R.id.nav_catalog -> 0
                com.fcc.biblioteca.R.id.nav_stock -> 1
                com.fcc.biblioteca.R.id.nav_loans -> 2
                com.fcc.biblioteca.R.id.nav_profile -> 3
                else -> 0
            }
        } else {
            when (menuId) {
                com.fcc.biblioteca.R.id.nav_catalog -> 0
                com.fcc.biblioteca.R.id.nav_loans -> 1
                com.fcc.biblioteca.R.id.nav_profile -> 2
                else -> 0
            }
        }
    }
}
