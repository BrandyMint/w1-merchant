package com.w1.merchant.android.extra;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.Template;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

//список шаблонов
public class ImageTextAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private List<Template> mTemplates = new ArrayList<>();

	public ImageTextAdapter(Context c) {
        mInflater = LayoutInflater.from(c);
	}

    public void setTemplates(List<Template> templates) {
        mTemplates.clear();
        mTemplates.addAll(templates);
        notifyDataSetChanged();
    }

	public int getCount() {
		return mTemplates.size() + 1;
	}

    public int getItemViewType(int position) {
        if (position == getDummyTemplatePosition()) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Nullable
	public Template getItem(int position) {
        if (position == getDummyTemplatePosition()) return null;
		return mTemplates.get(position);
	}

	public long getItemId(int position) {
        if (position == getDummyTemplatePosition()) return ListView.NO_ID;
		return getItem(position).templateId.longValue();
	}

    private int getDummyTemplatePosition() {
        return mTemplates.size();
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        if (position == getDummyTemplatePosition()) {
            if (convertView != null) return convertView;
            View root = mInflater.inflate(R.layout.template_cell, parent,false);
            root.findViewById(R.id.open_web_message).setVisibility(View.VISIBLE);
            root.findViewById(R.id.tvDate).setVisibility(View.GONE);
            return root;
        }

        Template template = getItem(position);

        ViewHolder holder;
		View grid;
		if (convertView == null) {
			grid = mInflater.inflate(R.layout.template_cell, parent, false);
            holder = new ViewHolder(grid);
            grid.setTag(R.id.tag_template_view_holder, holder);
		} else {
			grid = convertView;
            holder = (ViewHolder)grid.getTag(R.id.tag_template_view_holder);
		}

		Picasso.with(grid.getContext())
                .load(template.getLogoUrl())
                .into(holder.imagePart);

        if (template.schedule != null && template.schedule.getNextExecutionDate() != null) {
            holder.date.setVisibility(View.VISIBLE);
            holder.date.setText(dateFormatTempl(template.schedule.getNextExecutionDate(), grid.getResources()));
        } else {
            holder.date.setVisibility(View.INVISIBLE);
        }

		holder.textPart.setText(template.title);

		return grid;
	}

    //форматирование дат для списка шаблонов
    private static String dateFormatTempl(Date date, Resources resources) {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return day + " " + resources.
                getStringArray(R.array.month_array_cut)[month];
    }

    public static class ViewHolder {

        public final View root;

        public final ImageView imagePart;

        public final TextView textPart;

        public final TextView date;

        public ViewHolder(View root) {
            this.root = root;
            imagePart = (ImageView)root.findViewById(R.id.imagepart);
            textPart = (TextView)root.findViewById(R.id.textpart);
            date = (TextView)root.findViewById(R.id.tvDate);
        }

    }
}
