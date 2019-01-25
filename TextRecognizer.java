package com.example.android.camera2basic;


import android.graphics.Bitmap;

import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.util.Log;
import android.util.Size;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TextRecognizer {

    public GraphicOverlay mGraphicOverlay; // it is initialised in Camera2BasicFragment
    private static final String TAG = "txtProcessor";

    public Size mPreviewSize;

    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);

    public void imageFromByteBuffer(ByteBuffer buffer, int rotation) {

        if (shouldThrottle.get()) {  // This is to drop frames when one frame is processing.if its value is true it will exit the function but if false it will execute.
            return;
        }

        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(1280)   // 480x360 is typically sufficient for
                .setHeight(960)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                .setRotation(rotation)
                .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromByteBuffer(buffer, metadata);
        runTextRecognition(image);
    }

    public void runTextRecognition(FirebaseVisionImage image) {

//    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);

        if (image == null) {
            Log.d(TAG, "runTextRecognition: Image is null");
        }

        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer(); //We are running the TextRecognizer on device

        Task<FirebaseVisionText> result = recognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText textRecognized) {
                        shouldThrottle.set(false);
                        processTextRecognised(textRecognized); // here we are passing the text for processing
                        Log.d(TAG, "onSuccess: " + textRecognized.getText());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        shouldThrottle.set(false);
                        Log.d(TAG, "onFailure: ");
                    }
                });
        shouldThrottle.set(true);

    }

    private void processTextRecognised(FirebaseVisionText textRecognized) {


        List<FirebaseVisionText.TextBlock> blocks = textRecognized.getTextBlocks(); // get the text from the image think textblocks as a paragraph
        //    if (blocks.size() == 0) {
        //        Log.d(TAG, "processTextRecognised:Unsuccessful ");
        //         mGraphicOverlay.clear();
        //         return;
        //   }
        mGraphicOverlay.clear();

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    //     mGraphicOverlay.setAspectRatio(mPreviewSize.getWidth(),mPreviewSize.getHeight());
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);

                }
            }
        }
    }
}




