package net.masonapps.shippingdataocr.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import net.masonapps.shippingdataocr.R;
import net.masonapps.shippingdataocr.mlkit.FrameMetadata;
import net.masonapps.shippingdataocr.utils.Logger;

import java.util.List;
import java.util.Locale;

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
    @Nullable
    private OnSendToPCListener listener = null;

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

    public void updateRecognitionResult(@NonNull FirebaseVisionText results, @NonNull FrameMetadata frameMetadata) {
        synchronized (lock) {
            removeAllViews();
            final List<FirebaseVisionText.Block> blocks = results.getBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                final TextView textView = createTextView(blocks.get(i));
                final ViewGroup.LayoutParams layoutParams = createLayoutParams(blocks.get(i));
                if (layoutParams != null)
                    addView(textView, layoutParams);
            }
        }
    }

    private TextView createTextView(FirebaseVisionText.Block block) {
        final TextView textView = new TextView(getContext());
        float textSize = getContext().getResources().getDimension(R.dimen.defaultTextSize);
        StringBuilder sb = new StringBuilder();
        final List<FirebaseVisionText.Line> lines = block.getLines();
        for (int i = 0; i < lines.size(); i++) {
            final List<FirebaseVisionText.Element> elements = lines.get(i).getElements();
            for (int j = 0; j < elements.size(); j++) {
                final FirebaseVisionText.Element element = elements.get(j);
                if (element.getBoundingBox() != null && element.getBoundingBox().height() < textSize)
                    textSize = element.getBoundingBox().height();
                sb.append(element.getText());
                if (j < elements.size() - 1)
                    sb.append(" ");
            }
            if (i < lines.size() - 1)
                sb.append("\n");
        }
        textView.setText(sb);
        textView.setTextIsSelectable(true);
        textView.setBackgroundColor(0xa0FFFFFF);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setCustomSelectionActionModeCallback(new CustomActionModeCallback(textView));
        final int pad = 8;
        textView.setPadding(pad, pad, pad, pad);
        return textView;
    }

    @Nullable
    private ViewGroup.LayoutParams createLayoutParams(FirebaseVisionText.Block block) {
        final Rect boundingBox = block.getBoundingBox();
        if (boundingBox == null) return null;
        final int pad = 8;
//        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(boundingBox.width(), boundingBox.height());
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = boundingBox.top - pad;
        layoutParams.leftMargin = boundingBox.left - pad;
        return layoutParams;
    }

    public void setOnSendToPCListener(@Nullable OnSendToPCListener listener) {
        this.listener = listener;
    }

    public interface OnSendToPCListener {
        void onSendToPC(String text);
    }

    private class CustomActionModeCallback implements ActionMode.Callback {

        private final TextView textView;

        private CustomActionModeCallback(TextView textView) {
            this.textView = textView;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_text_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final int start = textView.getSelectionStart();
            final int end = textView.getSelectionEnd();
            Logger.d(String.format(Locale.US, "selection = [%d,%d]", start, end));
            final String text;
            if (start >= 0 && end > start && end <= textView.getText().length()) {
                text = textView.getText().subSequence(start, end).toString();
            } else {
                text = "";
            }
            Logger.d("selected text = " + text);
            switch (item.getItemId()) {
                case R.id.item_web_search:
                    Logger.d(String.format(Locale.US, "searching web: \"%s\"", text));
                    final Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, text);
                    if (intent.resolveActivity(getContext().getPackageManager()) != null)
                        getContext().startActivity(intent);
                    return true;
                case R.id.item_send_pc:
                    if (listener != null && !text.isEmpty())
                        listener.onSendToPC(text);
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }
}
