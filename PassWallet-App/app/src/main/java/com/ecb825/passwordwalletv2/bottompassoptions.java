package com.ecb825.passwordwalletv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class bottompassoptions extends BottomSheetDialogFragment {
    private Button delete;
    private Button send;
    private Button edit;
    private BottomSheetListener mListener;
    private int position;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.passoptions,container,false);
        delete = v.findViewById(R.id.button5);
        send = v.findViewById(R.id.button2);
        edit = v.findViewById(R.id.button3);
        final passwordActivity act = (passwordActivity)getActivity();
        final Context context = this.getContext();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mListener.onButtonClicked(position,"send");
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newpassdialog newpassdialog = new newpassdialog();
                newpassdialog.setIt(position);
                newpassdialog.showDialog(act,act.getFrag(),"2");
                dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new AlertDialog.Builder(context,R.style.AlertDialog)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this password?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    mListener.onButtonClicked(position,"delete");
                                } catch (GeneralSecurityException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dismiss();

                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setCancelable(false)
                        .show();
            }
        });
        return v;
    }

    public  interface  BottomSheetListener{
        void onButtonClicked(int id,String mode) throws GeneralSecurityException, IOException;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (BottomSheetListener) context;
    }

    public void setId(int pos){
        position = pos;
    }
}
