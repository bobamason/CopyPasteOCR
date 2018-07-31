// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package net.masonapps.shippingdataocr.mlkit.textrecognition;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import net.masonapps.shippingdataocr.R;
import net.masonapps.shippingdataocr.mlkit.FrameMetadata;
import net.masonapps.shippingdataocr.mlkit.GraphicOverlay;
import net.masonapps.shippingdataocr.mlkit.VisionProcessorBase;
import net.masonapps.shippingdataocr.ui.TextOverlay;

import java.io.IOException;
import java.util.List;

/**
 * Processor for the text recognition demo.
 */
public class TextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

    private static final String TAG = "TextRecProc";

    private final FirebaseVisionTextDetector detector;
    private final Context context;

    public TextRecognitionProcessor(Context context) {
        this.context = context;
        detector = FirebaseVision.getInstance().getVisionTextDetector();
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @NonNull FirebaseVisionText results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        List<FirebaseVisionText.Block> blocks = results.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, elements.get(k));
                    graphicOverlay.add(textGraphic);

                }
            }
        }
    }

    @Override
    protected void onSuccess(@NonNull FirebaseVisionText results, @NonNull FrameMetadata frameMetadata, @NonNull TextOverlay textOverlay) {
        textOverlay.removeAllViews();
        final List<FirebaseVisionText.Block> blocks = results.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            final TextView textView = createTextView(blocks.get(i));
            final ViewGroup.LayoutParams layoutParams = createLayoutParams(blocks.get(i));
            if (layoutParams != null)
                textOverlay.addView(textView, layoutParams);
        }
    }

    private TextView createTextView(FirebaseVisionText.Block block) {
        final TextView textView = new TextView(context);
        float textSize = context.getResources().getDimension(R.dimen.defaultTextSize);
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
        textView.setBackgroundColor(0x99FFFFFF);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        return textView;
    }

    @Nullable
    private ViewGroup.LayoutParams createLayoutParams(FirebaseVisionText.Block block) {
        final Rect boundingBox = block.getBoundingBox();
        if (boundingBox == null) return null;
//        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(boundingBox.width(), boundingBox.height());
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = boundingBox.top;
        layoutParams.leftMargin = boundingBox.left;
        return layoutParams;
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }
}
