package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder>{
    private List<FriendItem> friendItemsData;

    interface OnItemClickHandler{
        void onItemSelect(int position, String identify);

        void onItemCancel(int position, String identify);
    }

    private OnItemClickHandler onItemClickHandler;

    class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        private ToggleButton toggle;
        private TextView t1;

        public ViewHolder(View itemView){
            super(itemView);
            t1 = (TextView)itemView.findViewById(R.id.t1);
            toggle = (ToggleButton) itemView.findViewById(R.id.toggle);

            toggle.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.toggle:
                    if(buttonView.isChecked()){
                        int position = getAdapterPosition();
                        String identify = friendItemsData.get(position).getID();
                        onItemClickHandler.onItemSelect(position,identify);
                    }
                    else{
                        int position = getAdapterPosition();
                        String identify = friendItemsData.get(position).getID();
                        onItemClickHandler.onItemCancel(position,identify);
                    }
                    break;
            }
        }
    }

    public FriendAdapter(List<FriendItem> friendItemsData, OnItemClickHandler onItemClickHandler){
        this.friendItemsData = friendItemsData;
        this.onItemClickHandler = onItemClickHandler;
    }

    public void addItem(FriendItem text) {
        // 為了示範效果，固定新增在位置3。若要新增在最前面就把3改成0
        friendItemsData.add(1,text);//新增資料在清單標記位置
        notifyItemInserted(1);//插入列表標記位置，帶有動畫
        notifyItemRangeChanged(0, friendItemsData.size());//從列表0到底的資料批量進行數據刷新

    }

    public void removeItem(int position){
        friendItemsData.remove(position);//移除資料在清單標記位置
        notifyItemRemoved(position);//移除列表標記位置，帶有動畫
        notifyItemRangeChanged(0, friendItemsData.size());//從列表0到底的資料批量進行數據刷新
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendItem data = friendItemsData.get(position);
        holder.t1.setText(data.getName());
        holder.toggle.setChecked(data.getChecked());
    }

    @Override
    public int getItemCount() {
        return friendItemsData.size();
    }


}
