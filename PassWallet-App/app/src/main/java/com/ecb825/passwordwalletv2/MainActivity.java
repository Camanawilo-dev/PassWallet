package com.ecb825.passwordwalletv2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements enabling.OnFragmentInteractionListener{
    private FrameLayout fragmentContainer;
    private Button button;
    private FirebaseAuth mAuth;
    String code;
    private Boolean registered;
    private MainActivity activity;
    private Context context;
    private SharedPreferences sharedPreferences;
    private String masterKeyAlias;
    private authPreferences preferences;



    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);
        button = (Button) findViewById(R.id.button);

        mAuth = FirebaseAuth.getInstance();
        context =activity.getBaseContext();

        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    getBaseContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Executor executor = Executors.newSingleThreadExecutor();
        final BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this).setTitle("Fingerprint Authentication")
                .setSubtitle("Enter your Passwallett")
                .setDescription("Place your finger in the Device's fingerprint reader")
                .setNegativeButton("cancel", executor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).build();


        loadData();
        if(mAuth.getCurrentUser()!= null){
            button.setVisibility(View.GONE);
            fragmentContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    switch (preferences.getAuthMode()){
                        case "fing":
                            biometricPrompt.authenticate(new CancellationSignal(), executor, new BiometricPrompt.AuthenticationCallback() {
                                @Override
                                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                                    super.onAuthenticationSucceeded(result);
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            openNextAcr(v);
                                        }
                                    });
                                }

                                @Override
                                public void onAuthenticationFailed() {
                                    super.onAuthenticationFailed();
                                }
                            });
                            break;
                        case "pass":
                            final Dialog dialog = new Dialog(activity);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.passworddialog);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            final EditText password = dialog.findViewById(R.id.passwordint);
                            Button sub = dialog.findViewById(R.id.button6);

                            sub.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                                        String value = password.getText().toString();
                                        md5.update(value.getBytes());
                                        byte[] bytes = md5.digest();
                                        StringBuilder sb = new StringBuilder();
                                        for(int i=0; i< bytes.length ;i++)
                                        {
                                            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                                        }
                                        value = sb.toString();

                                        if (value.equals(preferences.getPassHash())){
                                            openNextAcr(v);
                                            dialog.dismiss();
                                        }else{
                                            Toast.makeText(context,"Invalid Password",Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    } catch (NoSuchAlgorithmException e) {
                                        e.printStackTrace();
                                    }


                                }

                            });
                            dialog.show();


                            break;
                        case "":
                            openNextAcr(v);
                            break;
                    }
                }
            });

        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment();
            }
        });

    }


    public  void openNextAcr(View v){
        Intent intent = new Intent(this,passwordActivity.class);
        startActivity(intent);
        finish();
    }

    public void visiblebutton() {

        button.setEnabled(true);

    }
    public void openFragment(){
        enabling fragment = enabling.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right,R.anim.slide_in_left,R.anim.slide_out_right);
        transaction.addToBackStack(null);
        button.setEnabled(false);
        transaction.add(R.id.fragment_container,fragment,"enabling_frag").commit();

    }

    @Override
    public void onFragmentInteraction() {

    }


    public void sendVerificationCode(String no) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+44" + no,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;

        }
    };
    String mVerificationId;
    public void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }
    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FragmentManager fm = getSupportFragmentManager();
                            enabling frag = (enabling) fm.findFragmentByTag("enabling_frag");
                            registered = true;
                            try {
                                saveData(context);
                            } catch (GeneralSecurityException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            frag.complete();

                        } else {

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }


                        }
                    }
                });
    }
    public void saveData(Context context) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);


        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                "shared preferences",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("registered",registered);
        editor.apply();
    }
    public void loadData(){
        String authmode = sharedPreferences.getString("authmode","");
        String passhash = sharedPreferences.getString("passhash","");

        preferences =  new authPreferences(passhash,authmode);
    }


}