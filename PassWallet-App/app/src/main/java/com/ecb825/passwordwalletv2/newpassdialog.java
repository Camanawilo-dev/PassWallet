package com.ecb825.passwordwalletv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class newpassdialog {
    private EditText username;
    private EditText password;
    private EditText password2;
    private EditText webname;
    private Button confirm;
    private ImageView close;
    private int it;
    private items itm;
    public void showDialog(final Activity activity,final passwords frag,String mode) {
        if (mode.equals("1")) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.addnewpassword);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            username = (EditText) dialog.findViewById(R.id.uname);
            password = (EditText) dialog.findViewById(R.id.pass1);
            password2 = (EditText) dialog.findViewById(R.id.pass2);
            webname = (EditText) dialog.findViewById(R.id.sname);
            close = (ImageView) dialog.findViewById(R.id.formclose);
            confirm = (Button) dialog.findViewById(R.id.formsub);


            final Context context = frag.getContext();


            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });


            confirm.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(!username.getText().toString().equals("")&&!password.getText().toString().equals("")&&!webname.getText().toString().equals("")){
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = new Date();
                    List<items> ex = new ArrayList<>(frag.getlist());
                    int id = 0;
                    boolean taken = true;

                    for (int i = 0; i <= ex.size(); i++) {
                        for (int j = 0; j < ex.size(); j++) {
                            if (ex.get(j).getId() == i) {
                                taken = true;
                                break;
                            } else {
                                taken = false;
                                id = i;
                            }
                        }
                        if (!taken) {
                            break;
                        }
                    }


                    if (password.getText().toString().equals(password2.getText().toString())) {
                        items itemtoadd = new items(id, webname.getText().toString(), username.getText().toString(), "Last changed on " + dateFormat.format(date), password.getText().toString());

                        webname.setText(null);
                        username.setText(null);
                        password.setText(null);
                        password2.setText(null);

                        try {
                            frag.addItem(itemtoadd);
                        } catch (GeneralSecurityException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    } else {
                        new AlertDialog.Builder(context, R.style.AlertDialog)
                                .setTitle("Passwords do not match")
                                .setMessage("Please make sure that both password entries match")
                                .setNegativeButton("Return", null)
                                .show();
                    }
                    }else {
                        Toast.makeText(context,"One or more fields are empty",Toast.LENGTH_SHORT).show();

                    }
                }
            });

            dialog.show();
        }else if(mode.equals("2")){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.editpassworddialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            final Context context = frag.getContext();

            username = (EditText) dialog.findViewById(R.id.uname3);
            password = (EditText) dialog.findViewById(R.id.newpassinput);
            password2 = (EditText) dialog.findViewById(R.id.newpass2input);
            webname = (EditText) dialog.findViewById(R.id.sitename1);
            close = (ImageView) dialog.findViewById(R.id.closebtn);
            confirm = (Button) dialog.findViewById(R.id.savechanges);

            passwordAdapter pa = frag.getmAdapter();
            items former = pa.getItem(it);

            webname.setText(former.getLine1());
            username.setText(former.getLine2());


            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!username.getText().toString().equals("") && !webname.getText().toString().equals("")) {
                        if(password.getText().toString().equals(password2.getText().toString())){
                        try {
                            frag.updateItem(it, password.getText().toString(), username.getText().toString(), webname.getText().toString());
                        } catch (GeneralSecurityException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                        }else {
                            Toast.makeText(context,"Passwords do not match",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(context,"One or more fields are empty",Toast.LENGTH_SHORT).show();
                    }
                }
            });

            dialog.show();
        }
    }
    public void setIt(int i){
        this.it = i;
    }
    public void setitm(items its){
        this.itm = its;
    }
}
