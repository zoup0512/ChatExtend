package com.zoup.android.chatextend

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.zoup.android.chatextend.databinding.ActivityMainBinding
import com.zoup.android.chatextend.ui.category.CategoryManagementFragment
import com.zoup.android.chatextend.ui.chat.ChatFragment


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: androidx.navigation.NavController

    private var mainMenu: Menu? = null
    private var shouldShowFavouriteMenu = false
    private var shouldShowSureMenu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_chat,
                R.id.nav_history,
                R.id.nav_notes,
                R.id.nav_category,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (shouldShowFavouriteMenu || shouldShowSureMenu) {
            menuInflater.inflate(R.menu.main, menu)
            this.mainMenu = menu
            if (shouldShowFavouriteMenu) {
                menu.findItem(R.id.action_favorites).isVisible = true
            } else if (shouldShowSureMenu) {
                menu.findItem(R.id.action_sure).isVisible = true
                menu.findItem(R.id.action_add).isVisible = true
                menu.findItem(R.id.action_delete).isVisible = true
            }
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorites -> {
                clickMenuItem(R.id.action_favorites)
                true
            }

            R.id.action_sure -> {
                clickMenuItem(R.id.action_sure)
                true
            }

            R.id.action_add -> {
                clickMenuItem(R.id.action_add)
                true
            }

            R.id.action_delete -> {
                clickMenuItem(R.id.action_delete)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun clickMenuItem(id: Int) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
        if (currentFragment is ChatFragment) {
            currentFragment.clickFavouriteMenuItem()
        } else if (currentFragment is CategoryManagementFragment) {
            if (id == R.id.action_add) {
                currentFragment.clickAddMenuItem()
            } else if (id == R.id.action_delete) {
                currentFragment.clickDeleteMenuItem()
            } else if (id == R.id.action_sure) {
                currentFragment.clickSureMenuItem()
            } else {
                // TODO: 添加其他处理逻辑
            }
        }
    }

    // 提供给 Fragment 调用来控制菜单显示/隐藏
    fun setFavouriteMenuVisibility(visible: Boolean) {
        shouldShowFavouriteMenu = visible
        invalidateOptionsMenu() // 强制刷新菜单
    }

    // 提供给 Fragment 调用来控制菜单显示/隐藏
    fun setSureMenuVisibility(visible: Boolean) {
        shouldShowSureMenu = visible
        invalidateOptionsMenu() // 强制刷新菜单
    }

    fun setFavouriteMenuChecked(checked: Boolean) {
        val favouriteMenu: MenuItem? = mainMenu?.findItem(R.id.action_favorites)
        if (favouriteMenu != null) {
            if (checked) {
                favouriteMenu.setIcon(R.drawable.ic_favorite_checked_24dp)
            } else {
                favouriteMenu.setIcon(R.drawable.ic_favorite_unchecked_24dp)
            }
        } else {
            Log.w("MainActivity", "mainMenu 或 menuItem 为 null")
        }
    }

}