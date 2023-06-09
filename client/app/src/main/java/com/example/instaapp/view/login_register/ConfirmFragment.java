package com.example.instaapp.view.login_register;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instaapp.R;
import com.example.instaapp.databinding.FragmentConfirmBinding;

public class ConfirmFragment extends Fragment {

    private FragmentConfirmBinding fragmentConfirmBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentConfirmBinding = FragmentConfirmBinding.inflate(getLayoutInflater());
        View view = fragmentConfirmBinding.getRoot();



        return view;
    }
}