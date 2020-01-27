package com.ecb825.passwordwalletv2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.binary.Hex.encodeHexString;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link sendstep1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link sendstep1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class sendstep1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Button submit;
    private EditText numbox;
    private DatabaseReference mDatabase;
    private SurfaceView surfaceView;
    private  CameraSource cameraSource;
    private  BarcodeDetector barcodeDetector;
    private Group g1;
    private  Group g2;
    private ConstraintLayout rl;
    private  ImageView complete;
    private  ImageView ico;
    private  TextView scantxt;
    private  Button cancelbut;
    private  passwordActivity act;
    private String key;
    private RSACipher rsakey;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public sendstep1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment sendstep1.
     */
    // TODO: Rename and change types and number of parameters
    public static sendstep1 newInstance(String param1, String param2) {
        sendstep1 fragment = new sendstep1();
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
    Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sendstep1, container, false);
        final View g = v;
        numbox = v.findViewById(R.id.numbox);
        submit = v.findViewById(R.id.button);
        context= this.getContext();
        complete = v.findViewById(R.id.completeico);
        ico = v.findViewById(R.id.scanico);
        scantxt = v.findViewById(R.id.scantext);

        cancelbut = v.findViewById(R.id.cancels);
        act = (passwordActivity)getActivity();

        if (ContextCompat.checkSelfPermission(act, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(act, new String[] {Manifest.permission.CAMERA}, 100);
        }

        try {
            rsakey = new RSACipher();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        cancelbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> updated = new HashMap<>();
                updated.put("appid", null);
                updated.put("appk",null);
                FirebaseDatabase.getInstance().getReference().child("extensions").child(numbox.getText().toString()).updateChildren(updated);
                act.onBackPressed();
            }
        });

        rl = v.findViewById(R.id.scan);

        surfaceView = v.findViewById(R.id.cameraprv);
        g1 = v.findViewById(R.id.dif);
        g2 = v.findViewById(R.id.gp);

        barcodeDetector = new BarcodeDetector.Builder(this.getContext())
                .setBarcodeFormats(Barcode.QR_CODE).build();

        cameraSource = new CameraSource.Builder(this.getContext(), barcodeDetector).setAutoFocusEnabled(true)
                .setRequestedPreviewSize(230, 213).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                rl.post(new Runnable() {
                    @Override
                    public void run() {
                        rl.setBackgroundResource(R.color.completebg);
                        ico.setVisibility(View.GONE);
                        scantxt.setVisibility(View.GONE);
                        cancelbut.setVisibility(View.GONE);
                        complete.setVisibility(View.VISIBLE);
                        new AlertDialog.Builder(context,R.style.AlertDialog)
                                .setTitle("Password Sent")
                                .setMessage("Your password has now been sent to the target extension")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference().child("extensions").child(numbox.getText().toString())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        // Get user information

                                                        SecureRandom randomSecureRandom = new SecureRandom();
                                                        byte[] ivbyte = new byte[16];
                                                        randomSecureRandom.nextBytes(ivbyte);

                                                        String rsapkey = dataSnapshot.child("extpk").getValue().toString();

                                                        String sname = act.getItem().getLine1();
                                                        String uname = act.getItem().getLine2();
                                                        String epass = act.getItem().getPass();

                                                        Map<String, Object> updated = new HashMap<>();

                                                        String keyValue = null;
                                                        keyValue = key;

                                                        try {
                                                            keyValue = rsakey.decrypt(key);
                                                        } catch (NoSuchAlgorithmException e) {
                                                            e.printStackTrace();
                                                        } catch (NoSuchPaddingException e) {
                                                            e.printStackTrace();
                                                        } catch (InvalidKeyException e) {
                                                            e.printStackTrace();
                                                        } catch (IllegalBlockSizeException e) {
                                                            e.printStackTrace();
                                                        } catch (BadPaddingException e) {
                                                            e.printStackTrace();
                                                        }

                                                        System.out.println(keyValue);

                                                        SecretKeyFactory factory = null;
                                                        try {
                                                            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                                                        } catch (NoSuchAlgorithmException e) {
                                                            e.printStackTrace();
                                                        }
                                                        KeySpec spec = null;
                                                        spec = new PBEKeySpec(keyValue.toCharArray(), ivbyte,
                                                                1000, 128);

                                                        Key key = null;
                                                        try {
                                                            key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
                                                        } catch (InvalidKeySpecException e) {
                                                            e.printStackTrace();
                                                        }
                                                        Cipher c = null;
                                                        try {
                                                            c = Cipher.getInstance("AES/CBC/PKCS5Padding");
                                                        } catch (NoSuchAlgorithmException e) {
                                                            e.printStackTrace();
                                                        } catch (NoSuchPaddingException e) {
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivbyte));
                                                        } catch (InvalidAlgorithmParameterException e) {
                                                            e.printStackTrace();
                                                        } catch (InvalidKeyException e) {
                                                            e.printStackTrace();
                                                        }

                                                        byte[] encVal = new byte[0];
                                                        byte[] encVal2 = new byte[0];

                                                        try {
                                                            encVal = c.doFinal(uname.getBytes());
                                                            encVal2 = c.doFinal(epass.getBytes());
                                                        } catch (BadPaddingException e) {
                                                            e.printStackTrace();
                                                        } catch (IllegalBlockSizeException e) {
                                                            e.printStackTrace();
                                                        }

                                                        String base64EncodedEncryptedData = "error";
                                                        String base64EncodedEncryptedData2 = "error";
                                                        String ivhex = encodeHexString(ivbyte);

                                                        try {
                                                            base64EncodedEncryptedData = new String(Base64.encodeBase64(encVal));
                                                            base64EncodedEncryptedData2 = new String(Base64.encodeBase64(encVal2));
                                                            base64EncodedEncryptedData = rsakey.encrypt(base64EncodedEncryptedData,rsakey.stringToPublicKey(rsapkey));
                                                            base64EncodedEncryptedData2 = rsakey.encrypt(base64EncodedEncryptedData2,rsakey.stringToPublicKey(rsapkey));
                                                        } catch (NoSuchAlgorithmException e) {
                                                            e.printStackTrace();
                                                        } catch (NoSuchPaddingException e) {
                                                            e.printStackTrace();
                                                        } catch (InvalidKeyException e) {
                                                            e.printStackTrace();
                                                        } catch (IllegalBlockSizeException e) {
                                                            e.printStackTrace();
                                                        } catch (BadPaddingException e) {
                                                            e.printStackTrace();
                                                        }

                                                        updated.put("sname",sname);
                                                        updated.put("suname",base64EncodedEncryptedData);
                                                        updated.put("spass",base64EncodedEncryptedData2);
                                                        updated.put("iv",ivhex);


                                                        FirebaseDatabase.getInstance().getReference().child("extensions").child(numbox.getText().toString()).updateChildren(updated);

                                                        g1.setVisibility(View.GONE);
                                                        g2.setVisibility(View.VISIBLE);
                                                        hideKeyboardFrom(context,g);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });



                                        act.onBackPressed();
                                        cameraSource.stop();

                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                });
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> qrCode = detections.getDetectedItems();

                if (qrCode.size() != 0){
                    key = qrCode.valueAt(0).displayValue;
                    System.out.println(key);
                    barcodeDetector.release();
                }

            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    FirebaseDatabase.getInstance().getReference().child("extensions").child(numbox.getText().toString()).child("uid")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.getValue() != null) {

                                        Map<String, Object> updated = new HashMap<>();

                                        updated.put("uid", dataSnapshot.getValue().toString());
                                        updated.put("appid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        try {
                                            updated.put("appk", rsakey.getPublicKey("pkcs8-pem"));
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        } catch (NoSuchPaddingException e) {
                                            e.printStackTrace();
                                        } catch (InvalidKeyException e) {
                                            e.printStackTrace();
                                        } catch (IllegalBlockSizeException e) {
                                            e.printStackTrace();
                                        } catch (BadPaddingException e) {
                                            e.printStackTrace();
                                        }

                                        FirebaseDatabase.getInstance().getReference().child("extensions").child(numbox.getText().toString()).updateChildren(updated);

                                        g1.setVisibility(View.GONE);
                                        g2.setVisibility(View.VISIBLE);
                                        hideKeyboardFrom(context, g);
                                    }else {
                                        Toast.makeText(context,"Invalid Extension Code",Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }catch(Exception e) {
                    Toast.makeText(context,"Invalid Extension Code",Toast.LENGTH_SHORT).show();
                }
            }
        });


        return v;
    }

    public  void goback(){


    }
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public byte[] hex(String str) throws DecoderException {
        return decodeHex(str);
    }

    public static String createRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.toString();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;

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
