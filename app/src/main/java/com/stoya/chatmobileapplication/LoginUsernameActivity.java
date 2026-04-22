package com.stoya.chatmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.stoya.chatmobileapplication.model.UserModel;
import com.stoya.chatmobileapplication.utils.FirebaseUtil;

public class LoginUsernameActivity extends AppCompatActivity {

    EditText loginUsername;
    Button loginBtn;
    String phoneNumber;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_username);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginUsername = findViewById(R.id.login_username);
        loginBtn = findViewById(R.id.login_btn);

        phoneNumber = getIntent().getExtras().getString("phone");
        getUsername();

        loginBtn.setOnClickListener(e -> {
            setUsername();
        });
    }

    void setUsername() {
        String username = loginUsername.getText().toString();
        if(username.isEmpty() || username.length() <= 3) {
            loginUsername.setError("Username length should be at least 4 characters");
            return;
        }

        if(userModel != null) {
            userModel.setUsername(username);
        }
        else {
            userModel = new UserModel(phoneNumber, username, Timestamp.now(), FirebaseUtil.currentUserId());
        }

        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Intent intent = new Intent(LoginUsernameActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    void getUsername() {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                     userModel = task.getResult().toObject(UserModel.class);
                    if(userModel != null) {
                        loginUsername.setText(userModel.getUsername());
                    }
                }
            }
        });
    }
}