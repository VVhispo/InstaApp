package com.example.instaapp.view.user_profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instaapp.R;
import com.example.instaapp.databinding.FragmentEditedBinding;
import com.example.instaapp.model.User;
import com.google.gson.Gson;

public class EditedFragment extends Fragment {

    private FragmentEditedBinding mainBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainBinding = FragmentEditedBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();

        ((UserActivity) getActivity()).setEditedHeight();

        mainBinding.closeBtn.setOnClickListener(v -> {
            getFragmentManager().popBackStack();
        });

        mainBinding.saveBtn.setOnClickListener(v -> {
            ((UserActivity) getActivity()).saveChanges(mainBinding);
        });

        getParentFragmentManager()
                .setFragmentResultListener("user_data", this, (s, b) -> {
                    Gson gson = new Gson();
                    User user = gson.fromJson(b.getString("user_data"), User.class);
                    fillTextEdits(user);
                });
        return view;
    }

    public void fillTextEdits(User user){
        mainBinding.inputName.setText(user.getFirstName());
        mainBinding.inputLastName.setText(user.getLastName());
        mainBinding.inputEmail.setText(user.getEmail());
        mainBinding.bioInput.setText(user.getBio());
    }
}