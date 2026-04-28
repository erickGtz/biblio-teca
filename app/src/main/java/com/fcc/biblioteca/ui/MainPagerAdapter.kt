package com.fcc.biblioteca.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(
    activity: FragmentActivity,
    private val isAdmin: Boolean
) : FragmentStateAdapter(activity) {

    private val fragments = mutableListOf<Fragment>().apply {
        if (isAdmin) {
            add(StockFragment())
            add(AdminLoansFragment())
            add(ProfileFragment())
        } else {
            add(CatalogFragment())
            add(MyLoansFragment())
            add(ProfileFragment())
        }
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getMenuIdForPosition(position: Int): Int {
        return if (isAdmin) {
            when (position) {
                0 -> com.fcc.biblioteca.R.id.nav_stock
                1 -> com.fcc.biblioteca.R.id.nav_loans
                2 -> com.fcc.biblioteca.R.id.nav_profile
                else -> com.fcc.biblioteca.R.id.nav_stock
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

    fun getPositionForMenuId(menuId: Int): Int {
        return if (isAdmin) {
            when (menuId) {
                com.fcc.biblioteca.R.id.nav_stock -> 0
                com.fcc.biblioteca.R.id.nav_loans -> 1
                com.fcc.biblioteca.R.id.nav_profile -> 2
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
