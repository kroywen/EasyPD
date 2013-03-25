package com.thepegeekapps.easypd.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thepegeekapps.easypd.R;

public class SwitchView extends RelativeLayout implements OnClickListener {
	
	public interface OnStateChangedListener {
		public void onStateChanged(int newState);
	}

	public static final int STATE_INTERNAL = 0;
	public static final int STATE_EXTERNAL = 1;
	public static final int STATE_DEFAULT = STATE_EXTERNAL;
	
	protected int state;
	protected String internalText;
	protected String externalText;
	
	protected ImageView leftThumb;
	protected TextView text;
	protected ImageView rightThumb;
	
	protected OnStateChangedListener listener;
	
	public SwitchView(Context context) {
		this(context, null);
	}
	
	public SwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchView);
		for (int i = 0; i < a.getIndexCount(); i++) {
		    int attr = a.getIndex(i);
		    switch (attr) {
	        case R.styleable.SwitchView_internalText:
	            setInternalText(a.getString(attr));
	            break;
	        case R.styleable.SwitchView_externalText:
	            setExternalText(a.getString(attr));
	            break;
		    }
		}
		a.recycle();
		initialize();
	}

		
	protected void initialize() {
		setOnClickListener(this);
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.switch_view, null);
		leftThumb = (ImageView) view.findViewById(R.id.leftThumb);
		text = (TextView) view.findViewById(R.id.text);
		rightThumb = (ImageView) view.findViewById(R.id.rightThumb);
		addView(view);
		setState(STATE_DEFAULT);
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
		text.setText(state == STATE_INTERNAL ? internalText : externalText);
		text.setGravity((state == STATE_INTERNAL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL);
		leftThumb.setVisibility(state == STATE_INTERNAL ? View.GONE : View.VISIBLE);
		rightThumb.setVisibility(state == STATE_INTERNAL ? View.VISIBLE : View.GONE);
		if (listener != null) {
			listener.onStateChanged(state);
		}
	}
	
	public String getInternalText() {
		return internalText;
	}
	
	public void setInternalText(String internalText) {
		this.internalText = internalText;
	}
	
	public String getExternalText() {
		return externalText;
	}
	
	public void setExternalText(String externalText) {
		this.externalText = externalText;
	}

	@Override
	public void onClick(View v) {
		setState(state == STATE_INTERNAL ? STATE_EXTERNAL : STATE_INTERNAL);
	}
	
	public void setOnStateChangedListener(OnStateChangedListener listener) {
		this.listener = listener;
	}

}
