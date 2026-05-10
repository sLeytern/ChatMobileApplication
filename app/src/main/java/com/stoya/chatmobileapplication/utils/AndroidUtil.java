package com.stoya.chatmobileapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stoya.chatmobileapplication.model.UserModel;

public class AndroidUtil {
    // Семпла функция за показване на кратко съобщение на екрана
    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Подаваме данните на потребител към друго Activity чрез Intent
    public static void passUserModelAsIntent (Intent intent, UserModel model) {
        intent.putExtra("username", model.getUsername());
        intent.putExtra("phone", model.getPhone());
        intent.putExtra("userId", model.getUserId());
        intent.putExtra("fcmToken", model.getFcmToken());

    }

    // Взимаме потребителските данни обратно от Intent и ги връщаме като UserModel
    public static  UserModel getUserModelFromIntent (Intent intent) {
        UserModel userModel = new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setFcmToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }

    // С Glide зареждаме снимката и я правим кръгла
    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView) {
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }
}
