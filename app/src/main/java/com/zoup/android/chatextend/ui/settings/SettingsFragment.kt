package com.zoup.android.chatextend.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.databinding.FragmentSettingsBinding
import com.zoup.android.chatextend.utils.encryptString

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(requireActivity() is MainActivity){
            (activity as MainActivity).setFavouriteMenuVisibility(false)
            (activity as MainActivity).setSureMenuVisibility(false)
        }
        viewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val apiKeyLayout = binding.apiKeyLayout
        apiKeyLayout.setOnClickListener {
            clickChangeAPIKey()
        }
        val textView: TextView = binding.tvKey
        viewModel.text.observe(viewLifecycleOwner) {
            textView.text = encryptString(it, 4, 4)
//            Constants.DEEPSEEK_API_KEY = it
        }
        return root
    }

    fun clickChangeAPIKey() {
        val input = EditText(requireContext())
        input.hint = "请输入API Key"

        AlertDialog.Builder(requireContext())
//            .setTitle("新增分类")
            .setView(input)
            .setPositiveButton("确定") { dialog, _ ->
                val inputKey = input.text.toString()
                if (inputKey.isNotEmpty()) {
                    viewModel._text.value = inputKey
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}