package com.ecb825.passwordwalletv2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link passwords.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link passwords#newInstance} factory method to
 * create an instance of this fragment.
 */
public class passwords extends Fragment implements bottompassoptions.BottomSheetListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView mRecyclerView;
    private passwordAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton add;
    private Button confirm;
    private ImageView close;
    private ImageView settings;
    private Group form;
    private Group list;
    private EditText username;
    private EditText password;
    private EditText password2;
    private EditText webname;
    private passwordActivity act;
    private List<items> ex;
    private TextView nopass;
    private passwords frag;
    private bottompassoptions bottompassoptions;
    private Context context;
    private SharedPreferences sharedPreferences;
    private String masterKeyAlias;
    private authPreferences preferences;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public passwords() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment passwords.
     */
    // TODO: Rename and change types and number of parameters
    public static passwords newInstance(String param1, String param2) {
        passwords fragment = new passwords();
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
    SearchView search;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_passwords, container, false);
        final Window current = getActivity().getWindow();
        passwordActivity activity = (passwordActivity) getActivity();
        context = getContext();

        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    this.getContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        act = (passwordActivity) getActivity();
        frag = this;

        list = (Group) view.findViewById(R.id.display);
        form = (Group) view.findViewById(R.id.group);
        add = (FloatingActionButton)view.findViewById(R.id.fab);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newpassdialog newpassdialog = new newpassdialog();
                newpassdialog.showDialog(act,frag,"1");
            }
        });

        close = (ImageView) view.findViewById(R.id.formclose);

        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                current.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                form.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                if(ex.size()!=0){
                    nopass.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        username = (EditText) view.findViewById(R.id.uname);
        password = (EditText) view.findViewById(R.id.pass1);
        password2= (EditText) view.findViewById(R.id.pass2);
        webname = (EditText) view.findViewById(R.id.sname);

        nopass =(TextView) view.findViewById(R.id.nopasstxt);
        mRecyclerView = view.findViewById(R.id.items);
        mRecyclerView.setHasFixedSize(true);

        loadData();
        refresh();
        if(preferences.getPassHash().isEmpty()){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.newpasskeydialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            final EditText pass1 = dialog.findViewById(R.id.passkey1);
            final EditText pass2 = dialog.findViewById(R.id.passkey2);
            final Switch fingerprint = dialog.findViewById(R.id.switch4);

            Button subimt = dialog.findViewById(R.id.button7);


            subimt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(pass1.getText().toString().equals(pass2.getText().toString())){
                        MessageDigest md5 = null;
                        try {
                            md5 = MessageDigest.getInstance("MD5");
                            String value = pass1.getText().toString();
                            md5.update(value.getBytes());
                            byte[] bytes = md5.digest();
                            StringBuilder sb = new StringBuilder();
                            for(int i=0; i< bytes.length ;i++)
                            {
                                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                            }
                            value = sb.toString();
                            sharedPreferences.edit().putString("passhash",value).apply();
                            if(fingerprint.isChecked()){
                                sharedPreferences.edit().putString("authmode","fing").apply();
                            }else {
                                sharedPreferences.edit().putString("authmode","pass").apply();
                            }
                            dialog.dismiss();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }

                    }else {
                        Toast.makeText(context,"Passwords do not match",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dialog.show();
        }

        mLayoutManager = new LinearLayoutManager(view.getContext());
        mAdapter = new passwordAdapter(ex);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnitemClickListener(new passwordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                items item = ex.get(position);
                hideKeyboardFrom(getContext(),view);
                bottompassoptions = new bottompassoptions();
                bottompassoptions.setId(item.getId());
                bottompassoptions.show(getFragmentManager(),"modal");

            }
        });

        confirm = (Button) view.findViewById(R.id.formsub);
        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                items itemtoadd = new items(0,webname.getText().toString(),username.getText().toString(),"Today",null);
                current.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                webname.setText(null);
                username.setText(null);
                password.setText(null);
                password2.setText(null);

                ex.add(itemtoadd);
                mAdapter.notifyDataSetChanged();
                saveData();

                form.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
                nopass.setVisibility(View.GONE);
            }
        });
        setHasOptionsMenu(true);
        Toolbar toolbar =(androidx.appcompat.widget.Toolbar) view.findViewById(R.id.toolbar);
        act.setSupportActionBar(toolbar);

        return view;
    }
    public void saveData(){
        Gson gson = new Gson();
        String json = gson.toJson(mAdapter.getFullList());
        sharedPreferences.edit().putString("password list",json).apply();

    }
    public void loadData(){
        try {
            Gson gson = new Gson();
            String json = sharedPreferences.getString("password list",null);
            Type type = new TypeToken<ArrayList<items>>(){}.getType();
            ex = gson.fromJson(json,type);
            if(ex == null){
                ex = new ArrayList<>();
            }
        }catch (Exception e){
            ex = new ArrayList<>();
        }

        String authmode = sharedPreferences.getString("authmode","");
        String passhash = sharedPreferences.getString("passhash","");

        preferences =  new authPreferences(passhash,authmode);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.appbar, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_setting:
                settings settings = new settings();
                FragmentTransaction fragt = act.getSupportFragmentManager().beginTransaction();
                fragt.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left,R.anim.slide_in_left,R.anim.slide_out_right);
                fragt.replace(R.id.fragment_container, settings);
                fragt.addToBackStack("settings");
                fragt.commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addItem(items item) throws GeneralSecurityException, IOException {
        ex.add(item);
        mAdapter.notifyDataSetChanged();
        mAdapter.setItemListFull(ex);
        saveData();
        if (ex.size() > 0) {
            nopass.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public List<items>  getlist(){
        return ex;
    }

    public void removeItem(int item) throws GeneralSecurityException, IOException {
        List<items> fullL = mAdapter.getFullList();
        for(int i = 0;i<ex.size();i++){
            if (ex.get(i).getId()==item){
                for(int j = 0;j<fullL.size();j++) {
                    if(fullL.get(j).getId() == ex.get(i).getId()) {
                        fullL.remove(j);
                        ex.remove(i);
                        break;
                    }
                }
            }
        }
        bottompassoptions.dismiss();
        mAdapter.notifyDataSetChanged();
        mAdapter.setItemListFull(fullL);
        saveData();
        if (fullL.size() == 0) {
            nopass.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }
    public void updateItem(int pos,String pass,String uname, String sname) throws GeneralSecurityException, IOException {
        List<items> fullL = mAdapter.getFullList();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        for(int i = 0;i<ex.size();i++){
            if (ex.get(i).getId()==pos){
                for(int j = 0;j<fullL.size();j++) {
                    if(fullL.get(j).getId() == ex.get(i).getId()) {

                        if(!pass.equals("")) {
                            fullL.get(j).setPass(pass);
                            ex.get(i).setPass(pass);
                            fullL.get(j).setLine3("Last changed on "
                                    + dateFormat.format(date));
                            ex.get(i).setLine3("Last changed on "
                                    + dateFormat.format(date));
                        }
                        if(!uname.equals("")) {
                            fullL.get(j).setLine2(uname);
                            ex.get(i).setLine2(uname);
                            fullL.get(j).setLine3("Last changed on "
                                    + dateFormat.format(date));
                            ex.get(i).setLine3("Last changed on "
                                    + dateFormat.format(date));
                        }
                        if(!sname.equals("")) {
                            fullL.get(j).setLine1(sname);
                            ex.get(i).setLine1(sname);
                            fullL.get(j).setLine3("Last changed on "
                                    + dateFormat.format(date));
                            ex.get(i).setLine3("Last changed on "
                                    + dateFormat.format(date));

                        }
                        break;
                    }
                }
            }
        }

        mAdapter.notifyDataSetChanged();
        mAdapter.setItemListFull(fullL);
        saveData();
    }

    public items getItem(int item) {
        List<items> fullL = mAdapter.getFullList();
        items it = new items(0,null,null,null,null);
        for(int i = 0;i<ex.size();i++){
            if (ex.get(i).getId()==item){
                for(int j = 0;j<fullL.size();j++) {
                    if(fullL.get(j).getId() == ex.get(i).getId()) {
                        it = new items(ex.get(i).getId(),ex.get(i).getLine1(),ex.get(i).getLine2(),ex.get(i).getLine3(),ex.get(i).getPass());
                        break;
                    }
                }
            }
        }
        bottompassoptions.dismiss();
        return it;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
        act.finish();

    }

    @Override
    public void onButtonClicked(int id, String mode) {

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
    public void refresh(){
        if(ex.size()!=0){
            nopass.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public  passwordAdapter getmAdapter(){
        return mAdapter;
    }

}
