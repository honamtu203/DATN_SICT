package com.qltc.finace.view.main.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentHelperBinding

class FragmentHelper : BaseFragment<FragmentHelperBinding, HelperViewModel>(), HelperListener {
    override val layoutID: Int = R.layout.fragment_helper
    override val viewModel: HelperViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.lifecycleOwner = this
        viewBinding.listener = this
    }

    override fun onBackClick() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            // Fallback nếu popBackStack() gặp lỗi
            try {
                findNavController().popBackStack(R.id.frag_profile, false)
            } catch (e2: Exception) {
                try {
                    // Phương pháp thay thế cuối cùng
                    findNavController().navigate(R.id.frag_profile)
                } catch (e3: Exception) {
                    // Ghi log lỗi
                    e3.printStackTrace()
                }
            }
        }
    }

    override fun onFacebookHelpClick() {
        openUrlWithChooser("https://www.facebook.com/Ho.Nam.Tu.0711/", "Chọn ứng dụng để mở Facebook")
    }

    override fun onMailHelpClick() {
        openEmailWithChooser("honamtu203.doan@gmail.com")
    }

    override fun onZaloHelpClick() {
        openUrlWithChooser("https://zalo.me/0398812298", "Chọn ứng dụng để mở Zalo")
    }

    override fun onPhoneHelpClick() {
        openPhoneDialerWithChooser("0398812298")
    }

//    private fun openUrlWithChooser(url: String, chooserTitle: String) {
//        try {
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//
//            if (intent.resolveActivity(requireActivity().packageManager) != null) {
//                val chooserIntent = Intent.createChooser(intent, chooserTitle)
//                startActivity(chooserIntent)
//            } else {
//                showNoAppToast("trình duyệt")
//            }
//        } catch (e: Exception) {
//            showErrorToast()
//        }
//    }

    private fun openUrlWithChooser(url: String, chooserTitle: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val chooserIntent = Intent.createChooser(intent, chooserTitle)
            startActivity(chooserIntent)
        } catch (e: Exception) {
            showNoAppToast("trình duyệt hoặc ứng dụng hỗ trợ")
        }
    }

//    private fun openEmailWithChooser(emailAddress: String) {
//        try {
//            val intent = Intent(Intent.ACTION_SENDTO).apply {
//                data = Uri.parse("mailto:$emailAddress")
//                putExtra(Intent.EXTRA_SUBJECT, "Yêu cầu trợ giúp từ ứng dụng QLTC")
//                putExtra(Intent.EXTRA_TEXT, "Xin chào,\n\nTôi cần hỗ trợ về ứng dụng QLTC.\n\nCảm ơn!")
//            }
//
//            if (intent.resolveActivity(requireActivity().packageManager) != null) {
//                val chooserIntent = Intent.createChooser(intent, "Chọn ứng dụng email để gửi")
//                startActivity(chooserIntent)
//            } else {
//                showNoAppToast("ứng dụng email")
//            }
//        } catch (e: Exception) {
//            showErrorToast()
//        }
//    }

    private fun openEmailWithChooser(emailAddress: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$emailAddress")
                putExtra(Intent.EXTRA_SUBJECT, "Yêu cầu trợ giúp từ ứng dụng QLTC")
                putExtra(Intent.EXTRA_TEXT, "Xin chào,\n\nTôi cần hỗ trợ về ứng dụng QLTC.\n\nCảm ơn!")
            }

            val chooser = Intent.createChooser(intent, "Chọn ứng dụng email để gửi")
            startActivity(chooser)

        } catch (e: Exception) {
            showErrorToast()
        }
    }

//    private fun openPhoneDialerWithChooser(phoneNumber: String) {
//        try {
//            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
//
//            if (intent.resolveActivity(requireActivity().packageManager) != null) {
//                val chooserIntent = Intent.createChooser(intent, "Chọn ứng dụng để gọi điện")
//                startActivity(chooserIntent)
//            } else {
//                showNoAppToast("ứng dụng gọi điện")
//            }
//        } catch (e: Exception) {
//            showErrorToast()
//        }
//    }

    private fun openPhoneDialerWithChooser(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            val chooser = Intent.createChooser(intent, "Chọn ứng dụng để gọi điện")
            startActivity(chooser)

        } catch (e: Exception) {
            showErrorToast()
        }
    }

    private fun showNoAppToast(appType: String) {
        Toast.makeText(
            requireContext(),
            "Không tìm thấy $appType phù hợp trên thiết bị này",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showErrorToast() {
        Toast.makeText(
            requireContext(),
            "Có lỗi xảy ra. Vui lòng thử lại sau",
            Toast.LENGTH_SHORT
        ).show()
    }
} 