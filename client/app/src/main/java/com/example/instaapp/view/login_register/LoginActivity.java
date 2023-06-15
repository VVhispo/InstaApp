package com.example.instaapp.view.login_register;
import com.example.instaapp.api.UsersAPI;
import com.example.instaapp.databinding.FragmentLoginBinding;
import com.example.instaapp.databinding.FragmentRegisterBinding;
import com.example.instaapp.model.LoginData;
import com.example.instaapp.model.Token;
import com.example.instaapp.model.RegisterData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Selection;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.instaapp.R;
import com.example.instaapp.databinding.ActivityLoginBinding;
import com.example.instaapp.view.main.HomeActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = main.getRoot();
        setContentView(view);

        Fragment login = new LoginFragment();
        replaceFragment(login);
        Fragment register = new RegisterFragment();

        SharedPreferences sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        String current_user = sharedPref.getString("current_user_token", "none");
        if(!Objects.equals(current_user, "none")){
            switchActivityToMain();
        }


        main.toggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.login:
                        replaceFragment(login);
                        main.login.setTypeface(null, Typeface.BOLD);
                        main.register.setTypeface(null, Typeface.NORMAL);
                        break;
                    case R.id.register:
                        replaceFragment(register);
                        main.login.setTypeface(null, Typeface.NORMAL);
                        main.register.setTypeface(null, Typeface.BOLD);
                        break;
                }
            }
        });
        main.settingsBtn.setOnClickListener(v -> {
            showBottomSheetSettings();
        });
    }

    public void attemptRegister(FragmentRegisterBinding regBinding){
         SharedPreferences sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE);
         String ip = sharedPref.getString("ip", "http://192.168.1.106:3000");
         Retrofit retrofit = new Retrofit.Builder()
                 .baseUrl(ip)
                 .addConverterFactory(GsonConverterFactory.create())
                 .build();
         UsersAPI uAPI = retrofit.create(UsersAPI.class);

        RegisterData data = new RegisterData(
            regBinding.nameInput.getEditText().getText().toString(),
            regBinding.lastnameInput.getEditText().getText().toString(),
            regBinding.emailInput.getEditText().getText().toString(),
            regBinding.passwordInput.getEditText().getText().toString()
        );

         Call<Token> call = uAPI.register(data);

         call.enqueue(new Callback<Token>(){
             @Override
             public void onResponse(Call<Token> call, Response<Token> response) {
                if(response.isSuccessful()){
                    Token token = response.body();
                    regBinding.nameInput.setEnabled(false);
                    regBinding.lastnameInput.setEnabled(false);
                    regBinding.emailInput.setEnabled(false);
                    regBinding.passwordInput.setEnabled(false);


                    regBinding.confirmLink.setVisibility(View.VISIBLE);
                    regBinding.confirmLink.setOnClickListener(v -> {
                        Intent confirmBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(ip + "/api/users/confirm/" + token.getToken()));
                        startActivity(confirmBrowser);
                        main.toggle.check(R.id.login);

                        Fragment login = new LoginFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("email", regBinding.emailInput.getEditText().getText().toString());
                        login.setArguments(bundle);
                        replaceFragment(login);

                    });
                }else{
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String error_msg = jObjError.getString("error");
                        regBinding.emailInput.setError(error_msg);
                        regBinding.emailInput.setErrorTextAppearance(R.style.AppTheme_TextAppearance_Error);

                    } catch (Exception e) {
                        Log.d("XXX", e.getMessage());
                    }

                }
             }

             @Override
             public void onFailure(Call<Token> call, Throwable t) {
                 Log.d("XXX", t.toString());
             }
         });

    }

    public void attemptLogin(FragmentLoginBinding logBinding){
        SharedPreferences sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("ip", "http://192.168.1.106:3000");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsersAPI uAPI = retrofit.create(UsersAPI.class);

        LoginData data = new LoginData(
            logBinding.emailInput.getEditText().getText().toString(),
            logBinding.passwordInput.getEditText().getText().toString()
        );

        Call<Token> call = uAPI.login(data);

        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if(response.isSuccessful()){
                    Token token = response.body();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("current_user_token", token.getToken());
                    editor.putString("user_id", token.getUid());
                    editor.apply();
                    switchActivityToMain();
                }else{
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        String error_msg = jObjError.getString("error");
                        if(error_msg.toLowerCase(Locale.ROOT).contains("password")){
                            logBinding.passwordInput.setError(error_msg);
                            logBinding.passwordInput.setErrorTextAppearance(R.style.AppTheme_TextAppearance_Error);
                        }else if(error_msg.toLowerCase(Locale.ROOT).contains("user")){
                            logBinding.emailInput.setError(error_msg);
                            logBinding.emailInput.setErrorTextAppearance(R.style.AppTheme_TextAppearance_Error);
                        }else{
                            logBinding.emailInput.setError(error_msg);
                            logBinding.passwordInput.setErrorTextAppearance(R.style.AppTheme_TextAppearance_Error);
                            logBinding.emailInput.setErrorTextAppearance(R.style.AppTheme_TextAppearance_Error);
                        }
                        logBinding.passwordInput.getEditText().setText("");

                    } catch (Exception e) {
                        Log.d("XXX", e.getMessage());
                    }
                }

            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Log.d("XXX", t.toString());
            }
        });
    }

    public void switchActivityToMain(){
        Intent myIntent = new Intent(this, HomeActivity.class);
        startActivity(myIntent);
        finish();
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.form, fragment)
                .commit();
    }

    public Boolean validateFields(ViewGroup parent){
        Boolean correct = true;
        for(int i=0; i<parent.getChildCount(); i++){
            final View child = parent.getChildAt(i);
            if(child instanceof TextInputLayout){
                TextInputLayout input = (TextInputLayout)child;
                String value = String.valueOf(input.getEditText().getText());
                if(value.length() == 0) {
                    input.setError("Required");
                    input.setErrorTextAppearance(R.style.AppTheme_TextAppearance_Error);
                    correct = false;
                }
                else input.setErrorEnabled(false);
            }else if(child instanceof ViewGroup){
                validateFields((ViewGroup) child);
            }
        }
        return correct;
    }

    public void showBottomSheetSettings(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.change_ip_bottom_sheet);
        EditText input = bottomSheetDialog.findViewById(R.id.input_ip);
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        imm.hideSoftInputFromWindow(main.mainLayout.getWindowToken(), 0);
                    }
                }, 250);
            }
        });

        SharedPreferences sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("ip", "http://192.168.1.106:3000");
        input.setText(ip);
        Selection.setSelection(input.getText(), input.length());

        bottomSheetDialog.show();

        bottomSheetDialog.findViewById(R.id.saveBtn).setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("ip", String.valueOf(input.getText()));
            editor.apply();
            bottomSheetDialog.dismiss();
        });
    }
}