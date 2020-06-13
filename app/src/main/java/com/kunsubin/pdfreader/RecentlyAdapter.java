package com.kunsubin.pdfreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class RecentlyAdapter extends BaseAdapter {
    
    private List<Recently> mData;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    public RecentlyAdapter(Context context){
        this.mContext=context;
        this.mLayoutInflater= LayoutInflater.from(mContext);
    }
    
    @Override
    public int getCount() {
        return mData.size();
    }
    
    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    public void setData(List<Recently> list){
        if(mData==null){
            mData=new ArrayList<>();
        }
        mData.clear();
        if(list==null)
            return;
        mData.addAll(list);
        notifyDataSetChanged();
    }
    
    public void addItem(Recently recently){
        mData.add(0,recently);
        notifyDataSetChanged();
    }
    public void removeItem(Recently recently){
        mData.remove(recently);
        notifyDataSetChanged();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_recently, null);
            holder = new ViewHolder();
            holder.mTextViewFileName = (TextView) convertView.findViewById(R.id.text_file_name);
            holder.mTextViewPath = (TextView) convertView.findViewById(R.id.text_path);
            holder.mTextViewDate = (TextView) convertView.findViewById(R.id.text_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Recently recently=this.mData.get(position);
        holder.mTextViewFileName.setText(recently.getFileName());
        holder.mTextViewPath.setText(recently.getPath());
        holder.mTextViewDate.setText(recently.getDate().toString());
    
        convertView.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.getFlashAnimation());
            if(mOnItemClickListener!=null){
                mOnItemClickListener.onItemClick(v,recently);
            }
        });
        
        return convertView;
    }
    
    public void setOnItemClickListener(
              OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
    
    static class ViewHolder {
        TextView mTextViewFileName;
        TextView mTextViewPath;
        TextView mTextViewDate;
    }
    
    interface OnItemClickListener{
        void onItemClick(View view, Recently recently);
    }
    
}
