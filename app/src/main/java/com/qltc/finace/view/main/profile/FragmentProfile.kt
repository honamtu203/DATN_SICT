package com.qltc.finace.view.main.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FagmentProfileBinding

class FragmentProfile : BaseFragment<FagmentProfileBinding,ProfileViewModel>(), ProfileListener {
    override val layoutID: Int = R.layout.fagment_profile
    override val viewModel: ProfileViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.lifecycleOwner = this

    }
}