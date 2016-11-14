package com.ddoskify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

public class PhotoReviewActivity extends AppCompatActivity {

    private Button shareFacebookButton;
    private ImageView takenImageView;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_review);

        Intent intent = getIntent();
        imageBitmap = (Bitmap) BitmapFactory.decodeFile(intent.getStringExtra(CameraOverlayActivity.BITMAP_MESSAGE));
        takenImageView = (ImageView) findViewById(R.id.takenImageView);

        takenImageView.setImageBitmap(imageBitmap);

        shareFacebookButton = (Button) findViewById(R.id.facebookShareButton);

        shareFacebookButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("PhotoReview", "sharing on fb");
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(imageBitmap)
                        .setCaption("Ddoskis at CalHacks 3.0!")
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                ShareDialog shareDialog = new ShareDialog(PhotoReviewActivity.this);
                shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);

            }
        });
    }
}