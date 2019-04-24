package com.example.camera2test;

import android.hardware.camera2.CameraCharacteristics;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseIntArray;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class TextRecognizer {

    public GraphicOverlay mGraphicOverlay; // it is initialised in Camera2BasicFragment
    private static final String TAG = "txtProcessor";
    private static final int textPositionTolerance = 15; //This stops the app from redrawing the text if their are minor changes in position
    private Map<String, Integer> textCoordinates = new Map<String, Integer>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(@Nullable Object key) {
            return false;
        }

        @Override
        public boolean containsValue(@Nullable Object value) {
            return false;
        }

        @Nullable
        @Override
        public Integer get(@Nullable Object key) {
            return null;
        }

        @Nullable
        @Override
        public Integer put(@androidx.annotation.NonNull String key, @androidx.annotation.NonNull Integer value) {
            return null;
        }

        @Nullable
        @Override
        public Integer remove(@Nullable Object key) {
            return null;
        }

        @Override
        public void putAll(@androidx.annotation.NonNull Map<? extends String, ? extends Integer> m) {

        }

        @Override
        public void clear() {

        }

        @androidx.annotation.NonNull
        @Override
        public Set<String> keySet() {
            return null;
        }

        @androidx.annotation.NonNull
        @Override
        public Collection<Integer> values() {
            return null;
        }

        @androidx.annotation.NonNull
        @Override
        public Set<Entry<String, Integer>> entrySet() {
            return null;
        }
    }; // i will intitlize both of its values and use its x value to pass into a spareintarray which will return yAxis location

    public int mWidth;
    public int mHeight;
    public String textOutput;
    public int No_Of_Suggestions; // It is initialized in MainActivity

    public String test;
    /**
     * if the recognizer count matches with it then the position adjuster will not be called and if the currentCount is more than the recognizer count
     * we will call a method which will take fresh positions value of x and y.
     * it is done to find out if we are using the value of new frame or the previous frame.This is usefull for determining the change in the position of text.
     */
    public int currentCount = 0;
    public int recognizerCount = 0;
    int xMax = 0, yMax = 0;
    int xAxis[] = new int[500];
    int yAxis[] = new int[500];
    int empty[] = new int[500];

    String textArray[] = new String[1000];

    public GraphicOverlay.Graphic textGraphic;
    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);

    public void setupResolution(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

     public void imageFromByteBuffer(ByteBuffer buffer, int rotation) {

        if (shouldThrottle.get()) {  // This is to drop frames when one frame is processing.if its value is true it will exit the function but if false it will execute.
            return;
        }

        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(mWidth)
                .setHeight(mHeight)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                .setRotation(rotation)
                .build();
        mGraphicOverlay.setCameraInfo(mHeight, mWidth, CameraCharacteristics.LENS_FACING_BACK);

        FirebaseVisionImage image = FirebaseVisionImage.fromByteBuffer(buffer, metadata);
        runTextRecognition(image);
    }

    public void runTextRecognition(FirebaseVisionImage image) {

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
        mGraphicOverlay.clear();

        Log.d("count", "currentCount: " + currentCount + " recognizerCount:" + recognizerCount);
        List<FirebaseVisionText.TextBlock> blocks = textRecognized.getTextBlocks(); // get the text from the image think textblocks as a paragraph

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {

                    textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    textOutput = elements.get(k).getText();
//todo:what if i can stop capturing more frames after capturing a couple of them the if their is a big change in the iamge

                    if (No_Of_Suggestions <= 0) {
                        mGraphicOverlay.add(textGraphic);
                    } /** This Condition Stops The Invalid Words From Overlaying */

                    Log.d("length", "Length: " + blocks.size());
                    //TODO:I think i have to define a Array which will find the extremes of both the xAxis and the yAxis.
                    // For example we will use the location of evey word.we will find out the highest x,y value and lowest x,y value.
                    // As the value will change constantly we will use a constant value and if the difference between the previous and the current is minor wont redraw the textBoxes and if true then vice versa
                    // i think i should declare a variable which will store the start value of the TTS Engine Which Will Be The Location of the first word.Also the location of last word.
                    // we can also define that value by touching the actual word from where we want to start reading.
                    // what if the text recognizer automatically recognizes the words from the frame according to the position of the word its reading.
                    // the max value will change every frame and we will use it the average of it to make decisions.

                    // I Will use the currentCount to wipe reset the array

                    //what if we can store the word and its location using a 3d array in which we will access the word and the location using a index

                    for (int l = 0; l < elements.size(); l++) {
                        textArray[l] = elements.get(k).getText();
                        xAxis[l] = elements.get(k).getBoundingBox().left;
                        yAxis[l] = elements.get(k).getBoundingBox().bottom;
                        textCoordinates.put(elements.get(k).getText(), xAxis[l]);
//                        Log.d("pos", "pos: " + xAxis[l]);
                    }


                    Log.d(TAG, "processTextRecognised: ");

                    /*The Perfect Plan is to recognizes the text and user should be able to tap on any text and start listining to the text.
                     * in background the recognizer will regularly scan the text and update the location of the words that will be stored in a map or a 3d array.
                     * The word on which the user will tap will become the word from where the TTS engine Stars.
                     * The Words that the TTS engine will speak will change its colour from transparent to white or black and they will be surrounded by a Blackish Translucent Overlay.*/

//                    if (recognizerCount >= 1 && (currentCount - recognizerCount) == 2) {
//                        positionChanger(elements.get(k), textGraphic);
//                    }

                    //TODO: We can also add a function which will scan the detected word and if even a little part of it matches with the previous text,We will use the words position to overlay the complete word.


//                     i think i need to create a map which will store a string(the actual word) and its position.By doing this we can use its location to draw text boxes more effectiently
//                     and also stop the text recognizer to save resources and if the position is changed then we will turn back text recognizer.
//                     we will use a constant if the change in position exceeds it we will turn on the text recognizer and if it not then continue
//                     I also have to clear the previous values overtime to stop duplication
//                     i can make a method for that if anything duplicate comes at the same position it will get rejected
//

//                     The plan is to Detect text and making sure each word is detected properly
                    // we can do this by taking a average of 10 to 15 frames if the position of the text does not change much from the orignal postion.
                    // if the position does not change much we will take 10 to 15 frames and make sure
                    // the text is same in each frame and even there is a slight difference we will add the text by comparing it to the previous text.
                    // we can use the page number to store text in a organised manner

                    // i think i have to make a new variable which will increment as the textRecognizer recognizes text.We can define the location at the end of the recognizer and the new locations will be obtained freshly from a extenal function which will measure the distance betweeen both.
                    // we can also use a map which will store text and its locations. the recognizerCount will be used to acces its previous locations using
                    /**I think i have to use to seprate map one will be the orignal text detected map which will store the orignal text and the other will track the realtime location of the text,
                     * this will come in handy when we want to create a black overlay on the text to be spoken
                     * Also if the text is correct and its position does not changes its overlay will not be cleared and not ne redrawn un*/
                    Log.d(TAG, textOutput);

//                    if ((currentCount - recognizerCount) == 2) {
//                        xAxis = elements.get(k).getBoundingBox().left;
//                        test = elements.get(k).getText();
//                        yAxis = elements.get(k).getBoundingBox().bottom;
//                    }

                }
            }
        }
        currentCount++;

        xMax = Arrays.stream(xAxis).max().getAsInt();
        yMax = Arrays.stream(yAxis).max().getAsInt();
        Log.d("posxy", "xMax: " + xMax + " | yMax:" + yMax + " | currentCount: " + currentCount);

        xAxis = empty.clone();
        yAxis = empty.clone();

//        if ((currentCount - recognizerCount) == 2) {
//            recognizerCount++;
//        }
    }

//    private void positionChanger(FirebaseVisionText.Element word, GraphicOverlay.Graphic textGraphic) {
//
//        Log.d("position", "Current position: " + word.getBoundingBox().left + " CurrentText: " + word.getText() + " Count: " + currentCount + " || Previous position: " + xAxis + " previousText: " + test + "  Count: " + recognizerCount);
//        if (recognizerCount > 0) {
//            Log.d("wordTest", " executed");
//            if ((word.getBoundingBox().left - xAxis) >= textPositionTolerance || (xAxis - word.getBoundingBox().left) >= textPositionTolerance) {
//                if ((word.getBoundingBox().bottom - yAxis) >= textPositionTolerance || (yAxis - word.getBoundingBox().bottom) >= textPositionTolerance) { //Todo: i think i have to fix the negative problem
//                    mGraphicOverlay.clear();
//                    mGraphicOverlay.add(textGraphic);
//                }
//            }
//        }
//
//    }
// i have to create a method which will revive the undetectd words from the old detecttion it will function in this way
    // we will use the currentCount and will use a if condition which will check if the new word is complete or uncomplete if uncompolete it will not erase the previous text or we will use the position to find the text
}


