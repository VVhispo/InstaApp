package com.example.instaapp.view.user_profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instaapp.R;
import com.example.instaapp.databinding.FragmentDisplayBinding;
import com.example.instaapp.model.User;

public class DisplayFragment extends Fragment {

    private FragmentDisplayBinding mainBinding;
    private User user_data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainBinding = FragmentDisplayBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();

        ((UserActivity) getActivity()).setProfilePicEditable(false);

        Fragment EditFragment = new EditedFragment();

        mainBinding.settingsBtn.setOnClickListener(v -> {
            ((UserActivity) getActivity()).replaceFragment(EditFragment);
            ((UserActivity) getActivity()).setProfilePicEditable(true);
        });

        mainBinding.logoutBtn.setOnClickListener(v -> {
            ((UserActivity)getActivity()).logout();
        });

        return view;
    }
    public void fillTextViews(User user){
        mainBinding.nameTxt.setText(user.getFullName());
        mainBinding.emailTxt.setText(user.getEmail());
        mainBinding.bioTxt.setText(user.getBio());
        user_data = user;
    }
}