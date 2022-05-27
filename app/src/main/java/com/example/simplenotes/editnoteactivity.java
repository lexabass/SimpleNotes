package com.example.simplenotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class editnoteactivity extends AppCompatActivity {

    Intent data;
    EditText medittitleofnote, meditcontentofnote;
    TextView mcreate_date_edit;
    FloatingActionButton msaveeditnote;
    ImageView meditimageofnote, maddimage;

    FirebaseStorage firebaseStorage;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    ProgressBar mprogressbarofeditnote;

    public Uri imageUri;
    public static Uri imageUrl=null;
    int Image_Request_Code = 7;

    public Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editnoteactivity);

        data = getIntent();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();

        mprogressbarofeditnote = findViewById(R.id.progressbarofeditnote);

        maddimage=findViewById(R.id.addimage);
        meditimageofnote = findViewById(R.id.editimageofnote);
        medittitleofnote = findViewById(R.id.edittitleofnote);
        meditcontentofnote = findViewById(R.id.editcontentofnote);
        mcreate_date_edit = findViewById(R.id.create_date_edit);

        msaveeditnote = findViewById(R.id.saveeditnote);
        Toolbar toolbar = findViewById(R.id.toolbarofeditnote);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Query query = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").orderBy("create_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<firebasemodel> allusernotes = new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query,firebasemodel.class).build();
        
        msaveeditnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateNote();
            }
        });

        maddimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Image_Request_Code);
            }
        });

        Bundle extras = getIntent().getExtras();
        byte[] byteArray = extras.getByteArray("image");

        if (byteArray != null) {
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            meditimageofnote.setVisibility(View.VISIBLE);
            meditimageofnote.setImageBitmap(bitmap);

            String notedate = data.getStringExtra("create_date");
            String notetitle = data.getStringExtra("title");
            String notecontent = data.getStringExtra("content");
            mcreate_date_edit.setText(notedate);
            meditcontentofnote.setText(notecontent);
            medittitleofnote.setText(notetitle);
        }
        else if (byteArray == null) {
            String notedate = data.getStringExtra("create_date");
            String notetitle = data.getStringExtra("title");
            String notecontent = data.getStringExtra("content");
            mcreate_date_edit.setText(notedate);
            meditcontentofnote.setText(notecontent);
            medittitleofnote.setText(notetitle);
        }

    }


    private void UpdateNote() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите...");
        String newtitle = medittitleofnote.getText().toString();
        String newcontent = meditcontentofnote.getText().toString();
        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
        Date date = new Date();


        if (!newtitle.isEmpty() && (imageUrl != null))  {
            progressDialog.show();
            StorageReference filepath = firebaseStorage.getReference().child("images").child(imageUrl.getLastPathSegment());
            filepath.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            DocumentReference documentReference = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                            Map<String,Object> note = new HashMap<>();
                            note.put("search", newtitle.toLowerCase());
                            note.put("title", newtitle);
                            note.put("content", newcontent);
                            note.put("create_date" , formatter.format(date));
                            note.put("image", task.getResult().toString());
                            documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Заметка сохранена", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(editnoteactivity.this, notesActivity.class));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                                    mprogressbarofeditnote.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    });
                };
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Загружено "+ (int) progress + "%");
                }
            });

        }
        else if (!newtitle.isEmpty() && (imageUrl == null))  {
            if (meditimageofnote.getDrawable() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                byte[] byte_image = baos.toByteArray();
                progressDialog.show();
                StorageReference filepath = firebaseStorage.getReference().child("images").child(byte_image.toString());
                filepath.putBytes(byte_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                DocumentReference documentReference = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                                Map<String,Object> note = new HashMap<>();
                                note.put("search", newtitle.toLowerCase());
                                note.put("title", newtitle);
                                note.put("content", newcontent);
                                note.put("create_date" , formatter.format(date));
                                note.put("image", task.getResult().toString());
                                documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Заметка сохранена", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(editnoteactivity.this, notesActivity.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                                        mprogressbarofeditnote.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        });
                    };
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Загружено "+ (int) progress + "%");
                    }
                });
            }
            else if (meditimageofnote.getDrawable() == null) {
                DocumentReference documentReference = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                Map<String,Object> note = new HashMap<>();
                note.put("search", newtitle.toLowerCase());
                note.put("title", newtitle);
                note.put("content", newcontent);
                note.put("create_date" , formatter.format(date));
                documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(), "Заметка сохранена", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(editnoteactivity.this, notesActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                        mprogressbarofeditnote.setVisibility(View.INVISIBLE);
                    }
                });
            }
                        }
        else {
            Toast.makeText(editnoteactivity.this, "Пожалуйста введите название", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Image_Request_Code && resultCode==RESULT_OK) {
            if (data!=null) {
                imageUrl = data.getData();
                meditimageofnote.setImageURI(imageUrl);
                meditimageofnote.setVisibility(View.VISIBLE);
            }
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