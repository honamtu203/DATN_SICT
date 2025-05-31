package com.qltc.finace.view.activity.home

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseActivity
import com.qltc.finace.databinding.ActivityMainBinding
import com.qltc.finace.databinding.NavHeaderMainBinding
import com.qltc.finace.view.activity.authen.AuthenticationActivity
import com.qltc.finace.view.main.webview.ChromeCustomTabHelper
import com.qltc.finace.view.main.webview.NotebookLMOptionDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityMainBinding, HomeActivityViewModel>(){
    override val viewModel: HomeActivityViewModel by viewModels()
    override val layoutId: Int = R.layout.activity_main
    private lateinit var googleSignInClient: GoogleSignInClient
    private var navHostFragment : NavHostFragment?= null
    private var navController : NavController?=null
    val headerDrawer : NavHeaderMainBinding by lazy {  NavHeaderMainBinding.bind(viewBinding.navigationViewDrawer.getHeaderView(0))}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_home_container) as NavHostFragment
        navController = navHostFragment?.navController
        navigationBottom()
        viewBinding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            viewBinding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight: Int = viewBinding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                this@HomeActivity.viewBinding.bottomNav.visibility = View.GONE
            } else {
                when (navController?.currentDestination?.id) {
                    R.id.fag_home,
                    R.id.fag_calender,
                    R.id.fag_report,
                    R.id.fag_profile -> {
                        this@HomeActivity.viewBinding.bottomNav.visibility = View.VISIBLE
                    }

                    else -> {
                        this@HomeActivity.viewBinding.bottomNav.visibility = View.GONE
                    }
                }
            }

        }
        setUpDrawerLayout()
//        val settings = FirebaseFirestoreSettings.Builder()
//            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED).build())
//            .build()
//        Firebase.firestore.firestoreSettings = settings
//        Firebase.firestore.persistentCacheIndexManager?.apply {
//            // Indexing is disabled by default
//            enableIndexAutoCreation()
//        } ?: println("indexManager is null")

    }

    override fun onResume() {
        super.onResume()
    }

    private fun navigationBottom() {
        viewBinding.bottomNav.apply {
            navController?.let {
                setupWithNavController(it)
            }
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.fag_home -> {
                        navController?.navigate(R.id.fag_home)
                        true
                    }

                    R.id.fag_calender -> {
                        navController?.navigate(R.id.fag_calender)
                        true
                    }
                    R.id.fag_report -> {
                        navController?.navigate(R.id.fag_report)
                        true
                    }
                    R.id.fag_notebook -> {
                        // Show option dialog to choose between Chrome Custom Tabs or WebView
                        NotebookLMOptionDialog.showOpenOptions(this@HomeActivity, navController)
                        true
                    }
                    R.id.fagg_profile -> {
                        viewBinding.drawer.openDrawer(GravityCompat.END)
                        false
                    }
                    else -> {
                        viewBinding.drawer.openDrawer(GravityCompat.END)
                        false
                    }
                }
            }
            navController?.addOnDestinationChangedListener{_,des,_ ->
                when(des.id) {
                    R.id.fag_home,
                    R.id.fag_calender,
                    R.id.fag_report,
                    R.id.fag_profile -> {
                        this@HomeActivity.viewBinding.bottomNav.visibility = View.VISIBLE
                    }
                    else -> {
                        this@HomeActivity.viewBinding.bottomNav.visibility = View.GONE
                    }
                }
            }
        }
    }
    fun setUpDrawerLayout() {
        headerDrawer.lifecycleOwner = this
        viewModel.getUserNameCurrent()
        headerDrawer.viewModel = viewModel
        viewBinding.navigationViewDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_drawer_home -> {
                    navController?.navigate(R.id.fag_home)
                    true
                }
                R.id.item_drawer_calendar -> {
                    navController?.navigate(R.id.fag_calender)
                    true
                }
                R.id.item_drawer_report -> {
                    navController?.navigate(R.id.fag_report)
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
                    navController?.navigate(R.id.fag_profile)
                    true
                }
                else -> {
                    false
                }
            }
            viewBinding.drawer.closeDrawers()
            true
        }
    }
    fun signOutFromGoogle() {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener { task: Task<Void> ->
            if (task.isSuccessful) {
                val intent = Intent(this, AuthenticationActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, AuthenticationActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

}