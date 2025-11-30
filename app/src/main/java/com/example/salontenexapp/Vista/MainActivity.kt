package com.example.salontenexapp.Vista

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.salontenexapp.Vista.ClientReservationsFragment
import com.example.salontenexapp.R
import androidx.activity.OnBackPressedCallback
import com.example.salontenexapp.databinding.ActivityMainBinding
import com.example.salontenexapp.util.SharedPreferencesManager
import com.example.salontenexapp.Vista.AdminDashboardFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var prefsManager: SharedPreferencesManager
    private var userType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userType = intent.getStringExtra("USER_TYPE") ?: "client"
        prefsManager = SharedPreferencesManager(this)
        setupOnBackPressedCallback()

        setupToolbar()
        setupNavigation()
        setupHeader()
        setInitialFragment()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val title = if (userType == "admin") {
            "Panel de Administración"
        } else {
            "Mi Salon Tenex"
        }
        supportActionBar?.title = title
    }

    private fun setupNavigation() {
        when (userType) {
            "admin" -> setupAdminNavigation()
            else -> setupClientNavigation()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (userType == "client") {
            menuInflater.inflate(R.menu.menu_toolbar_client, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (userType == "client" && item.itemId == R.id.action_logout) {
            logout()
            return true
        }

        if (userType == "admin" && toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupAdminNavigation() {
        binding.navView.visibility = android.view.View.VISIBLE
        binding.bottomNavigation.visibility = android.view.View.GONE

        binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        invalidateOptionsMenu()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            handleAdminNavigation(menuItem)
            true
        }
    }

    private fun setupClientNavigation() {
        binding.bottomNavigation.visibility = android.view.View.VISIBLE
        binding.navView.visibility = android.view.View.GONE

        binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding.toolbar.navigationIcon = null

        invalidateOptionsMenu()

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            handleClientNavigation(menuItem)
            true
        }
    }

    private fun setupHeader() {
        if (userType == "admin") {
            val headerView = binding.navView.getHeaderView(0)
            val tvEmail = headerView.findViewById<TextView>(R.id.textView_email)

            val userEmail = prefsManager.getUserEmail() ?: "admin@salon.com"
            tvEmail.text = userEmail
        }
    }

    private fun setInitialFragment() {
        val fragment = when (userType) {
            "admin" -> AdminDashboardFragment()
            else -> ClientReservationsFragment()
        }
        replaceFragment(fragment)
    }

    private fun handleAdminNavigation(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_dashboard -> replaceFragment(AdminDashboardFragment())
            R.id.nav_manage_reservations -> replaceFragment(ManageReservationsFragment())
            R.id.nav_manage_salons -> replaceFragment(ManageSalonsFragment())
            R.id.nav_manage_services -> replaceFragment(ManageServicesFragment())
            R.id.nav_profile -> replaceFragment(ProfileFragment())
            R.id.nav_change_password -> replaceFragment(ChangePasswordFragment())
            R.id.nav_logout -> logout()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun handleClientNavigation(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_my_reservations -> replaceFragment(ClientReservationsFragment())
            R.id.nav_new_reservation -> replaceFragment(CreateReservationFragment())
            R.id.nav_profile -> replaceFragment(ProfileFragment())
        }
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(fragment.javaClass.simpleName)
        }

        transaction.commit()
    }

    private fun logout() {
        prefsManager.logout()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupOnBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    fun isDrawerOpen(): Boolean {
        return binding.drawerLayout.isDrawerOpen(GravityCompat.START)
    }

    fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    fun selectNavigationItem(menuItemId: Int) {
        val menuItem = binding.navView.menu.findItem(menuItemId)
        if (menuItem != null) {
            handleAdminNavigation(menuItem)
        }
    }
}
