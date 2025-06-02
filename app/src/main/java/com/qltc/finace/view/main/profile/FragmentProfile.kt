package com.qltc.finace.view.main.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FagmentProfileBinding
import com.qltc.finace.view.activity.home.HomeActivity

class FragmentProfile : BaseFragment<FagmentProfileBinding,ProfileViewModel>(), ProfileListener {
    override val layoutID: Int = R.layout.fagment_profile
    override val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.lifecycleOwner = this
        viewBinding.listener = this
        viewBinding.viewModel = viewModel
    }

    override fun onResume() {
        super.onResume()
        // Update user profile when fragment becomes visible
        viewModel.updateUserProfile()
    }

    override fun onBackClick() {
        findNavController().navigate(R.id.fag_home)
    }

    override fun onEditProfileClick() {
        findNavController().navigate(R.id.frg_change_infor)
    }

    override fun onChangePasswordClick() {
        findNavController().navigate(R.id.frg_change_password)
    }

    override fun onHelpClick() {
        findNavController().navigate(R.id.frag_help)
    }

    override fun onLogoutClick() {
        (activity as? HomeActivity)?.signOutFromGoogle()
    }
}