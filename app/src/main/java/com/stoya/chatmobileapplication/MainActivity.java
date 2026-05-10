package com.stoya.chatmobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.stoya.chatmobileapplication.utils.FirebaseUtil;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageButton searchButton;
    ChatFragment chatFragment;
    ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Създаваме фрагментите, които ще се показват в главния екран
        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();

        bottomNavigationView = findViewById(R.id.bot_navigation);
        searchButton = findViewById(R.id.main_search_btn);

        searchButton.setOnClickListener(e -> {
            // При кликане на снимката на търсачка отиваме към ново Activity за търсене на потребители
            startActivity(new Intent(MainActivity.this, SearchUserActivity.class));
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Правим проверка за това кой фрагмент е избран, за да можем да преминем към съответния екран
                if(menuItem.getItemId() == R.id.menu_chat) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
                }
                if(menuItem.getItemId() == R.id.menu_profile) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
                }
                return true;
            }
        });

        // По подразбиране оставяме да бъде избран фрагмента за чатове
        bottomNavigationView.setSelectedItemId(R.id.menu_chat);

        getFCMToken();
    }

    void getFCMToken() {
        // Взимаме FCM token-а на устройството и го записваме към профила на потребителя
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener( e -> {
            if(e.isSuccessful()) {
                String token = e.getResult();
                FirebaseUtil.currentUserDetails().update("fcmToken", token);
            }
        });
    }
}