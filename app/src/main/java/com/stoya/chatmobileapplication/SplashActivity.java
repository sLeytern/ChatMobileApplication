package com.stoya.chatmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.stoya.chatmobileapplication.model.UserModel;
import com.stoya.chatmobileapplication.utils.AndroidUtil;
import com.stoya.chatmobileapplication.utils.FirebaseUtil;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Проверяваме дали приложението е отворено чрез натискане върху notification
        if(getIntent().getExtras() != null) {
            String userId = getIntent().getExtras().getString("userId");
            FirebaseUtil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener( e -> {
                        if(e.isSuccessful()) {
                            UserModel model = e.getResult().toObject(UserModel.class);

                            // Първо отваряме главния екран, за да има основа под ChatActivity
                            Intent mainIntent = new Intent(this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);

                            // Подаваме потребителя от notification-а и отваряме директно неговия чат
                            Intent intent = new Intent(this, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, model);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
        }
        else  {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Взимаме отговора на булевата променлива дали имаме данните на uid-то и ако го има значи сме се логнали и влизаме в приложението
                    // Проверяваме дали вече има логнат потребител във Firebase
                    if(FirebaseUtil.isLoggedIn()) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));

                    }
                    // Ако не се показват данни значи не е логнат потребителя, следователно ни ориентира към екрана за логин по тел. номер
                    else {
                        // Ако няма логнат потребител, започваме от екрана за телефонен номер
                        startActivity(new Intent(SplashActivity.this, LoginPhoneNumberActivity.class));
                    }
                    finish();
                }
            }, 2000);
        }
    }
}