package com.example.salontenexapp.Vista

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.salontenexapp.R
import androidx.activity.OnBackPressedCallback
import com.example.salontenexapp.databinding.ActivityMainBinding
import com.example.salontenexapp.util.SharedPreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var prefsManager: SharedPreferencesManager
    private var userType: String? = null

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "1. onCreate iniciado")

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            Log.d(TAG, "2. Binding inflado")

            setContentView(binding.root)
            Log.d(TAG, "3. setContentView completado")

            userType = intent.getStringExtra("USER_TYPE") ?: "client"
            Log.d(TAG, "4. UserType: $userType")

            prefsManager = SharedPreferencesManager(this)
            Log.d(TAG, "5. PrefsManager inicializado")

            setupToolbar()
            Log.d(TAG, "6. Toolbar configurado")

            setupNavigation()
            Log.d(TAG, "7. Navegación configurada")

            setupHeader()
            Log.d(TAG, "8. Header configurado")

            setInitialFragment()
            Log.d(TAG, "9. Fragment inicial configurado")

            setupOnBackPressedCallback()
            Log.d(TAG, "10. Back pressed callback configurado")

            Log.d(TAG, "MainActivity COMPLETAMENTE cargada - User: $userType")

        } catch (e: Exception) {
            Log.e(TAG, "ERROR CRÍTICO en onCreate: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            val title = if (userType == "admin") {
                "Panel de Administración"
            } else {
                "Mi Salon Tenex"
            }
            supportActionBar?.title = title
            Log.d(TAG, "Toolbar título: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupToolbar: ${e.message}", e)
            throw e
        }
    }

    private fun setupNavigation() {
        try {
            when (userType) {
                "admin" -> setupAdminNavigation()
                else -> setupClientNavigation()
            }
            Log.d(TAG, "Navegación configurada para: $userType")
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupNavigation: ${e.message}", e)
            throw e
        }
    }

    private fun setupAdminNavigation() {
        try {
            Log.d(TAG, "Configurando navegación ADMIN")

            // Ocultar bottom navigation para admin
            binding.bottomNavigation.visibility = android.view.View.GONE

            // Configurar el ActionBarDrawerToggle
            toggle = ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
            binding.drawerLayout.addDrawerListener(toggle)
            toggle.syncState()

            // Configurar el listener del NavigationView
            binding.navView.setNavigationItemSelectedListener { menuItem ->
                Log.d(TAG, "Item del menú seleccionado: ${menuItem.title}")
                handleAdminNavigation(menuItem)
                true
            }

            Log.d(TAG, "Navegación admin configurada")
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupAdminNavigation: ${e.message}", e)
            throw e
        }
    }

    private fun setupClientNavigation() {
        try {
            Log.d(TAG, "Configurando navegación CLIENTE")

            // Para clientes, deshabilitar drawer
            binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            binding.navView.visibility = android.view.View.GONE

            // Configurar bottom navigation
            binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
                Log.d(TAG, "Item del bottom navigation seleccionado: ${menuItem.title}")
                handleClientNavigation(menuItem)
                true
            }
            binding.bottomNavigation.visibility = android.view.View.VISIBLE

            Log.d(TAG, "Navegación cliente configurada")
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupClientNavigation: ${e.message}", e)
            throw e
        }
    }

    private fun setupHeader() {
        try {
            if (userType == "admin") {
                val headerView = binding.navView.getHeaderView(0)
                val tvEmail = headerView.findViewById<TextView>(R.id.textView_email)

                val userEmail = prefsManager.getUserEmail() ?: "admin@salon.com"
                tvEmail.text = userEmail
                Log.d(TAG, "Header configurado con email: $userEmail")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupHeader: ${e.message}", e)
            // No es crítico, continuar
        }
    }

    private fun setInitialFragment() {
        try {
            Log.d(TAG, "Configurando fragment inicial")

            val fragment = when (userType) {
                "admin" -> AdminDashboardFragment()
                else -> ClientReservationsFragment()
            }

            replaceFragment(fragment)
            Log.d(TAG, "Fragment inicial configurado: ${fragment::class.java.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error en setInitialFragment: ${e.message}", e)
            // Fallback a fragment básico
            val fallbackFragment = androidx.fragment.app.Fragment().apply {
                val view = android.widget.TextView(this@MainActivity).apply {
                    text = "¡Bienvenido $userType!"
                    textSize = 24f
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.BLACK)
                    setBackgroundColor(android.graphics.Color.WHITE)
                    setPadding(50, 50, 50, 50)
                }
            }
            replaceFragment(fallbackFragment)
        }
    }

    private fun handleAdminNavigation(menuItem: MenuItem) {
        try {
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    Log.d(TAG, "Navegando a Dashboard")
                    replaceFragment(AdminDashboardFragment())
                }
                R.id.nav_manage_reservations -> {
                    Log.d(TAG, "Navegando a Gestionar Reservas")
                    replaceFragment(ManageReservationsFragment())
                }
                R.id.nav_manage_salons -> {
                    Log.d(TAG, "Navegando a Gestionar Salones")
                    replaceFragment(ManageSalonsFragment())
                }
                R.id.nav_manage_services -> {
                    Log.d(TAG, "Navegando a Gestionar Servicios")
                    replaceFragment(ManageServicesFragment())
                }
                R.id.nav_manage_admins -> {
                    Log.d(TAG, "Navegando a Gestionar Admins")
                    replaceFragment(ManageAdminsFragment())
                }
                R.id.nav_profile -> {
                    Log.d(TAG, "Navegando a Perfil")
                    replaceFragment(ProfileFragment())
                }
                R.id.nav_change_password -> {
                    Log.d(TAG, "Navegando a Cambiar Contraseña")
                    replaceFragment(ChangePasswordFragment())
                }
                R.id.nav_logout -> {
                    Log.d(TAG, "Cerrando sesión")
                    logout()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } catch (e: Exception) {
            Log.e(TAG, "Error en handleAdminNavigation: ${e.message}", e)
        }
    }

    private fun handleClientNavigation(menuItem: MenuItem) {
        try {
            when (menuItem.itemId) {
                R.id.nav_my_reservations -> {
                    Log.d(TAG, "Navegando a Mis Reservaciones")
                    replaceFragment(ClientReservationsFragment())
                }
                R.id.nav_new_reservation -> {
                    Log.d(TAG, "Navegando a Nueva Reserva")
                    replaceFragment(CreateReservationFragment())
                }
                R.id.nav_profile -> {
                    Log.d(TAG, "Navegando a Perfil")
                    replaceFragment(ProfileFragment())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en handleClientNavigation: ${e.message}", e)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        try {
            Log.d(TAG, "Iniciando replaceFragment con: ${fragment::class.java.simpleName}")

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            Log.d(TAG, "Fragment reemplazado: ${fragment::class.java.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error en replaceFragment: ${e.message}", e)
        }
    }

    private fun logout() {
        prefsManager.logout()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupOnBackPressedCallback() {
        try {
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (userType == "admin" && binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        finish()
                    }
                }
            }
            onBackPressedDispatcher.addCallback(this, callback)
            Log.d(TAG, "Back pressed callback configurado")
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupOnBackPressedCallback: ${e.message}", e)
        }
    }

    // IMPORTANTE: Este método es necesario para que el toggle funcione
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}