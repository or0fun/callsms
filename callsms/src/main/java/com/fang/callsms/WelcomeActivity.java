package com.fang.callsms;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.fang.base.BaseActivity;
import com.fang.call.CallHelper;
import com.fang.contact.ContactHelper;

public class WelcomeActivity extends BaseActivity implements AnimationListener {

	@Override
	protected void onResume() {
		super.onResume();
	}

	private ImageView imageView = null;
	private Animation alphaAnimation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (ContactHelper.hasReaded() && CallHelper.hasRead()) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			this.finish();
		} else {

			setContentView(R.layout.welcome);
			imageView = (ImageView) findViewById(R.id.welcome_image_view);
			alphaAnimation = AnimationUtils.loadAnimation(this,
					R.anim.welcome_alpha);
			alphaAnimation.setFillEnabled(true);
			alphaAnimation.setFillAfter(true);
			imageView.setAnimation(alphaAnimation);
			alphaAnimation.setAnimationListener(this);
		}

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		this.finish();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return false;
	}
}
