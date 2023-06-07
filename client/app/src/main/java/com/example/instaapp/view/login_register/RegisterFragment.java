package com.example.instaapp.view.login_register;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instaapp.view.login_register.LoginActivity;
import com.example.instaapp.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding fragmentRegisterBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentRegisterBinding = FragmentRegisterBinding.inflate(getLayoutInflater());
        View view = fragmentRegisterBinding.getRoot();

        fragmentRegisterBinding.confirmBtn.setOnClickListener(v -> {
            if(((LoginActivity)getActivity()).validateFields(fragmentRegisterBinding.formLl)) {
                ((LoginActivity)getActivity()).attemptRegister(fragmentRegisterBinding);
            }
        });
        return view;
    }
}