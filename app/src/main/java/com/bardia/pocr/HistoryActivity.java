package com.bardia.pocr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bardia.pocr.adapter.HistoryRecyclerViewAdapter;
import com.bardia.pocr.model.TextObjectDecoded;
import com.bardia.pocr.model.TextObjectEncoded;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    ArrayList<TextObjectEncoded> objectEncodedArrayList = new ArrayList<>();
    AlertDialog loading, dialog;
    RecyclerView recyclerView;
    HistoryRecyclerViewAdapter recyclerViewAdapter;
    DatabaseReference databaseReference;
    ArrayList<TextObjectDecoded> objectDecodedArrayList = new ArrayList<>();

    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.historyRecycler);
        imageView = findViewById(R.id.emptyImg);
        textView = findViewById(R.id.emptyTv);

        loading = loadingWindow(HistoryActivity.this).show();
        downloadData();

    }

    public void downloadData() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.v("History", dataSnapshot.getChildrenCount() + " ");
                objectEncodedArrayList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    objectEncodedArrayList.add(postSnapshot.getValue(TextObjectEncoded.class));
                }
                Log.v("LIST_OBJECTS", objectEncodedArrayList.size() + "");
                DecodeObjects thread = new DecodeObjects(objectEncodedArrayList);
                thread.execute();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.v("History", "Retrieving data failed");
            }
        });
    }

    public class DecodeObjects extends AsyncTask<Void, Void, Void> {

        ArrayList<TextObjectEncoded> objectEncodedArrayList;

        public DecodeObjects(ArrayList<TextObjectEncoded> objectEncodedArrayList) {
            this.objectEncodedArrayList = objectEncodedArrayList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            byte[] imageBytes;
            objectDecodedArrayList = new ArrayList<>();
            for (int i = 0; i < objectEncodedArrayList.size(); i++) {
                Log.v("LIST_OBJECTS", objectEncodedArrayList.get(i).getText());
                imageBytes = Base64.decode(objectEncodedArrayList.get(i).getImage(), Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                TextObjectDecoded objectDecoded = new TextObjectDecoded(
                        objectEncodedArrayList.get(i).getText(),
                        objectEncodedArrayList.get(i).getDate(),
                        decodedImage);
                objectDecodedArrayList.add(objectDecoded);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (objectDecodedArrayList.size() > 0) {
                imageView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                recyclerViewAdapter = new HistoryRecyclerViewAdapter(objectDecodedArrayList, itemClickListener(), showItemDetail());
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(HistoryActivity.this);
                recyclerView.setAdapter(recyclerViewAdapter);
                recyclerView.setLayoutManager(layoutManager);
                Log.v("RECYCLERVIEW", "ArrayList items: " + objectDecodedArrayList.size()
                        + ", Adapter items: " + recyclerViewAdapter.getItemCount()
                        + ", Recyclerview items: " + recyclerView.getAdapter().getItemCount());
                loading.dismiss();
            } else {
                imageView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                loading.dismiss();
            }
        }
    }

    private HistoryRecyclerViewAdapter.OnItemClickListener showItemDetail() {
        HistoryRecyclerViewAdapter.OnItemClickListener listener = new HistoryRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TextObjectDecoded objectDecoded, int adapterPosition) {
                dialog = itemDetail(HistoryActivity.this, objectDecoded).show();
            }
        };
        return listener;
    }


    public AlertDialog.Builder loadingWindow(Context context) {
        LayoutInflater li = LayoutInflater.from(HistoryActivity.this);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.loadingTitle))
                .setView(layout)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        return builder;
    }

    public HistoryRecyclerViewAdapter.OnItemClickListener itemClickListener() {
        HistoryRecyclerViewAdapter.OnItemClickListener listener = new HistoryRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final TextObjectDecoded objectDecoded, final int adapterPosition) {
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle(getResources().getString(R.string.deleteItemTitle))
                        .setMessage(getResources().getString(R.string.deleteItemMessage))
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                                Query itemToDelete = reference.child("users").child(FirebaseAuth.getInstance().getUid()).orderByChild("date").equalTo(objectDecoded.getDate());
                                itemToDelete.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                            snapshot.getRef().removeValue();
                                        }
                                        objectDecodedArrayList.remove(adapterPosition);
                                        recyclerViewAdapter.notifyItemRemoved(adapterPosition);
                                        Toast.makeText(HistoryActivity.this, getResources().getString(R.string.deleteSuccess), Toast.LENGTH_LONG).show();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(HistoryActivity.this, getResources().getString(R.string.deleteFailed), Toast.LENGTH_LONG).show();
                                        Log.e("Error", "onCancelled", databaseError.toException());
                                    }
                                });
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), null)
                        .show();
            }
        };
        return listener;
    }

    public AlertDialog.Builder itemDetail(Context context, final TextObjectDecoded objectDecoded) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setNeutralButton(getResources().getString(R.string.ok), null);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) layoutInflater.inflate(getResources().getLayout(R.layout.history_item_detail_layout), null);
        ImageView imageView = layout.findViewById(R.id.historyItemDetailImage);
        TextView textView = layout.findViewById(R.id.historyItemDetailText);
        Button button = layout.findViewById(R.id.historyCopyClipboard);
        Button share = layout.findViewById(R.id.historyShare);
        imageView.setImageBitmap(objectDecoded.getImage());
        textView.setText(objectDecoded.getText());
        builder.setView(layout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString(R.string.app_name), objectDecoded.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(HistoryActivity.this, getResources().getString(R.string.copiedToClipboard), Toast.LENGTH_LONG).show();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, objectDecoded.getText());
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        });
        dialog = builder.create();
        return builder;
    }
}
