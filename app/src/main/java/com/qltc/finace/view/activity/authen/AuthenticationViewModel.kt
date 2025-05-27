package com.qltc.finace.view.activity.authen

import android.content.Context
import com.qltc.finace.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    @ApplicationContext var applicationContext: Context
) : BaseViewModel() {

}