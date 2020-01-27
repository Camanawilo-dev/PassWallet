package com.ecb825.passwordwalletv2;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.content.Context.ACTIVITY_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link settings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link settings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class settings extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageView back;
    private passwordActivity act;
    private SharedPreferences sharedPreferences;
    private String masterKeyAlias;
    private authPreferences preferences;
    private Context context;


    private OnFragmentInteractionListener mListener;

    public settings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment settings.
     */
    // TODO: Rename and change types and number of parameters
    public static settings newInstance(String param1, String param2) {
        settings fragment = new settings();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        act = (passwordActivity) getActivity();
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        context = this.getContext();
        final Context context = getContext();
        setHasOptionsMenu(true);
        Toolbar toolbar = v.findViewById(R.id.toolbar2);
        act.setSupportActionBar(toolbar);
        act.getSupportActionBar().setTitle("Settings");
        act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        act.getSupportActionBar().setHomeButtonEnabled(true);

        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    getContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadData();
        final RadioButton passkeybtn = v.findViewById(R.id.radioButton);
        final RadioButton fingpbtn = v.findViewById(R.id.radioButton2);
        Button cleardata = v.findViewById(R.id.formsub3);
        cleardata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context,R.style.AlertDialog)
                        .setTitle("Delete all app data?")
                        .setMessage("This will delete all saved passwords and reset the application to its factory setting")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                final Dialog dialog2 = new Dialog(act);
                                dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog2.setCancelable(false);
                                dialog2.setContentView(R.layout.passworddialog);
                                dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                final EditText password = dialog2.findViewById(R.id.passwordint);
                                Button sub = dialog2.findViewById(R.id.button6);

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
                                                ((ActivityManager)context.getSystemService(ACTIVITY_SERVICE))
                                                        .clearApplicationUserData();
                                                dialog2.dismiss();
                                            }else{
                                                Toast.makeText(context,"Invalid Password",Toast.LENGTH_SHORT).show();
                                                dialog2.dismiss();
                                            }
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        }


                                    }

                                });
                                dialog2.show();


                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setCancelable(false)
                        .show();

            }
        });

        if(!checkFing()){
            fingpbtn.setClickable(false);
        }

        if(preferences.getAuthMode().equals("fing")){
            fingpbtn.setChecked(true);
        }else{
            passkeybtn.setChecked(true);
        }

        Button saveChanges = v.findViewById(R.id.formsub5);
        final EditText pass = v.findViewById(R.id.newpass);
        final EditText pass2 = v.findViewById(R.id.newpass2);
        final RadioGroup authMethods = v.findViewById(R.id.radioGroup);


        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(act);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.passworddialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final EditText password = dialog.findViewById(R.id.passwordint);
                Button sub = dialog.findViewById(R.id.button6);

                sub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean change = false;
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
                                if(passkeybtn.isChecked()) {
                                    if(preferences.getAuthMode().equals("fing")){
                                        change = true;
                                    }
                                    sharedPreferences.edit().putString("authmode", "pass").apply();
                                }else if(fingpbtn.isChecked()){
                                    if(preferences.getAuthMode().equals("pass")){
                                        change = true;
                                    }
                                    sharedPreferences.edit().putString("authmode","fing").apply();
                                }

                                if(!pass.getText().toString().equals("")){
                                    if(pass.getText().toString().equals(pass2.getText().toString())){
                                        md5 = null;
                                        try {
                                            md5 = MessageDigest.getInstance("MD5");
                                            value = pass.getText().toString();
                                            md5.update(value.getBytes());
                                            bytes = md5.digest();
                                            sb = new StringBuilder();
                                            for(int i=0; i< bytes.length ;i++)
                                            {
                                                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                                            }
                                            value = sb.toString();
                                            sharedPreferences.edit().putString("passhash",value).apply();
                                            change = true;
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        }

                                    }else {
                                        Toast.makeText(context,"Passwords do not match",Toast.LENGTH_SHORT).show();
                                    }
                                }

                                if(change){
                                    Toast.makeText(context,"Changes Saved",Toast.LENGTH_SHORT).show();
                                    loadData();
                                }
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
            }
        });

        Button reset = v.findViewById(R.id.formsub3);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                new AlertDialog.Builder(context, R.style.AlertDialog)
                        .setTitle("Passwords do not match")
                        .setMessage("Please make sure that both password entries match")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                               final Dialog dialog3 = new Dialog(act);
                                dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog3.setCancelable(false);
                                dialog3.setContentView(R.layout.passworddialog);
                                dialog3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                final EditText password = dialog3.findViewById(R.id.passwordint);
                                Button sub = dialog3.findViewById(R.id.button6);

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
                                                user.delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    FirebaseAuth.getInstance().signOut();
                                                                }
                                                            }
                                                        });
                                                ((ActivityManager)context.getSystemService(ACTIVITY_SERVICE))
                                                        .clearApplicationUserData();
                                                dialog3.dismiss();
                                            }else{
                                                Toast.makeText(context,"Invalid Password",Toast.LENGTH_SHORT).show();
                                                dialog3.dismiss();
                                            }
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        }


                                    }

                                });
                                dialog3.show();
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.appbar2, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
//                passwords pass =new passwords();
//                FragmentTransaction ft = act.getSupportFragmentManager().beginTransaction();
//                ft.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right,R.anim.slide_in_left,R.anim.slide_out_right);
//                ft.replace(R.id.fragment_container, pass);
//                // Start the animated transition.
//                ft.commit();
//                getFragmentManager().popBackStackImmediate();
                act.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public boolean checkFing(){
        boolean result = true;
        result = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
        FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(context);
        result = fingerprintManager.isHardwareDetected();
        result = fingerprintManager.hasEnrolledFingerprints();

        return result;
    }

    public void loadData(){
        String authmode = sharedPreferences.getString("authmode","");
        String passhash = sharedPreferences.getString("passhash","");

        preferences =  new authPreferences(passhash,authmode);
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
