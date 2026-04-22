package com.stoya.chatmobileapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.stoya.chatmobileapplication.model.UserModel;
import com.stoya.chatmobileapplication.utils.AndroidUtil;
import com.stoya.chatmobileapplication.utils.FirebaseUtil;

public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText loginUsername;
    EditText phoneInput;
    Button updateProfileBtn;
    TextView logoutBtn;
    UserModel currentUserModel;

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = view.findViewById(R.id.profile_image_view);
        loginUsername = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phone);
        updateProfileBtn = view.findViewById(R.id.update_profile_btn);
        logoutBtn = view.findViewById(R.id.logout_btn);

        getUserData();
        updateProfileBtn.setOnClickListener( e -> {
            updateBtnClick();
        });

        logoutBtn.setOnClickListener( e -> {
            FirebaseUtil.logout();
            Intent intent = new Intent(getContext(), SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    void updateBtnClick() {
        String newUsername = loginUsername.getText().toString();
        if(newUsername.isEmpty() || newUsername.length() <= 3) {
            loginUsername.setError("Username length should be at least 4 characters");
            return;
        }

        currentUserModel.setUsername(newUsername);
        updateToFirestore();
    }

    void updateToFirestore() {
        FirebaseUtil.currentUserDetails().set(currentUserModel)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        AndroidUtil.showToast(getContext(), "updated successfully");
                    }
                    else {
                        AndroidUtil.showToast(getContext(), "updated failed");
                    }
                });
    }

    void getUserData() {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            currentUserModel = task.getResult().toObject(UserModel.class);
            loginUsername.setText(currentUserModel.getUsername());
            phoneInput.setText(currentUserModel.getPhone());
        });
    }
}