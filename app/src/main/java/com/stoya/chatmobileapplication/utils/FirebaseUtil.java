package com.stoya.chatmobileapplication.utils;

import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtil {

    // Връща uid-то на текущо логнатия потребител
    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();

    }

    public static boolean isLoggedIn() {
        if(currentUserId() != null) {
            return  true;
        }
        return false;
    }

    // Връща документа на текущия потребител от колекцията users
    public static DocumentReference currentUserDetails() {
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }

    public  static CollectionReference allUserCollectionReference() {
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static DocumentReference getChatRoomReference(String chatRoomId) {
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatRoomId);
    }

    public static CollectionReference getChatRoomMessageReference(String chatRoomId) {
        return getChatRoomReference(chatRoomId).collection("chats");
    }

    // Прави еднакво id за чат стаята, независимо кой потребител започва разговора
    public static String getChatRoomId(String userId1, String userId2) {
        if(userId1.hashCode() < userId2.hashCode()) {
            return userId1+"_"+userId2;
        }
        else {
            return userId2+"_"+userId1;
        }
    }

    public static CollectionReference allChatRoomCollectionReference() {
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    // От двата userId-та в чат стаята връща документа на другия потребител
    public static DocumentReference getOtherUserFromChatRoom(List<String> userIds) {
        if(userIds.get(0).equals(FirebaseUtil.currentUserId())) {
            return allUserCollectionReference().document(userIds.get(1));
        }
        else  {
            return allUserCollectionReference().document(userIds.get(0));
        }
    }

    public static String timestampToString(Timestamp timestamp) {
        return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
    }

    public static void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    // Връща мястото в Storage, където пазим снимката на текущия потребител
    public static StorageReference getCurrentProfilePicStorageRef() {
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(FirebaseUtil.currentUserId());
    }

    // Връща мястото в Storage, където пазим снимката на друг потребител
    public static StorageReference getOtherProfilePicStorageRef(String otherUserId) {
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(otherUserId);
    }
}
