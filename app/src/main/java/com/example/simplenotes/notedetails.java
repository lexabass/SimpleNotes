package com.example.simplenotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;


public class notedetails extends AppCompatActivity {

    private TextView mtitleofnotedetail, mcontentofnotedetail, mcreate_date_detail;
    FloatingActionButton mgotoeditnote;
    ImageView mimageofnotedetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notedetails);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        mtitleofnotedetail = findViewById(R.id.titleofnotedetail);
        mcontentofnotedetail = findViewById(R.id.contentofnotedetail);
        mcreate_date_detail = findViewById(R.id.create_date_detail);
        mimageofnotedetail = findViewById(R.id.imageofnotedetail);

        mgotoeditnote = findViewById(R.id.gotoeditnote);
        Toolbar toolbar = findViewById(R.id.toolbarofnotedetail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent data = getIntent();

        mgotoeditnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mimageofnotedetail.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable) mimageofnotedetail.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    byte[] byteArray = stream.toByteArray();

                    //go to edit note
                    Intent intent = new Intent(view.getContext(), editnoteactivity.class);
                    intent.putExtra("title", data.getStringExtra("title"));
                    intent.putExtra("search", data.getStringExtra("search"));
                    intent.putExtra("content", data.getStringExtra("content"));
                    intent.putExtra("create_date", data.getStringExtra("create_date"));
                    intent.putExtra("noteId", data.getStringExtra("noteId"));
                    intent.putExtra("image", byteArray);
                    view.getContext().startActivity(intent);
                }
                else if (mimageofnotedetail.getDrawable() == null) {
                    Intent intent = new Intent(view.getContext(), editnoteactivity.class);
                    intent.putExtra("title", data.getStringExtra("title"));
                    intent.putExtra("search", data.getStringExtra("search"));
                    intent.putExtra("content", data.getStringExtra("content"));
                    intent.putExtra("create_date", data.getStringExtra("create_date"));
                    intent.putExtra("noteId", data.getStringExtra("noteId"));
                    view.getContext().startActivity(intent);
                }
            }
        });



            Bundle extras = getIntent().getExtras();
            byte[] byteArray = extras.getByteArray("image");
            if (byteArray != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                mimageofnotedetail.setVisibility(View.VISIBLE);
                mimageofnotedetail.setImageBitmap(bitmap);
                mcreate_date_detail.setText(data.getStringExtra("create_date"));
                mcontentofnotedetail.setText(data.getStringExtra("content"));
                mtitleofnotedetail.setText(data.getStringExtra("title"));
            }
            else if (byteArray == null) {
                mcreate_date_detail.setText(data.getStringExtra("create_date"));
                mcontentofnotedetail.setText(data.getStringExtra("content"));
                mtitleofnotedetail.setText(data.getStringExtra("title"));
            }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}