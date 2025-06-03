package com.zoup.android.chatextend

import android.os.Bundle
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
import com.zoup.android.chatextend.ui.chat.ChatFragment


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: androidx.navigation.NavController

    private var shouldShowMenu = false
    private var isFavorite = false

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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
//        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                if (currentFragment is ChatFragment) {
//                    val chatViewModel = currentFragment.viewModel
//                    chatViewModel.collectState.collect { isCollected ->
//                        val menu = binding.appBarMain.toolbar.menu
//                        val favoriteItem = menu.findItem(R.id.action_favorites)
//                        favoriteItem?.setIcon(if (isCollected) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp)
//                    }
//                }
//            }
//        }
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_chat, R.id.nav_history, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (shouldShowMenu) {
            menuInflater.inflate(R.menu.main, menu)
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorites -> {
                onMenuFavoritesSelected()
//                item.setIcon(if (isFavorite) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
//        if (item.getItemId() === R.id.action_favorites) {
//            isFavorite = !isFavorite
//            item.setIcon(if (isFavorite) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp)
//            return true
//        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun onMenuFavoritesSelected() {
//        val currentDestination = navController.currentDestination
//        if (currentDestination != null) {
//            // 获取当前Fragment的ID
//            val fragmentId = currentDestination.id
//            // 你可以根据fragmentId进一步获取Fragment实例，例如：
//            val currentFragment: Fragment? = supportFragmentManager.findFragmentById(fragmentId)?.childFragmentManager?.fragments?.firstOrNull()
//            if (currentFragment is ChatFragment) {
//                currentFragment.clickFavorites()
//            }
//        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
        if (currentFragment is ChatFragment) {
            currentFragment.clickFavorites()
        }
    }

    // 提供给 Fragment 调用来控制菜单显示/隐藏
    fun setMenuVisibility(visible: Boolean) {
        shouldShowMenu = visible
        invalidateOptionsMenu() // 强制刷新菜单
    }

    fun setFavorite(isFavorite: Boolean) {
        this.isFavorite = isFavorite
    }

}