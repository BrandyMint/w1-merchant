package ru.bokus.w1.Extra;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.bokus.w1.Activity.MenuActivity;
import ru.bokus.w1.Activity.R;

//список шаблонов
public class ImageTextAdapter extends BaseAdapter {
	private Context mContext;
	public ArrayList<String[]> dataArray  = new ArrayList<String[]>();
	
	public ImageTextAdapter(Context c) {
		mContext = c;
		//получаем список шаблонов
		try {
			dataArray = ((MenuActivity) mContext).dataTemplate;
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
		//последний кружок
		String[] lastTemplate = { "", null, null, null };
		dataArray.add(lastTemplate);
	}
	
	public int getCount() {
		return dataArray.size();
	}

	public Object getItem(int position) {
		return dataArray.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View grid;

		if (convertView == null) {
			grid = new View(mContext);
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			grid = inflater.inflate(R.layout.template_cell, parent, false);
		} else {
			grid = convertView;
		}

		ImageView imageView = (ImageView) grid.findViewById(R.id.imagepart);
		TextView textView = (TextView) grid.findViewById(R.id.textpart);
		TextView tvDate = (TextView) grid.findViewById(R.id.tvDate);
		TextView tvGoWeb = (TextView) grid.findViewById(R.id.tvGoWeb);
		TextView tvMoveOn = (TextView) grid.findViewById(R.id.tvMoveOn);
		Picasso.with(mContext).load(dataArray.get(position)[1]).into(imageView);
		if (TextUtils.isEmpty(dataArray.get(position)[3])) {
			tvDate.setVisibility(View.INVISIBLE);
		} else {
			tvDate.setText(dataArray.get(position)[3]);
		}
		textView.setText(dataArray.get(position)[0]);
		if ((position + 1) == getCount()) {
			imageView.setBackgroundResource(R.drawable.ring);
			tvGoWeb.setVisibility(View.VISIBLE);
			tvMoveOn.setVisibility(View.VISIBLE);
		} else {
			imageView.setBackgroundResource(R.drawable.oval);
			tvGoWeb.setVisibility(View.INVISIBLE);
			tvMoveOn.setVisibility(View.INVISIBLE);
		}
		return grid;
	}
}
