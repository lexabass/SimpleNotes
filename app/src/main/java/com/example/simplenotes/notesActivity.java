package com.example.simplenotes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class notesActivity extends AppCompatActivity {

    public String imageUri;

    private FirebaseAuth firebaseAuth;
    FloatingActionButton mcreatenotesfab;

    SearchView searchView;

    RecyclerView mrecyclerView;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    FirebaseStorage firebaseStorage;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder> noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        getSupportActionBar().setTitle("Все заметки");

        searchView = findViewById(R.id.search);

        mcreatenotesfab = findViewById(R.id.createnotefab);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mcreatenotesfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(notesActivity.this, createnote.class));
            }
        });

        Query query = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").orderBy("create_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<firebasemodel> allusernotes = new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query,firebasemodel.class).build();

        noteAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusernotes) {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel firebasemodel) {

                ImageView popupbutton = noteViewHolder.itemView.findViewById(R.id.menupopbutton);

                int colorcode = getRandomColor();
                noteViewHolder.mnote.setBackgroundColor(noteViewHolder.itemView.getResources().getColor(colorcode, null));

                noteViewHolder.notetitle.setText(firebasemodel.getTitle());
                noteViewHolder.notecontent.setText(firebasemodel.getContent());
                firebasemodel.getCreate_date();
                firebasemodel.getSearch();

                imageUri = null;
                imageUri = firebasemodel.getImage();
                Picasso.get().load(imageUri).into(noteViewHolder.noteimage);

                String docid = noteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //open note detail activity

                        if (noteViewHolder.noteimage.getDrawable() !=null ) {
                            Bitmap bitmap = ((BitmapDrawable) noteViewHolder.noteimage.getDrawable()).getBitmap();
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                            byte[] byteArray = stream.toByteArray();

                            Intent intent = new Intent(view.getContext(),notedetails.class);
                            intent.putExtra("title", firebasemodel.getTitle());
                            intent.putExtra("search", firebasemodel.getSearch());
                            intent.putExtra("content", firebasemodel.getContent());
                            intent.putExtra("create_date", firebasemodel.getCreate_date());
                            intent.putExtra("image", byteArray);
                            intent.putExtra("noteId", docid);
                            view.getContext().startActivity(intent);
                        }
                        else if (noteViewHolder.noteimage.getDrawable() == null ) {
                            Intent intent = new Intent(view.getContext(),notedetails.class);
                            intent.putExtra("title", firebasemodel.getTitle());
                            intent.putExtra("search", firebasemodel.getSearch());
                            intent.putExtra("content", firebasemodel.getContent());
                            intent.putExtra("create_date", firebasemodel.getCreate_date());
                            intent.putExtra("noteId", docid);
                            view.getContext().startActivity(intent);
                        }
                    }
                });

                popupbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Изменить").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                if (noteViewHolder.noteimage.getDrawable() !=null ) {
                                    Bitmap bitmap = ((BitmapDrawable) noteViewHolder.noteimage.getDrawable()).getBitmap();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                                    byte[] byteArray = stream.toByteArray();

                                    Intent intent = new Intent(view.getContext(),editnoteactivity.class);
                                    intent.putExtra("title", firebasemodel.getTitle());
                                    intent.putExtra("search", firebasemodel.getSearch());
                                    intent.putExtra("content", firebasemodel.getContent());
                                    intent.putExtra("create_date", firebasemodel.getCreate_date());
                                    intent.putExtra("image", byteArray);
                                    intent.putExtra("noteId", docid);
                                    view.getContext().startActivity(intent);
                                }
                                else if (noteViewHolder.noteimage.getDrawable() == null ) {
                                    Intent intent = new Intent(view.getContext(),editnoteactivity.class);
                                    intent.putExtra("title", firebasemodel.getTitle());
                                    intent.putExtra("search", firebasemodel.getSearch());
                                    intent.putExtra("content", firebasemodel.getContent());
                                    intent.putExtra("create_date", firebasemodel.getCreate_date());
                                    intent.putExtra("noteId", docid);
                                    view.getContext().startActivity(intent);
                                }
                                return false;
                            }
                        });
                        popupMenu.getMenu().add("Удалить").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference documentReference = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(docid);
                                documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(view.getContext(), "Заметка удалена", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(view.getContext(), "Ошибка удаления заметки", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });

            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        mrecyclerView = findViewById(R.id.recyclerview);
        mrecyclerView.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mrecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mrecyclerView.setAdapter(noteAdapter);

    }



    public class NoteViewHolder extends RecyclerView.ViewHolder {

        public TextView notetitle;
        public TextView notecontent;
        public ImageView noteimage;
        public LinearLayout mnote;


        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle = itemView.findViewById(R.id.notetitle);
            notecontent = itemView.findViewById(R.id.notecontent);
            noteimage = itemView.findViewById(R.id.noteimage);
            mnote = itemView.findViewById(R.id.note);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);

        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                return true;
            }
        };
        menu.findItem(R.id.search).setOnActionExpandListener(onActionExpandListener);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("Что ищете...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchText) {
                search(searchText);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                search(searchText);
                return false;
            }
        });
        return true;
    }

    private void search(String searchText) {
        Query searchquery = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).
                collection("myNotes").orderBy("search").startAt(searchText).endAt(searchText+"\uf8ff");
        FirestoreRecyclerOptions<firebasemodel> searchnotes = new FirestoreRecyclerOptions.Builder<firebasemodel>()
                .setQuery(searchquery,firebasemodel.class).
                        build();
        noteAdapter.updateOptions(searchnotes);
        noteAdapter.startListening();
        mrecyclerView.setAdapter(noteAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(notesActivity.this, MainActivity.class));
                return true;
            case R.id.about:
                startActivity(new Intent(notesActivity.this, about.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (noteAdapter!=null) {
            noteAdapter.startListening();
        }
    }

    private int getRandomColor() {
        List<Integer> colorcode = new ArrayList<>();
        colorcode.add(R.color.pink);
        colorcode.add(R.color.delicate_green);
        colorcode.add(R.color.dusty_green);
        colorcode.add(R.color.light_blue);
        colorcode.add(R.color.light_pink);
        colorcode.add(R.color.beige);
        colorcode.add(R.color.orange);
        colorcode.add(R.color.red_pink);
        colorcode.add(R.color.frost_color);
        colorcode.add(R.color.green_brown);
        colorcode.add(R.color.yellow_green);
        colorcode.add(R.color.light_green);
        colorcode.add(R.color.gray_2);
        colorcode.add(R.color.cyan);

        Random random = new Random();
        int number=random.nextInt(colorcode.size());
        return colorcode.get(number);
    }
}