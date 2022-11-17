package com.xlzhen.dlnavideodownload.widget;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;

import com.google.android.material.textview.MaterialTextView;
import com.xlzhen.dlnavideodownload.R;


public class ToastUIView extends Toast {


    @IntDef({
            LENGTH_SHORT,LENGTH_LONG
    })
    @interface LENGTH{}

    private static ToastUIView toastUIView;

    private long countTime;//总时长
    private int duration;//系统toast持续时间
    private static Context context;
    private static CharSequence text;

    private MaterialTextView toastTextView;
    public ToastUIView(Context context) {
        super(context);
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.toast_layout, null);
        toastTextView = layout.findViewById(R.id.toast_text);
        setView(layout);

    }

    public static ToastUIView makeText(Context context, CharSequence text,@LENGTH  int duration) {
        ToastUIView.context=context;
        ToastUIView.text=text;
        toastUIView=new ToastUIView(context);
        toastUIView.setToastText(text);
        toastUIView.setToastDuration(duration);
        return toastUIView;
    }

    public static ToastUIView makeText(Context context, int resId, int duration){
        toastUIView=makeText(context,context.getString(resId),duration);
        return toastUIView;
    }

    public ToastUIView setToastText(CharSequence text){
        toastTextView.setText(text);
        return this;
    }

    public ToastUIView setToastDuration(@LENGTH int duration){
        setDuration(duration);
        return this;
    }

    public ToastUIView setShowTime(long countTime) {
        this.duration=getDuration();
        this.countTime=countTime;
        return this;
    }

    @Override
    public void show() {
        super.show();
        if(countTime>0) {
            new Handler().postDelayed(() -> {
                countTime -= (duration == LENGTH_SHORT ? 1000 : 2000);

                if (countTime > (duration == LENGTH_SHORT ? 1000 : 2000)) {
                    //cancel();//先取消
                    ToastUIView.makeText(context, text, duration).setShowTime(countTime).show();
                }
            }, duration == LENGTH_SHORT ? 1000 : 2000);//short是1秒，long是2秒
        }
    }
}
