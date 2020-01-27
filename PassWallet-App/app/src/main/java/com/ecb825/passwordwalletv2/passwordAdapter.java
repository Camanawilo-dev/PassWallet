package com.ecb825.passwordwalletv2;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class passwordAdapter extends RecyclerView.Adapter<passwordAdapter.passwordViewHolder> implements Filterable {
    private List<items> itemList;
    private List<items> itemListFull;
    private  OnItemClickListener mlistener;
    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public  void  setOnitemClickListener(OnItemClickListener listener){
        mlistener = listener;
    }
    public static class passwordViewHolder extends RecyclerView.ViewHolder{
        private TextView txt1;
        private TextView txt2;
        private TextView txt3;
        private TextView id;

        public  passwordViewHolder(View itemView, final OnItemClickListener listener){
            super(itemView);
            txt1 = itemView.findViewById(R.id.txt1);
            txt2 = itemView.findViewById(R.id.txt2);
            txt3 = itemView.findViewById(R.id.txt3);




            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public  passwordAdapter(List<items> items){
        itemList = items;
        itemListFull = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public passwordViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item,viewGroup,false);
        passwordViewHolder pvh = new passwordViewHolder(v,mlistener);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull passwordViewHolder passwordViewHolder, int i) {
        items currentitem = itemList.get(i);

        passwordViewHolder.txt1.setText(currentitem.getLine1());
        passwordViewHolder.txt2.setText(currentitem.getLine2());
        passwordViewHolder.txt3.setText(currentitem.getLine3());
        Log.w("WARN",   Integer.toString(currentitem.getId()));
        String ib = Integer.toString(currentitem.getId());



    }

    public void setItemListFull(List<items> items){
        itemListFull = new ArrayList<>(items);
    }
    public List<items> getFullList(){
        return itemListFull;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private  Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<items> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(itemListFull);
            }else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(items item:itemListFull){
                    if(item.getLine1().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            itemList.clear();
            itemList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };



    public items getItem(int pos){
        return itemList.get(pos);
    }
    @Override
    public Filter getFilter(){
        return  itemFilter;
    }

}
