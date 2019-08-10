package com.laminalfalah.menu.makanan.ui;

import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.laminalfalah.menu.makanan.R;
import com.laminalfalah.menu.makanan.model.Makanan;

import java.text.DecimalFormat;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.laminalfalah.menu.makanan.utils.Config.DIRECTORY;

public class DetailMakananActivity extends AppCompatActivity implements EventListener<DocumentSnapshot> {

    private static final String TAG = DetailMakananActivity.class.getSimpleName();

    public static final String ID = "id";

    @BindView(R.id.gambarMakanan) AppCompatImageView mGambarMakanan;
    @BindView(R.id.ratingMakanan) AppCompatRatingBar mRatingMakanan;
    @BindView(R.id.hargaMakanan) AppCompatTextView tvHargaMakanan;
    @BindView(R.id.titleMakanan) AppCompatTextView tvTitleMakanan;
    @BindView(R.id.deskripsiMakanan) AppCompatTextView tvDeskripsiMakanan;

    private FirebaseFirestore mFirestore;
    private DocumentReference mDocumentReference;
    private ListenerRegistration mRegistration;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private String id;
    private DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_makanan);
        ButterKnife.bind(this);

        id = getIntent().getExtras().getString(ID);

        if (id == null) {
            throw new IllegalArgumentException("must be implement " + id);
        }

        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        df = new DecimalFormat("#,###,###");
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDocumentReference = mFirestore.collection(Makanan.COLLECTION).document(id);

        mRegistration = mDocumentReference.addSnapshotListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }
    }

    @Override
    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
        if (e != null) {
            Log.e(TAG, "onEvent: ", e);
        }

        Makanan makanan = documentSnapshot.toObject(Makanan.class);
        setDataMakanan(makanan);
    }

    private void setDataMakanan(Makanan makanan) {
        mStorageReference
                .child(DIRECTORY + makanan.getFotoMakanan())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(this)
                        .applyDefaultRequestOptions(new RequestOptions().centerCrop().placeholder(R.drawable.background_makanan))
                        .load(uri.toString())
                        .centerCrop()
                        .into(mGambarMakanan))
                .addOnFailureListener(e -> Glide.with(this)
                        .load(R.drawable.background_makanan)
                        .centerCrop()
                        .into(mGambarMakanan));
        tvTitleMakanan.setText(makanan.getNamaMakanan());
        tvHargaMakanan.setText(getString(R.string.hint_harga_makanan_1, df.format(makanan.getHargaMakanan())));
        mRatingMakanan.setRating((float) makanan.getRatingMakanan());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvDeskripsiMakanan.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
        tvDeskripsiMakanan.setText(makanan.getDeskripsiMakanan());
    }

    @OnClick(R.id.backHome) void back() {
        onBackPressed();
    }
}
