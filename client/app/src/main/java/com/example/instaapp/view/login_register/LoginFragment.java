package com.example.instaapp.view.login_register;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instaapp.view.login_register.LoginActivity;
import com.example.instaapp.databinding.FragmentLoginBinding;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding fragmentLoginBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentLoginBinding = FragmentLoginBinding.inflate(getLayoutInflater());
        View view = fragmentLoginBinding.getRoot();

        fragmentLoginBinding.confirmBtn.setOnClickListener(v -> {
            if(((LoginActivity)getActivity()).validateFields(fragmentLoginBinding.formLl)) {
                ((LoginActivity)getActivity()).attemptLogin(fragmentLoginBinding);
            }
        });

        if(getArguments() != null){
            fragmentLoginBinding.emailInput.getEditText().setText(
              getArguments().getString("email")
            );
        }
        return view;
    }
}