package example.com.cominghome.data.drawer_menu;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import example.com.cominghome.R;

/**
 * Created by Loner on 24.05.2015.
 */
public class DrawerExpItemAdapter extends BaseExpandableListAdapter {
    private static final int MAP_TYPE_CHOICE = 1;


    private LayoutInflater mInflater;
    private String[] groupNames;
    private DrawerItem[] items;
    private String[] mapTypeChildren;

    public DrawerExpItemAdapter(Activity context, DrawerItem[] items) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        groupNames = context.getResources().getStringArray(R.array.groups_array);
        this.items = items;
        mapTypeChildren = context.getResources().getStringArray(R.array.map_type_children_array);
    }

    @Override
    public int getGroupCount() {
        return groupNames.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition == MAP_TYPE_CHOICE)
            return mapTypeChildren.length;
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupNames[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition == MAP_TYPE_CHOICE)
            return mapTypeChildren[childPosition];
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_exp_item, null);
        }
        ImageView imageGroup = (ImageView) convertView.findViewById(R.id.imageViewIconExp);
        TextView groupText = (TextView) convertView.findViewById(R.id.textViewNameExp);
        imageGroup.setImageResource(items[groupPosition].getIcon());
        groupText.setText(items[groupPosition].getName());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_exp_item_child, null);
        }
        TextView childText = (TextView) convertView.findViewById(R.id.tv_list_item_child);
        childText.setText((String) getChild(groupPosition, childPosition));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
