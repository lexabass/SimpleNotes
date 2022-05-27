package com.example.simplenotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class createnote extends AppCompatActivity {

    EditText mcreatetitleofnote, mcreatecontentofnote;
    ImageView mcreateimageofnote, maddimage;
    FloatingActionButton msavenote;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage firebaseStorage;
    public static Uri imageUrl=null;

    int Image_Request_Code = 7;

    ProgressBar mprogressbarofcreatenote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createnote);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        maddimage=findViewById(R.id.addimage);
        msavenote=findViewById(R.id.savenote);
        mcreatecontentofnote=findViewById(R.id.createcontentofnote);
        mcreatetitleofnote=findViewById(R.id.createtitleofnote);
        mcreateimageofnote=findViewById(R.id.createimageofnote);

        mprogressbarofcreatenote = findViewById(R.id.progressbarofcreatenote);

        Toolbar toolbar=findViewById(R.id.toolbarofcreatenote);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        msavenote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveNote();
            }
        });

//        SELECT PHOTO

        maddimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Image_Request_Code);
            }
        });

//        END SELECT PHOTO

    }

    private void SaveNote() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите...");
        String title = mcreatetitleofnote.getText().toString();
        String content = mcreatecontentofnote.getText().toString();
        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
        Date date = new Date();

        if ((mcreateimageofnote.getDrawable() == null) && (!title.isEmpty())) {
            progressDialog.show();
            DocumentReference documentReference = firebaseFirestore.collection("notes").
                    document(firebaseUser.getUid()).collection("myNotes").document();
            Map<String, Object> note = new HashMap<>();
            note.put("title", title);
            note.put("search", title.toLowerCase());
            note.put("content", content);
            note.put("create_date", formatter.format(date));

            documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    startActivity(new Intent(createnote.this, notesActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Ошибка создания заметки", Toast.LENGTH_SHORT).show();
                    mprogressbarofcreatenote.setVisibility(View.INVISIBLE);
                }
            });
        }
        if (!title.isEmpty() && (mcreateimageofnote.getDrawable() != null)) {
            progressDialog.show();
            mprogressbarofcreatenote.setVisibility(View.VISIBLE);
            StorageReference filepath = firebaseStorage.getReference().child("images").child(imageUrl.getLastPathSegment());
            filepath.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            DocumentReference documentReference = firebaseFirestore.collection("notes").
                                    document(firebaseUser.getUid()).collection("myNotes").document();
                            Map<String, Object> note = new HashMap<>();
                            note.put("title", title);
                            note.put("search", title.toLowerCase());
                            note.put("content", content);
                            note.put("create_date", formatter.format(date));
                            note.put("image", task.getResult().toString());

                            documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    startActivity(new Intent(createnote.this, notesActivity.class));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Ошибка создания заметки", Toast.LENGTH_SHORT).show();
                                    mprogressbarofcreatenote.setVisibility(View.INVISIBLE);
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
        else {
            Toast.makeText(createnote.this, "Пожалуйста введите название", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Image_Request_Code && resultCode==RESULT_OK) {
            if (data!=null) {
                imageUrl = data.getData();
                mcreateimageofnote.setImageURI(imageUrl);
                mcreateimageofnote.setVisibility(View.VISIBLE);
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