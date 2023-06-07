package com.example.instaapp.view.user_profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instaapp.R;
import com.example.instaapp.databinding.FragmentEditedBinding;

public class EditedFragment extends Fragment {

    private FragmentEditedBinding mainBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainBinding = FragmentEditedBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();

        mainBinding.closeBtn.setOnClickListener(v -> {
            getFragmentManager().popBackStack();
        });

        mainBinding.saveBtn.setOnClickListener(v -> {
            ((UserActivity) getActivity()).saveChanges();
        });


        return view;
    }
}