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
    }

    override fun onBackClick() {
        findNavController().navigateUp()
    }

    override fun onEditProfileClick() {
        // TODO: Navigate to edit profile screen
    }

    override fun onChangePasswordClick() {
        // TODO: Navigate to change password screen
    }

    override fun onHelpClick() {
        findNavController().navigate(R.id.frag_help)
    }

    override fun onLogoutClick() {
        (activity as? HomeActivity)?.signOutFromGoogle()
    }
}