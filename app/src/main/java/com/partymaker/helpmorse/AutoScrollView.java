package com.partymaker.helpmorse;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class AutoScrollView extends ScrollView {
    private boolean atBottom;

    public AutoScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.atBottom = true;
    }

    public AutoScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.atBottom = true;
    }

    public AutoScrollView(Context context) {
        super(context);
        this.atBottom = true;
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        int count = getChildCount();
        if (count > 0) {
            this.atBottom = getHeight() + t >= getChildAt(count - 1).getBottom();
        }
    }

    public void scrollDownMaybe() {
        if (this.atBottom) {
            fullScroll(130);
        }
    }
}
