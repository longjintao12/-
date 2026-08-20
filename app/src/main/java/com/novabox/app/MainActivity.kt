package com.novabox.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.novabox.app.databinding.ActivityMainBinding
import com.novabox.app.ui.favorite.FavoriteFragment
import com.novabox.app.ui.home.HomeFragment
import com.novabox.app.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private var homeFragment: HomeFragment? = null
    private var favoriteFragment: FavoriteFragment? = null
    private var settingsFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        homeFragment = HomeFragment()
        showFragment(homeFragment!!)

        b.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(homeFragment!!)
                R.id.nav_favorite -> {
                    if (favoriteFragment == null) favoriteFragment = FavoriteFragment()
                    showFragment(favoriteFragment!!)
                }
                R.id.nav_mine -> {
                    if (settingsFragment == null) settingsFragment = SettingsFragment()
                    showFragment(settingsFragment!!)
                }
            }
            true
        }
    }

    private fun showFragment(frag: Fragment) {
        val fm = supportFragmentManager
        val tx = fm.beginTransaction()
        fm.fragments.forEach { tx.hide(it) }
        if (frag.isAdded) tx.show(frag) else tx.add(R.id.container, frag)
        tx.commit()
    }
}
