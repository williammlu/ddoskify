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
package com.ddoskify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

import com.ddoskify.ui.camera.GraphicOverlay;

/**
 * Graphics class for rendering Googly Eyes on a graphic overlay given the current eye positions.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float EYE_RADIUS_PROPORTION = 0.9f;
    private static final float EYE_WIDTH_TO_FACE_WIDTH_RATIO = 0.4f;
    private static final float FACE_WIDTH_TO_HEIGHT_RATIO = 1/1.61f; // golden ratio!
    private static final float PI = 3.14159f;


    private static Bitmap mDdoskiIcon;
    private Matrix mMatrix;
    private boolean mIsFrontFacing;
    private volatile PointF mLeftPosition;
    private volatile PointF mRightPosition;

    private Canvas mCanvas;


    private Context context;

    //==============================================================================================
    // Methods
    //==============================================================================================

    FaceGraphic(GraphicOverlay overlay, Context c, boolean isFrontFacing) {
            super(overlay);
            context = c;

        mDdoskiIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ddoski);
        mMatrix = new Matrix();
        mIsFrontFacing = isFrontFacing;
    }

    /**
     * Updates the eye positions and state from the detection of the most recent frame.  Invalidates
     * the relevant portions of the overlay to trigger a redraw.
     */
    void updateEyes(PointF leftPosition, PointF rightPosition) {
        mLeftPosition = leftPosition;
        mRightPosition = rightPosition;

        postInvalidate();
    }

    /**
     * Draw image onto center of eyes.
     * Goals:
     * 1 - (COMPLETE) draw image between eyes
     * 2 - (COMPLETE) draw image to scale according to face size
     * 3 - (COMPLETE) draw image with correct angle between eyes
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
        float angleDegree = (float) Math.toDegrees(angle);
        float perpAngle = angle - PI/2;

//        Log.e("Eyes", "x:" + leftEye.x + "\ty: " + leftEye.y + "\tangle: " + angle * (180/3.14) + "\t perp: " + perpAngle * 180 /3.14 );


        // Using faceHeight/8 is by trial and error
        // Supposed to be faceHeight/6 assuming perfect facial proportions

        // ddoski appears better at eye centered level.
        float faceCenterX = eyeCenterX; //- (float) Math.cos(perpAngle) * 0*faceHeight/8;
        float faceCenterY = eyeCenterY; // + (float) Math.sin(perpAngle) * 0*faceHeight/8;


        // to give icon constant scaling factor
        float geoMeanScaling = (float) (1.2f * Math.sqrt((faceWidth * faceHeight)/(mDdoskiIcon.getWidth() * mDdoskiIcon.getHeight())));

        mMatrix.reset(); // mutate mMatrix = speed optimization
        mMatrix.postTranslate(-mDdoskiIcon.getWidth()/2, -mDdoskiIcon.getHeight()/2); // move center of image to top left corner

        mMatrix.postRotate(-angleDegree);
        if (mIsFrontFacing) {
            mMatrix.postRotate(180);
        }
        mMatrix.postScale(geoMeanScaling, geoMeanScaling);
        mMatrix.postTranslate(faceCenterX, faceCenterY);

        canvas.drawBitmap(mDdoskiIcon, mMatrix, new Paint());
//        Log.e("FaceGraphics", mMatrix.toShortString());


        // Testing below!
        /*
        Paint green = new Paint();
        green.setColor(Color.GREEN);
        green.setStyle(Paint.Style.STROKE);
        green.setStrokeWidth(5);
        Paint red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.FILL);

        canvas.drawCircle(faceCenterX, faceCenterY, faceWidth/2, green); // circle from face center
        canvas.drawCircle(eyeCenterX, eyeCenterY, 5, green); // eye center
        canvas.drawCircle(faceCenterX, faceCenterY, 5, red);
        canvas.drawLine(leftEye.x, leftEye.y, rightEye.x, rightEye.y, green);
        canvas.drawLine(eyeCenterX, eyeCenterY, faceCenterX, faceCenterY, red);
        */
        mCanvas = canvas;

    }

    public static Bitmap getDdoskiIcon() {
            return mDdoskiIcon;
    }


    public Matrix getMatrix() {
        return mMatrix;
    }

    public Canvas getCanvas() {
        return mCanvas;
    }

}
