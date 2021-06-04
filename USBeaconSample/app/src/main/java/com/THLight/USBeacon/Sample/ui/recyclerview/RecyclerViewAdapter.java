package com.THLight.USBeacon.Sample.ui.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ViewTypeInterface> itemTypeList;

    public RecyclerViewAdapter(Context context, List<ViewTypeInterface> itemTypeList) {
        this.context = context;
        this.itemTypeList = itemTypeList;
    }

    @Override
    public int getItemViewType(int position) {
        ViewTypeInterface itemType = getItemTypeByPosition(position);
        return itemType.getLayoutId();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(viewType, parent, false);
        ViewTypeInterface itemType = getItemTypeByViewType(viewType);
        return itemType.getViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewTypeInterface itemType = getItemTypeByPosition(position);
        itemType.bindViewHolder(holder, getItemTypeIndexByPosition(position));
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (ViewTypeInterface itemType : itemTypeList) {
            count += itemType.getItemCount();
        }
        return count;
    }

    private ViewTypeInterface getItemTypeByPosition(int position) {
        int count = 0;
        for (ViewTypeInterface itemType : itemTypeList) {
            count += itemType.getItemCount();
            if (position < count) {
                return itemType;
            }
        }
        return null;
    }

    private ViewTypeInterface getItemTypeByViewType(int viewType) {
        for (ViewTypeInterface itemType : itemTypeList) {
            if (itemType.getLayoutId() == viewType) {
                return itemType;
            }
        }
        return null;
    }

    private int getItemTypeIndexByPosition(int position) {
        int count = 0;
        for (ViewTypeInterface itemType : itemTypeList) {
            count += itemType.getItemCount();
            if (position < count) {
                int preItemCount = (count - itemType.getItemCount());
                return position - preItemCount;
            }
        }
        return -1;
    }

    public interface ViewTypeInterface {
        int getItemCount();

        int getLayoutId();

        RecyclerView.ViewHolder getViewHolder(View itemView);

        void bindViewHolder(RecyclerView.ViewHolder viewHolder, int index);
    }
}