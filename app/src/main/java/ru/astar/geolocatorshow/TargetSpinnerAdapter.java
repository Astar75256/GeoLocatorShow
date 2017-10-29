package ru.astar.geolocatorshow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Astar on 27.10.2017.
 */

public class TargetSpinnerAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Target> targets;

    public TargetSpinnerAdapter(Context context, ArrayList<Target> targets) {
        this.context = context;
        this.targets = targets;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return targets.size();
    }

    @Override
    public Object getItem(int i) {
        return targets.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.spinner_array_adapter, parent, false);

        Target target = getTarget(position);

        ((TextView) view.findViewById(R.id.textTitle)).setText(target.getName());
        return view;
    }

    public Target getTarget(int position) {
        return ((Target)getItem(position));
    }
}
