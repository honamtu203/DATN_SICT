package com.qltc.finace.view.activity.home

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseActivity
import com.qltc.finace.databinding.ActivityMainBinding
import com.qltc.finace.databinding.NavHeaderMainBinding
import com.qltc.finace.view.activity.authen.AuthenticationActivity
import com.qltc.finace.view.main.webview.NotebookLMOptionDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityMainBinding, HomeActivityViewModel>() {
    override val viewModel: HomeActivityViewModel by viewModels()
    override val layoutId: Int = R.layout.activity_main
    private lateinit var googleSignInClient: GoogleSignInClient
    private var navHostFragment: NavHostFragment? = null
    private var navController: NavController? = null
    val headerDrawer: NavHeaderMainBinding by lazy { NavHeaderMainBinding.bind(viewBinding.navigationViewDrawer.getHeaderView(0)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(viewBinding.root)
            setupNavigation()
            setupKeyboardVisibilityListener()
            setupDrawerLayout()
        } catch (e: Exception) {
            // Nếu có lỗi khởi tạo, quay về màn Authentication
            startActivity(Intent(this, AuthenticationActivity::class.java))
            finish()
        }
    }

    private fun setupNavigation() {
        try {
            navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_home_container) as? NavHostFragment
            navController = navHostFragment?.navController

            if (navController == null) {
                throw Exception("Navigation initialization failed")
            }

            viewBinding.bottomNav.apply {
                navController?.let {
                    setupWithNavController(it)
                }
                setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.frag_home -> {
                            navController?.navigate(R.id.frag_home)
                            true
                        }
//                        R.id.frag_enter -> {
//                            navController?.navigate(R.id.frag_enter)
//                            true
//                        }
                        R.id.frag_calendar -> {
                            navController?.navigate(R.id.frag_calendar)
                            true
                        }
                        R.id.frag_report -> {
                            navController?.navigate(R.id.frag_report)
                            true
                        }
                        R.id.frag_notebook -> {
                            NotebookLMOptionDialog.showOpenOptions(this@HomeActivity, navController)
                            true
                        }
                        R.id.frag_profile -> {
                            viewBinding.drawer.openDrawer(GravityCompat.END)
                            false
                        }
                        else -> {
                            viewBinding.drawer.openDrawer(GravityCompat.END)
                            false
                        }
                    }
                }
            }

            navController?.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.frag_home,
//                    R.id.frag_enter,
                    R.id.frag_calendar,
                    R.id.frag_report,
                    R.id.frag_notebook,
                    R.id.frag_profile -> {
                        viewBinding.bottomNav.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinding.bottomNav.visibility = View.GONE
                    }
                }
            }
        } catch (e: Exception) {
            // Log lỗi và xử lý phù hợp
            e.printStackTrace()
        }
    }

    private fun setupKeyboardVisibilityListener() {
        viewBinding.root.viewTreeObserver.addOnGlobalLayoutListener {
            try {
                val rect = Rect()
                viewBinding.root.getWindowVisibleDisplayFrame(rect)
                val screenHeight: Int = viewBinding.root.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) {
                    viewBinding.bottomNav.visibility = View.GONE
                } else {
                    navController?.currentDestination?.id?.let { destinationId ->
                        when (destinationId) {
                            R.id.frag_home,
//                            R.id.frag_enter,
                            R.id.frag_calendar,
                            R.id.frag_report,
                            R.id.frag_notebook,
                            R.id.frag_profile -> {
                                viewBinding.bottomNav.visibility = View.VISIBLE
                            }
                            else -> {
                                viewBinding.bottomNav.visibility = View.GONE
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupDrawerLayout() {
        headerDrawer.lifecycleOwner = this
        viewModel.getUserNameCurrent()
        headerDrawer.viewModel = viewModel
        viewBinding.navigationViewDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_drawer_home -> {
                    navController?.navigate(R.id.frag_home)
                    true
                }
                R.id.item_drawer_enter -> {
                    navController?.navigate(R.id.frag_enter)
                    true
                }
                R.id.item_drawer_calendar -> {
                    navController?.navigate(R.id.frag_calendar)
                    true
                }
                R.id.item_drawer_report -> {
                    navController?.navigate(R.id.frag_report)
                    true
                }

                R.id.item_drawer_notebook -> {
                    // Show option dialog for drawer access as well
                    NotebookLMOptionDialog.showOpenOptions(this@HomeActivity, navController)
                    true
                }

                R.id.item_drawer_pdf -> {
                    navController?.navigate(R.id.frg_export_pdf)
                    true
                }

                R.id.item_drawer_excel -> {
                    // Handle setting functionality
                    true
                }

                R.id.item_drawer_share -> {
                    // Handle share functionality
                    true
                }
                R.id.item_drawer_infor -> {
                    // Handle information display
                    true
                }
                R.id.item_drawer_faq -> {
                    navController?.navigate(R.id.frag_FAQ)
                    true
                }
                R.id.item_drawer_account -> {
                    navController?.navigate(R.id.frag_profile)
                    true
                }
                else -> false
            }
            viewBinding.drawer.closeDrawers()
            true
        }
    }

    fun signOutFromGoogle() {
        try {
            FirebaseAuth.getInstance().signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut().addOnCompleteListener { task: Task<Void> ->
                val intent = Intent(this, AuthenticationActivity::class.java)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            // Nếu có lỗi, vẫn chuyển về màn Authentication
            startActivity(Intent(this, AuthenticationActivity::class.java))
            finish()
        }
    }
}