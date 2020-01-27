package com.ecb825.passwordwalletv2;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


public class enabling extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private TextView keypad0;
    private TextView keypad1;
    private TextView keypad2;
    private TextView keypad3;
    private TextView keypad4;
    private TextView keypad5;
    private TextView keypad6;
    private TextView keypad7;
    private TextView keypad8;
    private TextView keypad9;
    private TextView keypadclear;
    private TextView keypadback;
    private ImageView field;
    private TextView txt;

    private TextView txt1;
    private TextView txt2;

    private ImageView logo;
    private ImageView logo1;

    private TableLayout table;

    private StringBuilder mPasswordField = new StringBuilder();
    private StringBuilder code = new StringBuilder();

    private TextView onscreen;

    private Group mode1;
    private Group mode2;
    private Group mode3;


    private int mode = 1;

    public enabling() {
        // Required empty public constructor
    }
    public static enabling newInstance() {
        enabling fragment = new enabling();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = this.getContext();
         View view = inflater.inflate(R.layout.fragment_enabling, container, false);

         mode1 = (Group) view.findViewById(R.id.mode1);
         mode2 = (Group) view.findViewById(R.id.mode2);
         mode3 = (Group) view.findViewById(R.id.mode3);

        logo1 = (ImageView) view.findViewById(R.id.logo5);
         field = (ImageView) view.findViewById(R.id.entry5);
         txt2 = (TextView) view.findViewById(R.id.enterpass5);

        logo = (ImageView) view.findViewById(R.id.logo2);
        txt = (TextView) view.findViewById(R.id.textView);
        txt1 = (TextView) view.findViewById(R.id.enterpass);

        table = (TableLayout) view.findViewById(R.id.keyboard);


        keypad0 = (TextView) view.findViewById(R.id.t9_key_0);
         keypad0.setOnClickListener(this);
         keypad1 = (TextView) view.findViewById(R.id.t9_key_1);
         keypad1.setOnClickListener(this);
         keypad2 = (TextView) view.findViewById(R.id.t9_key_2);
         keypad2.setOnClickListener(this);
         keypad3 = (TextView) view.findViewById(R.id.t9_key_3);
         keypad3.setOnClickListener(this);
         keypad4 = (TextView) view.findViewById(R.id.t9_key_4);
         keypad4.setOnClickListener(this);
         keypad5 = (TextView) view.findViewById(R.id.t9_key_5);
         keypad5.setOnClickListener(this);
         keypad6 = (TextView) view.findViewById(R.id.t9_key_6);
         keypad6.setOnClickListener(this);
         keypad7 = (TextView) view.findViewById(R.id.t9_key_7);
         keypad7.setOnClickListener(this);
         keypad8 = (TextView) view.findViewById(R.id.t9_key_8);
         keypad8.setOnClickListener(this);
         keypad9 = (TextView) view.findViewById(R.id.t9_key_9);
         keypad9.setOnClickListener(this);
         keypadback = (TextView) view.findViewById(R.id.t9_key_backspace);
         keypadback.setOnClickListener(this);
         keypadclear = (TextView) view.findViewById(R.id.t9_key_clear);
         keypadclear.setOnClickListener(this);
         onscreen = (TextView) view.findViewById(R.id.textView5);
        Button submit = (Button) view.findViewById(R.id.buttonsub);
         submit.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(mPasswordField.length() == 11){
                     mode1.setVisibility(View.GONE);
                     mode2.setVisibility(View.VISIBLE);
                     ((MainActivity)getActivity()).sendVerificationCode(mPasswordField.substring(1,11));
                     mode = 2;
                 }else{
                     Toast toast=Toast.makeText(context,"Number invalid",Toast.LENGTH_SHORT);
                     toast.setMargin(50,50);
                     toast.show();
                 }
             }
         });

        return view;
    }

    public void onClick(View v) {
        if (v.getTag() != null && "number_button".equals(v.getTag())) {
            switch(mode) {
                case 1:
                    if (mPasswordField.length() != 11) {
                        mPasswordField.append(((TextView) v).getText());
                        setScreenText();
                    }
                    break;
                case 2:
                    if (code.length() != 6) {
                        code.append(((TextView) v).getText());
                        setScreenText();
                    }
                    break;
            }
            return;
        }
        switch (v.getId()) {
            case R.id.t9_key_clear: { // handle clear button

                switch(mode) {
                    case 1:
                        mPasswordField.delete(0,mPasswordField.length());
                        setScreenText();
                        break;
                    case 2:
                        code.delete(0,code.length());
                        setScreenText();
                        break;
                }
            }
            break;
            case R.id.t9_key_backspace: { // handle backspace button
                // delete one character
                int charCount;
                if (mode == 1) {
                    charCount = mPasswordField.length();
                } else {
                    charCount = code.length();
                }

                if (code.length() == 0) {

                    mode2.setVisibility(View.GONE);
                    mode1.setVisibility(View.VISIBLE);
                    mode = 1;
                }
                if (charCount > 0) {
                    if (mode == 1) {
                        mPasswordField.delete(charCount - 1, charCount);
                    }
                    if (mode == 2) {
                        code.delete(charCount - 1, charCount);
                    }
                }

            }
            setScreenText();
            break;
        }
    }

    public void setScreenText(){
        StringBuilder text = new StringBuilder();

        if(mode == 1) {
            for (int i = 0; i < 11; i++) {
                Log.d("ERROR", "hey => " + mPasswordField.length());
                if (mPasswordField.length() > i) {
                    char number = mPasswordField.charAt(i);
                    text.append(number + " ");
                } else {
                    text.append(" _");
                }
            }
        }else if(mode == 2){
            switch(code.length()){
                case 0:
                    field.setImageResource(R.drawable.pass0);
                    break;
                case 1:
                    field.setImageResource(R.drawable.pass1);
                    break;
                case 2:
                    field.setImageResource(R.drawable.pass2);
                    break;
                case 3:
                    field.setImageResource(R.drawable.pass3);
                    break;
                case 4:
                    field.setImageResource(R.drawable.pass4);
                    break;
                case 5:
                    field.setImageResource(R.drawable.pass5);
                    break;
                case 6:
                    field.setImageResource(R.drawable.pass6);
                    ((MainActivity)getActivity()).verifyVerificationCode(code.toString());
                    break;
            }

        }

        onscreen.setText(text);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction();
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
    public void onDetach() {
        super.onDetach();
        MainActivity act = ((MainActivity)getActivity());
        act.visiblebutton();
        mListener = null;

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }
    public void complete(){
        field.setImageResource(R.drawable.pass6);
        loading();
    }

    public void loading(){
        mode2.setVisibility(View.GONE);
        table.setVisibility(View.GONE);
        mode3.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this.getActivity(),passwordActivity.class);
        startActivity(intent);

    }



}
