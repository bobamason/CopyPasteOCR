package net.masonapps.shippingdataocr.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.google.android.gms.vision.CameraSource;

/**
 * Created by Bob Mason on 7/31/2018.
 */
public class TextOverlay extends FrameLayout {

    private final Object lock = new Object();
    private int previewWidth;
    private float widthScaleFactor = 1.0f;
    private int previewHeight;
    private float heightScaleFactor = 1.0f;
    private int facing = CameraSource.CAMERA_FACING_BACK;

    public TextOverlay(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        synchronized (lock) {
            this.previewWidth = previewWidth;
            this.previewHeight = previewHeight;
            this.facing = facing;
        }
        postInvalidate();
    }
}
