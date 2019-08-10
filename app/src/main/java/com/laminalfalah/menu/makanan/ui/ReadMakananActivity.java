package com.laminalfalah.menu.makanan.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.laminalfalah.menu.makanan.R;
import com.laminalfalah.menu.makanan.adapter.MakananAdapter;
import com.laminalfalah.menu.makanan.model.Makanan;
import com.laminalfalah.menu.makanan.utils.Config;
import com.laminalfalah.menu.makanan.utils.ProgressBarUtils;
import com.laminalfalah.menu.makanan.utils.SnackBarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.laminalfalah.menu.makanan.utils.Config.DIRECTORY;

public class ReadMakananActivity extends AppCompatActivity implements MakananAdapter.OnMakananSelectedListener {

    private static final String TAG = ReadMakananActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.addMakanan) FloatingActionButton mAddMakanan;
    @BindView(R.id.rvMakanan) RecyclerView mRvMakanan;
    @BindView(R.id.viewEmpty) ViewGroup mEmptyView;
    @BindView(R.id.tvViewEmpty) TextView tvEmptyText;
    @BindView(R.id.shimmer_view_makanan) ShimmerFrameLayout mShimmerFrameLayout;

    private ProgressBarUtils progressBarUtils;
    private SnackBarUtils snackBarUtils;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference, mStorageMakanan;
    private Query mQuery;

    private MakananAdapter mAdapter;

    private Intent intent;

    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_makanan);
        ButterKnife.bind(this);

        mAddMakanan.hide();
        mEmptyView.setVisibility(View.GONE);
        mRvMakanan.setVisibility(View.GONE);

        //mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        progressBarUtils = new ProgressBarUtils(this);
        snackBarUtils = new SnackBarUtils(this);
        intent = new Intent();

        onInitFirebase();

        onRecyclerView();

        if (Config.DEVELOPMENT) {
            mAddMakanan.show();
        } else {
            mAddMakanan.hide();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShimmerFrameLayout.startShimmer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mShimmerFrameLayout.stopShimmer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AddEditMakananActivity.REQUEST_ADD) {
            if (resultCode == AddEditMakananActivity.RESULT_ADD_SUCCESS) {
                snackBarUtils.snackBarLong(getString(R.string.snack_bar_success,
                        getString(R.string.snack_bar_add).toLowerCase(),
                        getString(R.string.makanan).toLowerCase()
                ));
            } else if (resultCode == AddEditMakananActivity.RESULT_ADD_FAILED) {
                snackBarUtils.snackBarLong(getString(R.string.snack_bar_error,
                        data.getStringExtra("error")
                ));
            }
        } else if (requestCode == AddEditMakananActivity.REQUEST_UPDATE) {
            if (resultCode == AddEditMakananActivity.RESULT_UPDATE_SUCCESS) {
                snackBarUtils.snackBarLong(getString(R.string.snack_bar_success,
                        getString(R.string.snack_bar_update).toLowerCase(),
                        getString(R.string.makanan).toLowerCase()
                ));
            } else if (resultCode == AddEditMakananActivity.RESULT_UPDATE_FAILED) {
                snackBarUtils.snackBarLong(getString(R.string.snack_bar_error,
                        data.getStringExtra("error")
                ));
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_about);
        mStorageReference.child("me.jpeg").getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(this)
                        .asBitmap()
                        .placeholder(R.drawable.ic_account_circle_white_24dp)
                        .load(uri.toString())
                        .circleCrop()
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                menuItem.setIcon(new BitmapDrawable(resource));
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        }))
                .addOnFailureListener(e -> Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.ic_account_circle_white_24dp)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                menuItem.setIcon(new BitmapDrawable(resource));
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        }));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_about) {
            startActivity(new Intent(getApplicationContext(), TentangActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 3000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            snackBarUtils.snackBarLong(getString(R.string.msg_exit));
        }
        back_pressed = System.currentTimeMillis();
    }

    private void onInitFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();

        mFirestore = FirebaseFirestore.getInstance();

        mStorage = FirebaseStorage.getInstance();

        mStorageReference = mStorage.getReference();

        mQuery = mFirestore.collection(Makanan.COLLECTION)
                .orderBy(Makanan.TIMESTAMPS, Query.Direction.DESCENDING)
                .limit(100);
    }

    private void onRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No Query, not installizing Recycler View");
        }

        mRvMakanan.setLayoutManager(new LinearLayoutManager(this));
        mRvMakanan.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setChangeDuration(500);
        itemAnimator.setRemoveDuration(500);
        mRvMakanan.setItemAnimator(itemAnimator);

        if (mFirebaseAuth.getCurrentUser() != null) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {

                private final Drawable deleteIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_delete_white_24dp);
                private final Drawable editIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_edit_white_24dp);
                private final ColorDrawable backgroundDelete = new ColorDrawable(getResources().getColor(R.color.red));
                private final ColorDrawable backgroundEdit = new ColorDrawable(getResources().getColor(R.color.green));

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                    if (i == ItemTouchHelper.LEFT) {
                        mAdapter.OnDeleteMakanan(viewHolder.getAdapterPosition());
                    }
                    if (i == ItemTouchHelper.RIGHT) {
                        mAdapter.OnEditMakanan(viewHolder.getAdapterPosition());
                    }
                }

                @Override
                public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
                    return animationType == ItemTouchHelper.ANIMATION_TYPE_DRAG ? DEFAULT_DRAG_ANIMATION_DURATION
                            : DEFAULT_SWIPE_ANIMATION_DURATION;
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        View itemView = viewHolder.itemView;
                        int dXSwipe = (int) (dX * 1.05);
                        if (dX > 0) {
                            // draw background
                            backgroundEdit.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dXSwipe, itemView.getBottom());
                            backgroundEdit.draw(c);
                            // draw icon
                            int top = (itemView.getTop() + itemView.getBottom() - editIcon.getIntrinsicHeight()) / 2;
                            int left = itemView.getLeft() + 48;
                            editIcon.setBounds(left, top, left + editIcon.getIntrinsicWidth(), top + editIcon.getIntrinsicHeight());
                            editIcon.draw(c);
                        } else {
                            // draw background
                            backgroundDelete.setBounds(itemView.getRight() + dXSwipe, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                            backgroundDelete.draw(c);
                            // draw icon
                            int top = (itemView.getTop() + itemView.getBottom() - deleteIcon.getIntrinsicHeight()) / 2;
                            int right = itemView.getRight() - 48;
                            deleteIcon.setBounds(right - deleteIcon.getIntrinsicWidth(), top, right, top + deleteIcon.getIntrinsicHeight());
                            deleteIcon.draw(c);
                        }
                    }
                }

            }).attachToRecyclerView(mRvMakanan);
        }

        mAdapter = new MakananAdapter(mQuery, this) {
            @Override
            protected void onError(FirebaseFirestoreException e) {
                snackBarUtils.snackbarShort(e.getMessage());
            }

            @Override
            protected void onDataChanged() {
                new Handler().postDelayed(() -> {
                    mShimmerFrameLayout.stopShimmer();
                    mShimmerFrameLayout.setVisibility(View.GONE);
                    if (getItemCount() == 0) {
                        mEmptyView.setVisibility(View.VISIBLE);
                        mRvMakanan.setVisibility(View.GONE);
                        tvEmptyText.setText(getString(R.string.msg_data_empty));
                    } else {
                        mEmptyView.setVisibility(View.GONE);
                        mRvMakanan.setVisibility(View.VISIBLE);
                        mRvMakanan.setAdapter(mAdapter);
                    }
                }, 2000);
            }
        };
    }

    @Override
    public void onMakananSelectedEdit(DocumentSnapshot snapshot) {
        intent = new Intent(this, AddEditMakananActivity.class);
        intent.putExtra(AddEditMakananActivity.ID, snapshot.getId());
        startActivityForResult(intent, AddEditMakananActivity.REQUEST_UPDATE);
    }

    @Override
    public void onMakananSelectedDetail(DocumentSnapshot snapshot) {
        startActivity(new Intent(this, DetailMakananActivity.class)
                .putExtra(DetailMakananActivity.ID, snapshot.getId())
        );
    }

    @Override
    public void onMakananSelectedDelete(DocumentSnapshot snapshot) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle(getString(R.string.toolbar_makanan, getString(R.string.delete_title)));
        mBuilder.setCancelable(false);
        mBuilder.setIcon(R.drawable.ic_delete_red_24dp);
        mBuilder.setMessage(
                getString(R.string.msg_delete_makanan,
                        snapshot.getString(Makanan.FIELD_NAMA_MAKANAN),
                        getString(R.string.makanan)
                )
        );
        mBuilder.setPositiveButton(getString(R.string.positive_delete), (dialog, which) ->
                onDeleteMakanan(
                        snapshot.getId(),
                        snapshot.getString(Makanan.FIELD_NAMA_MAKANAN),
                        snapshot.getString(Makanan.FIELD_IMAGE_MAKANAN)
                )
        );
        mBuilder.setNegativeButton(getString(R.string.negative_delete), (dialog, which) -> {
            mAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });
        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
    }

    private void onDeleteMakanan(String document, String nama, String fotoMakanan) {
        mFirestore.collection(Makanan.COLLECTION).document(document).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mStorageMakanan = mStorageReference.child(DIRECTORY + fotoMakanan);
                        mStorageMakanan.delete();
                        snackBarUtils.snackBarLong(getString(R.string.snack_bar_success, getString(R.string.snack_bar_delete), nama));
                    } else {
                        snackBarUtils.snackBarLong(getString(R.string.msg_delete_failed, nama));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onDeleteMakanan: ", e);
                    snackBarUtils.snackBarLong(e.getMessage());
                });
    }

    @OnClick(R.id.addMakanan) void addMakanan() {
        intent = new Intent(this, AddEditMakananActivity.class);
        intent.putExtra(AddEditMakananActivity.ID, AddEditMakananActivity.ADD_DATA);
        startActivityForResult(intent, AddEditMakananActivity.REQUEST_ADD);
    }
}
