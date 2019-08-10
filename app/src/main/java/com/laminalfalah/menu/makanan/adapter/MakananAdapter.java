package com.laminalfalah.menu.makanan.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.laminalfalah.menu.makanan.R;
import com.laminalfalah.menu.makanan.model.Makanan;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.laminalfalah.menu.makanan.utils.Config.DIRECTORY;

public class MakananAdapter extends FirestoreAdapter<MakananAdapter.ViewHolder> {

    private final OnMakananSelectedListener mListener;

    public MakananAdapter(Query query, OnMakananSelectedListener mListener) {
        super(query);
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_menu_makanan, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    public void OnEditMakanan(int position) {
        if (mListener != null) {
            mListener.onMakananSelectedEdit(getSnapshot(position));
        }
    }

    public void OnDeleteMakanan(int position) {
        if (mListener != null) {
            mListener.onMakananSelectedDelete(getSnapshot(position));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgMakanan) AppCompatImageView mImageMakanan;
        @BindView(R.id.txtTitleMakanan) AppCompatTextView mTitleMakanan;
        @BindView(R.id.txtHargaMakanan) AppCompatTextView mHargaMakanan;

        private final FirebaseStorage mStorage;
        private final StorageReference mStorageReference;
        private final DecimalFormat df;
        private Drawable mDrawable;
        private BitmapDrawable mBitmapDrawable;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mStorage = FirebaseStorage.getInstance();
            mStorageReference = mStorage.getReference();
            df = new DecimalFormat("#,###,###");
        }

        void bind(DocumentSnapshot snapshot, OnMakananSelectedListener mListener) {
            Makanan makanan = snapshot.toObject(Makanan.class);
            mTitleMakanan.setText(makanan.getNamaMakanan());
            mHargaMakanan.setText(itemView.getResources().getString(R.string.hint_harga_makanan_1,
                    df.format(makanan.getHargaMakanan()))
            );
            mDrawable = mImageMakanan.getDrawable();
            mBitmapDrawable = mDrawable instanceof BitmapDrawable ? (BitmapDrawable) mDrawable : null;
            if (mBitmapDrawable == null || mBitmapDrawable.getBitmap() == null) {
                mStorageReference
                        .child(DIRECTORY + makanan.getFotoMakanan())
                        .getDownloadUrl()
                        .addOnSuccessListener(uri -> Glide.with(itemView.getContext())
                                .applyDefaultRequestOptions(new RequestOptions().circleCrop().placeholder(R.drawable.background_makanan_rounded))
                                .load(uri.toString())
                                .circleCrop()
                                .into(mImageMakanan))
                        .addOnFailureListener(e -> Glide.with(itemView.getContext())
                                .load(R.drawable.background_makanan)
                                .circleCrop()
                                .into(mImageMakanan));
            }
            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onMakananSelectedDetail(snapshot);
                }
            });
        }
    }

    public interface OnMakananSelectedListener {
        void onMakananSelectedEdit(DocumentSnapshot snapshot);
        void onMakananSelectedDetail(DocumentSnapshot snapshot);
        void onMakananSelectedDelete(DocumentSnapshot snapshot);
    }
}
