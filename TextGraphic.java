package com.example.camera2test;
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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;


import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.example.camera2test.GraphicOverlay.Graphic;




/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends Graphic {

    private static final int TEXT_COLOR = Color.WHITE; //Todo:What if i can use a tts engine which will change the colour of the text the text
    private static final float TEXT_SIZE = 44.0f; // TODO i may have to make a method which will automatically adjust text size acoording to real size
    private static final float STROKE_WIDTH = 2.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final FirebaseVisionText.Element text;


    TextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element text) {
        super(overlay);

        this.text = text; //Todo: to make a map which will store each word and use it

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        postInvalidate();
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (text == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        // Draws the bounding box around the TextBlock.
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, rectPaint);

        //  Log.d("area", "text: " + text.getText() + "\nArea: " + Area);
        /**Here we are defining a Map which takes a string(Text) and a Integer X_Axis.The text will act as key to the X_Axis.
         Once We Got the X_Axis we will pass its value to a SparseIntArray which will Assign X Axis To Y Axis
         .Then We might Create another Map which will Store Both The text and the coordinates*/
        int X_Axis = (int) rect.left;
        int Y_Axis = (int) rect.bottom;

        // Renders the text at the bottom of the box.
        Log.d("PositionXY", "x: "+X_Axis +" |Y: "+ Y_Axis);
        canvas.drawText(text.getText(), rect.left, rect.bottom, textPaint); // rect.left and rect.bottom are the coordinates of the text they can be used for mapping puposes


    }
}