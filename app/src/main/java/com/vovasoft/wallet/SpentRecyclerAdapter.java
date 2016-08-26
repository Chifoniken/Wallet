package com.vovasoft.wallet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vovasoft.wallet.Database.SpentModel;

import java.util.ArrayList;

/**
 * Created by arsen on 28.03.2016.
 */
public class SpentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private class VIEW_TYPES {
        public static final int Normal = 1;
        public static final int Footer = 2;
    }

    private Context context;
    private ArrayList<SpentModel> items;


    public SpentRecyclerAdapter(Context context, ArrayList<SpentModel> items) {
        this.context = context;
        this.items = items;
    }


    @Override
    public int getItemViewType(int position) {
        if(position == items.size())
            return VIEW_TYPES.Footer;
        else
            return VIEW_TYPES.Normal;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;

        switch (viewType) {
            case VIEW_TYPES.Normal:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_tem_layout, parent, false);
                holder = new SpentDayViewHolder(parent.getContext(), itemView);
                break;
            default:
                View footer = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_footer_layout, parent, false);
                holder = new Footer(footer);
                break;
        }

        return holder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPES.Normal) {
            SpentDayViewHolder viewHolder = (SpentDayViewHolder) holder;
            viewHolder.setSum(items.get(position).getSum());
            viewHolder.setCategory(context.getText(items.get(position).getCategory()).toString());
        }
    }


    @Override
    public int getItemCount() {
        return items.size() + 1;
    }


    private class SpentDayViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private View view;

        private TextView sum;
        private TextView info;


        public SpentDayViewHolder(Context context, View itemView) {
            super(itemView);

            this.context = context;
            this.view = itemView;

            sum = (TextView) view.findViewById(R.id.sum);
            info = (TextView) view.findViewById(R.id.info);
        }


        public void setSum(String str) {
            sum.setText(Helper.getInstance().getSpacedString(str));
        }


        public void setCategory(String str) {
            info.setText(str);
        }
    }

    private class Footer extends RecyclerView.ViewHolder {
        public Footer(View itemView) {
            super(itemView);
        }
    }

}
