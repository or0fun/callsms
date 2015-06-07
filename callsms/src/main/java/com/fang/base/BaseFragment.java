package com.fang.base;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class BaseFragment extends Fragment {

	public Context mContext;
	public static WindowManager mWindowManager;
	public View mView;
	protected boolean mIsSelected = false;
	protected Model mModel;

    public void setModel(Model model) {
        mModel = model;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mView = view;
		super.onViewCreated(view, savedInstanceState);
	}
	
	public boolean onBackPressed() {
		return false;
	}

	public boolean isSelected() {
		return mIsSelected;
	}

	public void setSelected(boolean mIsSelected) {
		this.mIsSelected = mIsSelected;
	}
	
	public boolean isNeedLoading() {
		return false;
	}

    public void showLoading() { }
    public void hideLoading() { }
}
