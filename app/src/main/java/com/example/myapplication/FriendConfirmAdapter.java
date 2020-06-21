package com.example.myapplication;

import android.content.Context;
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

public class FriendConfirmAdapter extends RecyclerView.Adapter<FriendConfirmAdapter.ViewHolder>{
    private List<FriendItem> friendItemsData;

    interface OnItemClickHandler{
        void onItemClick(int position, FriendItem friendItemsData);

    }

    private OnItemClickHandler onItemClickHandler;

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Button button;
        private TextView t1;

        public ViewHolder(View itemView){
            super(itemView);
            t1 = (TextView)itemView.findViewById(R.id.t1);
            button = (Button) itemView.findViewById(R.id.button);
            button.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button:
                    int position = getAdapterPosition();
                    FriendItem text = friendItemsData.get(position);
                    onItemClickHandler.onItemClick(position,text);
                    break;
            }
        }
    }

    public FriendConfirmAdapter(List<FriendItem> friendItemsData, OnItemClickHandler onItemClickHandler){
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
                .inflate(R.layout.list_friend_confirm_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendItem data = friendItemsData.get(position);
        holder.t1.setText(data.getName());
    }

    @Override
    public int getItemCount() {
        return friendItemsData.size();
    }


}
