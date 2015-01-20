package ru.bokus.w1.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.bokus.w1.Constants;
import ru.bokus.w1.Extra.ImageTextAdapter;

public class TemplateFragment extends Fragment {

    private View parentView;
    Intent intent;
    RelativeLayout rlGrid;
    TextView tv;
    String[] item = { "", "", "" };
    GridView gridview;
    MenuActivity menuActivity;
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.templates, container, false);
        gridview = (GridView) parentView.findViewById(R.id.gridview);
		gridview.setOnItemClickListener(gridviewOnItemClickListener);
		menuActivity = (MenuActivity) getActivity();
		return parentView;
    }

    private GridView.OnItemClickListener gridviewOnItemClickListener = new GridView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			rlGrid = (RelativeLayout) v;
			tv = (TextView) rlGrid.getChildAt(4);
			if (tv.getText().toString().isEmpty()) {
				Uri address = Uri.parse(Constants.URL_WALLETONE);
				intent = new Intent(Intent.ACTION_VIEW, address);
				startActivity(intent);
			} else {
				intent = new Intent(getActivity(), EditTemplate.class);
				item = (String[]) parent.getAdapter().getItem(position);
				intent.putExtra("templateId", item[2]);
				intent.putExtra("token", menuActivity.token);
				intent.putExtra("accountTypeId", menuActivity.accountTypeId);
				startActivity(intent);
			}
		}
	};
	
	public void setAdapter() {
		gridview.setAdapter(new ImageTextAdapter(getActivity()));
	}
}
