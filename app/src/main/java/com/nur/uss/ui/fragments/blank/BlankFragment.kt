package com.nur.uss.ui.fragments.blank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.nur.uss.R
import com.nur.uss.databinding.FragmentBlankBinding
import com.nur.uss.databinding.FragmentSingInBinding
import com.nur.uss.ui.fragments.singin.SingInViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlankFragment : Fragment() {

    private val binding by viewBinding(FragmentBlankBinding::bind)
    private val viewModel: BlankViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }
}