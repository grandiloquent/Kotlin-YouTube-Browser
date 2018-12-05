package euphoria.psycho.browser;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

public class BottomSheet {
    private final Context mContext;
    private final List<Item> mItems;
    private WeakReference<PopupWindow> mWindow;

    public BottomSheet(Context context, List<Item> items) {

        mContext = context;
        mItems = items;
    }

    private View createView(AdapterView.OnItemClickListener listener) {
        GridView gridView = new GridView(mContext);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gridView.setLayoutParams(layoutParams);
        gridView.setHorizontalFadingEdgeEnabled(false);
        gridView.setVerticalFadingEdgeEnabled(false);
        gridView.setAdapter(new ViewAdapter());
        gridView.setNumColumns(5);
        gridView.setOnItemClickListener(listener);
        return gridView;
    }

    public void show(View parent, AdapterView.OnItemClickListener listener) {
        PopupWindow window = new PopupWindow(parent, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow = new WeakReference<PopupWindow>(window);
        window.setAnimationStyle(android.R.style.Animation);
        window.setOutsideTouchable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setElevation(5.0f);
        }
        window.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.dialog_background));
        window.setContentView(createView(listener));
        window.showAtLocation(parent, Gravity.BOTTOM, 0, 0);

    }

    public void dismiss() {
        if (mWindow.get() != null) {
            mWindow.get().dismiss();
        }
    }

    static class Item {
        public String title;
        public int imageResId;
    }

    private class ViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Item getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_grid, null);
                viewHolder = new ViewHolder();
                viewHolder.title = convertView.findViewById(R.id.title);
                viewHolder.image = convertView.findViewById(R.id.image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.title.setText(mItems.get(position).title);
            viewHolder.image.setImageResource(mItems.get(position).imageResId);
            return convertView;
        }
    }

    private class ViewHolder {
        public TextView title;
        public ImageView image;
    }
}
