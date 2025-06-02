package com.qltc.finace.view.authentication.sign_up

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentSignUpBinding
import com.qltc.finace.view.activity.authen.AuthenticationActivity
import com.qltc.finace.view.activity.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragmentSignUpBinding, SignUpViewModel>(), SignUpListener {
    override val viewModel: SignUpViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_sign_up
    
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            listener = this@SignUpFragment
            viewModel = this@SignUpFragment.viewModel
        }
        setupGoogleSignIn()
    }
    
    private fun setupGoogleSignIn() {
        context?.let {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(it.getString(R.string.client_id))
                .requestEmail().build()
            googleSignInClient = GoogleSignIn.getClient(it, gso)
            Log.d("GoogleSignIn", "Client ID (SignUp): ${it.getString(R.string.client_id)}")
        }
        
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val signInAccountTask: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                if (signInAccountTask.isSuccessful) {
                    Log.d("GoogleSignIn", "Account task successful (SignUp)")
                    try {
                        val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)
                        if (googleSignInAccount != null) {
                            Log.d("GoogleSignIn", "Account retrieved (SignUp): ${googleSignInAccount.email}")
                            val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                                googleSignInAccount.idToken, null
                            )
                            firebaseAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener(this.requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        Log.d("GoogleSignIn", "Firebase auth successful (SignUp)")
                                        displayToast("Đăng ký thành công")
                                        val isNewUser = task.result.additionalUserInfo?.isNewUser ?: false
                                        navigateToHome(isNewUser)
                                    } else {
                                        Log.e("GoogleSignIn", "Firebase auth failed (SignUp)", task.exception)
                                        displayToast("Lỗi đăng ký: " + task.exception?.message)
                                    }
                                }
                        } else {
                            Log.e("GoogleSignIn", "Google account is null (SignUp)")
                            displayToast("Lỗi: Không thể lấy thông tin tài khoản Google")
                        }
                    } catch (e: ApiException) {
                        Log.e("GoogleSignIn", "Google API exception (SignUp): ${e.statusCode}", e)
                        displayToast("Lỗi đăng ký Google: ${e.statusCode} - ${e.message}")
                        e.printStackTrace()
                    }
                } else {
                    Log.e("GoogleSignIn", "Account task failed (SignUp)", signInAccountTask.exception)
                    displayToast("Lỗi: Không thể lấy thông tin đăng nhập Google")
                }
            } else {
                Log.e("GoogleSignIn", "Result code not OK (SignUp): ${result.resultCode}")
            }
        }
    }

    private fun displayToast(message: String) {
        Toast.makeText(this.requireContext().applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun signUp() {
        viewModel.signUp(callback = { success, message ->
            if (success) {
                viewModel.insertDefaultCategory {
                    Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                    navigateToHome(true)
                }
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToHome(isNewUser: Boolean = true) {
        viewModel.insertDefaultCategory {
            val intent = Intent(getOwnerActivity<AuthenticationActivity>(), HomeActivity::class.java)
            getOwnerActivity<AuthenticationActivity>()?.startActivity(intent)
            getOwnerActivity<AuthenticationActivity>()?.finish()
        }
    }

    override fun openSignInGoogle() {
        try {
            val intent = googleSignInClient.signInIntent
            Log.d("GoogleSignIn", "Launching Google Sign-In intent (SignUp)")
            activityResultLauncher?.launch(intent)
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error launching Google Sign-In (SignUp)", e)
            displayToast("Lỗi khởi động đăng ký Google: ${e.message}")
        }
    }

    override fun openSignInFacebook() {
        Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
    }

    override fun openSignInPhone() {
        findNavController().navigate(R.id.sign_in_to_login_phone)
    }

    override fun openApp() {
        val intent = Intent(getOwnerActivity<AuthenticationActivity>(), HomeActivity::class.java)
        getOwnerActivity<AuthenticationActivity>()?.startActivity(intent)
    }

    override fun backSignUp() {
        findNavController().popBackStack()
    }
}