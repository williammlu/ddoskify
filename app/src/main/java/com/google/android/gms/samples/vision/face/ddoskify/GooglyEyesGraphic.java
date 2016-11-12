/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.ddoskify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.samples.vision.face.ddoskify.ui.camera.GraphicOverlay;

/**
 * Graphics class for rendering Googly Eyes on a graphic overlay given the current eye positions.
 */
class GooglyEyesGraphic extends GraphicOverlay.Graphic {
    private static final float EYE_RADIUS_PROPORTION = 0.9f;
    private static final float IRIS_RADIUS_PROPORTION = EYE_RADIUS_PROPORTION / 2.0f;
    private static final float EYE_WIDTH_TO_FACE_WIDTH_RATIO = 0.4f;
    private static final float FACE_WIDTH_TO_HEIGHT_RATIO = 1/1.61f; // golden ratio!
    private static final float EYE_HEIGHT_TO_FACE_HEIGHT_RATIO = 1/3.0f; // (eye to center of head)/(face height)
    private static Bitmap catIcon;


    private Matrix matrix;
    private Paint mEyeWhitesPaint;
    private Paint mEyeIrisPaint;
    private Paint mEyeOutlinePaint;
    private Paint mEyeLidPaint;

    // Keep independent physics state for each eye.
    private EyePhysics mLeftPhysics = new EyePhysics();
    private EyePhysics mRightPhysics = new EyePhysics();

    private volatile PointF mLeftPosition;
    private volatile boolean mLeftOpen;

    private volatile PointF mRightPosition;
    private volatile boolean mRightOpen;


    private Context context;

    //==============================================================================================
    // Methods
    //==============================================================================================

    GooglyEyesGraphic(GraphicOverlay overlay, Context c) {
        super(overlay);
        context = c;

        mEyeWhitesPaint = new Paint();
        mEyeWhitesPaint.setColor(Color.WHITE);
        mEyeWhitesPaint.setStyle(Paint.Style.FILL);

        mEyeLidPaint = new Paint();
        mEyeLidPaint.setColor(Color.YELLOW);
        mEyeLidPaint.setStyle(Paint.Style.FILL);

        mEyeIrisPaint = new Paint();
        mEyeIrisPaint.setColor(Color.BLACK);
        mEyeIrisPaint.setStyle(Paint.Style.FILL);

        mEyeOutlinePaint = new Paint();
        mEyeOutlinePaint.setColor(Color.BLACK);
        mEyeOutlinePaint.setStyle(Paint.Style.STROKE);
        mEyeOutlinePaint.setStrokeWidth(5);

        catIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.catface);
        matrix = new Matrix();
    }

    /**
     * Updates the eye positions and state from the detection of the most recent frame.  Invalidates
     * the relevant portions of the overlay to trigger a redraw.
     */
    void updateEyes(PointF leftPosition, boolean leftOpen,
                    PointF rightPosition, boolean rightOpen) {
        mLeftPosition = leftPosition;
        mLeftOpen = leftOpen;

        mRightPosition = rightPosition;
        mRightOpen = rightOpen;

        postInvalidate();
    }

    /**
     * Draw image onto center of eyes.
     * Goals:
     * 1 - draw image between eyes
     * 2 - draw image to scale according to face size
     * 3 - draw image with correct angle between eyes
     * 4 - skew image according to face tilt, may not be possible due to a lack of data points to
     * detect rotational angle of face.
    */
    @Override
    public void draw(Canvas canvas) {
        PointF detectLeftPosition = mLeftPosition;
        PointF detectRightPosition = mRightPosition;
        if ((detectLeftPosition == null) || (detectRightPosition == null)) {
            return;
        }

        PointF leftPosition =
                new PointF(translateX(detectLeftPosition.x), translateY(detectLeftPosition.y));
        PointF rightPosition =
                new PointF(translateX(detectRightPosition.x), translateY(detectRightPosition.y));

        drawFace(canvas, leftPosition, rightPosition);
//        // Use the inter-eye distance to set the size of the eyes.
//        float distance = (float) Math.sqrt(
//                Math.pow(rightPosition.x - leftPosition.x, 2) +
//                Math.pow(rightPosition.y - leftPosition.y, 2));
//        float eyeRadius = EYE_RADIUS_PROPORTION * distance;
//        float irisRadius = IRIS_RADIUS_PROPORTION * distance;
//
//        // Advance the current left iris position, and draw left eye.
//        PointF leftIrisPosition =
//                mLeftPhysics.nextIrisPosition(leftPosition, eyeRadius, irisRadius);
//        drawEye(canvas, leftPosition, eyeRadius, leftIrisPosition, irisRadius, mLeftOpen);
//
//        // Advance the current right iris position, and draw right eye.
//        PointF rightIrisPosition =
//                mRightPhysics.nextIrisPosition(rightPosition, eyeRadius, irisRadius);
//        drawEye(canvas, rightPosition, eyeRadius, rightIrisPosition, irisRadius, mRightOpen);
    }

    /**
     * Draws the eye, either closed or open with the iris in the current position.
     */
    private void drawEye(Canvas canvas, PointF eyePosition, float eyeRadius,
                         PointF irisPosition, float irisRadius, boolean isOpen) {
        if (isOpen) {
            Bitmap catIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.catface);
//            canvas.drawBitmap(catIcon, eyePosition.x, eyePosition.y, mEyeWhitesPaint);
//            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeWhitesPaint);
//            canvas.drawCircle(irisPosition.x, irisPosition.y, irisRadius, mEyeIrisPaint);
        } else {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeLidPaint);
            float y = eyePosition.y;
            float start = eyePosition.x - eyeRadius;
            float end = eyePosition.x + eyeRadius;
            canvas.drawLine(start, y, end, y, mEyeOutlinePaint);
        }
        canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeOutlinePaint);
    }

    /**
     * Draw a simple figure onto person's face. Use center of person's face as center for image.
     * @param canvas
     * @param leftEye
     * @param rightEye
     */
    private void drawFace(Canvas canvas, PointF leftEye, PointF rightEye) {
        float distX = rightEye.x - leftEye.x;
        float distY = rightEye.y - leftEye.y;

        float eyeWidth = (float) Math.sqrt(distX * distX + distY * distY);
        float faceWidth = eyeWidth/EYE_WIDTH_TO_FACE_WIDTH_RATIO;
        float faceHeight = faceWidth/FACE_WIDTH_TO_HEIGHT_RATIO;

        float eyeCenterX  = (leftEye.x + rightEye.x)/2;
        float eyeCenterY = (leftEye.y + rightEye.y)/2;


        float angle = (float) Math.atan2(distY, -distX);
        float angleDegree = angle  * 180 / 3.14f;
        float perpAngle = angle - 3.14f/2;

        Log.e("Eyes", "x:" + leftEye.x + "\ty: " + leftEye.y + "\tangle: " + angle * (180/3.14) + "\t perp: " + perpAngle * 180 /3.14 );


        // Using faceHeight/8 is by trial and error
        // Supposed to be faceHeight/6 assuming perfect facial proportions
        float faceCenterX = eyeCenterX - (float) Math.cos(perpAngle) * faceHeight/8;
        float faceCenterY = eyeCenterY + (float) Math.sin(perpAngle) * faceHeight/8;


        // to give icon constant scaling factor
        float geoMeanScaling = (float) Math.sqrt((faceWidth * faceHeight)/(catIcon.getWidth() * catIcon.getHeight()));

        matrix.reset(); // mutate matrix = speed optimization
        matrix.postTranslate(-catIcon.getWidth()/2, -catIcon.getHeight()/2); // move center of image to top left corner
        matrix.postRotate(-angleDegree + 180);
        matrix.postScale(geoMeanScaling, geoMeanScaling);
        matrix.postTranslate(faceCenterX, faceCenterY);
        
        canvas.drawBitmap(catIcon, matrix, new Paint());

//        canvas.drawBitmap(catIcon, faceCenterX - faceWidth/2, faceCenterY - faceHeight/2, mEyeWhitesPaint);

        // Testing below!
        Paint green = new Paint();
        green.setColor(Color.GREEN);
        green.setStyle(Paint.Style.STROKE);
        green.setStrokeWidth(5);
        canvas.drawCircle(faceCenterX, faceCenterY, faceWidth/2, green); // circle from face center
        canvas.drawCircle(eyeCenterX, eyeCenterY, 5, green); // eye center



        Paint red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.FILL);
//        canvas.drawCircle(faceCenterX, faceCenterY, 5, red);

        canvas.drawLine(leftEye.x, leftEye.y, rightEye.x, rightEye.y, green);
        canvas.drawLine(eyeCenterX, eyeCenterY, faceCenterX, faceCenterY, red);


    }
}
