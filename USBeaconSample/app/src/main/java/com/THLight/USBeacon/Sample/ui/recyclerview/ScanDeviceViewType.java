package com.THLight.USBeacon.Sample.ui.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.THLight.USBeacon.Sample.R;
import com.THLight.USBeacon.Sample.entity.ScanDeviceItemEntity;

import java.util.List;

public class ScanDeviceViewType implements RecyclerViewAdapter.ViewTypeInterface {
    private List<ScanDeviceItemEntity> scanDeviceItemEntityList;

    public ScanDeviceViewType(List<ScanDeviceItemEntity> scanDeviceItemEntityList) {
        this.scanDeviceItemEntityList = scanDeviceItemEntityList;
    }

    @Override
    public int getItemCount() {
        return scanDeviceItemEntityList != null && scanDeviceItemEntityList.size() > 0 ? scanDeviceItemEntityList.size() : 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.adapter_scan_device_item;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View itemView) {
        return new ItemViewHolder(itemView);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder viewHolder, int index) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        ScanDeviceItemEntity entity = scanDeviceItemEntityList.get(index);
        itemViewHolder.deviceNameTextView.setText(entity.getDeviceName());
        itemViewHolder.majorTextView.setText(entity.getMajor());
        itemViewHolder.minorTextView.setText(entity.getMinor());
        String rssiString = entity.getRssi() + " dbm";
        itemViewHolder.rssiTextView.setText(rssiString);
        String batteryPowerString = entity.getBatteryPower() + " V";
        itemViewHolder.batteryPowerTextView.setText(batteryPowerString);
        itemViewHolder.macAddressTextView.setText(entity.getMacAddress());
    }

    public void setScanDeviceItemEntityList(List<ScanDeviceItemEntity> scanDeviceItemEntityList) {
        this.scanDeviceItemEntityList = scanDeviceItemEntityList;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceNameTextView;
        private TextView majorTextView;
        private TextView minorTextView;
        private TextView rssiTextView;
        private TextView batteryPowerTextView;
        private TextView macAddressTextView;

        private ItemViewHolder(View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.deviceNameTextView);
            majorTextView = itemView.findViewById(R.id.majorTextView);
            minorTextView = itemView.findViewById(R.id.minorTextView);
            rssiTextView = itemView.findViewById(R.id.rssiTextView);
            batteryPowerTextView = itemView.findViewById(R.id.batteryPowerTextView);
            macAddressTextView = itemView.findViewById(R.id.macAddressTextView);
        }
    }
}
