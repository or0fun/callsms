package com.fang.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

/**
 * Created by benren.fj on 6/9/15.
 */
public class CustomEditText extends EditText {
    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(MotionEvent.ACTION_DOWN == event.getAction()) {
            clearFocus();  //在滑动设备列表的时候，editview无法弹出软键盘
        }
        return super.onTouchEvent(event);
    }
}
