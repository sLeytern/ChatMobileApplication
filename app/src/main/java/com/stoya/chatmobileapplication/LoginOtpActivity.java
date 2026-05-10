package com.stoya.chatmobileapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.stoya.chatmobileapplication.utils.AndroidUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {

    String phoneNumber;
    EditText otpInput;
    Button otpConfirm;
    TextView resendOtp;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Long timeoutSeconds = 60L;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        otpInput = findViewById(R.id.login_otp);
        otpConfirm = findViewById(R.id.confirm_otp_btn);
        resendOtp = findViewById(R.id.resend_otp_textview);

        // Тук тросто взимаме подадената стойснот по клюя "phone" и я вкарваме в локална променлива за да покажем визуализация на екран
        phoneNumber = getIntent().getExtras().getString("phone");
        Toast.makeText(getApplicationContext(), phoneNumber,Toast.LENGTH_LONG).show();

        // Изпращаме първи OTP код към въведения телефонен номер
        sendOtp(phoneNumber, false);

        otpConfirm.setOnClickListener(e -> {
            String enteredOtp = otpInput.getText().toString();
            // Правим credential от въведения OTP код, за да го проверим във Firebase
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp);
            signIn(credential);
        });

        resendOtp.setOnClickListener(e -> {
            sendOtp(phoneNumber, true);
        });
    }

    // Разглеждаме дали
    void sendOtp(String phoneNumber, boolean isResend){
        // Функция за изпращане или повторно изпращане на OTP код чрез Firebase Authentication
        startRecendTimer();
        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    // Ако Firebase автоматично разпознае кода, директно логваме потребителя
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signIn(phoneAuthCredential);
                    }

                    @Override
                    // При грешка в OTP процеса показваме съобщение на потребителя
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        AndroidUtil.showToast(getApplicationContext(), "OTP verification failed");
                    }

                    @Override
                    // Запазваме verificationCode, за да можем после да сравним въведения код
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationCode = s;
                        resendingToken = forceResendingToken;
                        AndroidUtil.showToast(getApplicationContext(), "OTP sent successfully");
                    }
                });

        // Ако потребителят поиска нов код, използваме resendingToken от Firebase
        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        }
        else{
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    void signIn(PhoneAuthCredential phoneAuthCredential){
        // Логиката за голване и преминаване към следващо Activity
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // При правилен верификационен код отиваме при username Activity
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginOtpActivity.this, LoginUsernameActivity.class);
                    intent.putExtra("phone", phoneNumber);
                    startActivity(intent);
                }
                // При грешен верификационен код излиза съобщението по-долу
                else{
                    AndroidUtil.showToast(getApplicationContext(), "Wrong OTP code");
                }
            }
        });
    }

    void startRecendTimer(){
        // Заключваме текста за препращане на нов код, докато не ни потрябва.
        resendOtp.setEnabled(false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Намаляваме секундите на таймера постепенно, докато не свърши таймера и ако това се случи, се рестартира от начало и бутона вече е кликаем.
                timeoutSeconds--;
                resendOtp.setText("Resend OTP in "+ timeoutSeconds +" seconds");
                if(timeoutSeconds <= 0){
                    timeoutSeconds = 60L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        resendOtp.setEnabled(true);
                    });
                }
            }
        }, 0, 1000);
    }
}