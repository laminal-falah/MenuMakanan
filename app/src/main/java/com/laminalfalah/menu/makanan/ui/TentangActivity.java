package com.laminalfalah.menu.makanan.ui;

import android.animation.ValueAnimator;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.laminalfalah.menu.makanan.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.laminalfalah.menu.makanan.utils.Config.EMAIL;
import static com.laminalfalah.menu.makanan.utils.Config.NAMA;

public class TentangActivity extends AppCompatActivity {

    private static final String TAG = TentangActivity.class.getSimpleName();

    @BindView(R.id.fotoProfile) AppCompatImageView mFotoProfile;
    @BindView(R.id.logout) View mViewLogout;
    @BindView(R.id.jumlahFollowing) AppCompatTextView jumlahFollowing;
    @BindView(R.id.jumlahFollower) AppCompatTextView jumlahFollower;
    @BindView(R.id.isi_nama) AppCompatTextView tvNama;
    @BindView(R.id.isi_email) AppCompatTextView tvEmail;
    @BindView(R.id.copyright) AppCompatTextView tvCopyright;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tentang);
        ButterKnife.bind(this);
        String simpleDateFormat = new SimpleDateFormat("yyyy",Locale.getDefault()).format(new Date());
        tvCopyright.setText(getString(R.string.copyright, simpleDateFormat, getString(R.string.app_version)));

        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getCurrentUser() != null) {
            mViewLogout.setVisibility(View.VISIBLE);
        } else {
            mViewLogout.setVisibility(View.GONE);
        }

        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        mStorageReference.child("me.jpeg")
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(this)
                            .applyDefaultRequestOptions(new RequestOptions().circleCrop().placeholder(R.drawable.ic_account_circle_white_24dp))
                            .load(uri.toString())
                            .circleCrop()
                            .into(mFotoProfile);
                    setup();

                })
                .addOnFailureListener(e -> {
                    Glide.with(this)
                            .load(R.drawable.ic_account_circle_white_24dp)
                            .circleCrop()
                            .into(mFotoProfile);
                    setup();
                });
    }

    private void setup() {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1000);
        valueAnimator.setDuration(3000);
        valueAnimator.addUpdateListener(animation -> {
            jumlahFollowing.setText(animation.getAnimatedValue().toString());
            jumlahFollower.setText(animation.getAnimatedValue().toString());
        });
        valueAnimator.start();

        tvNama.setText(NAMA);
        tvEmail.setText(EMAIL);
    }

    @OnClick(R.id.backHome) void back() {
        onBackPressed();
    }

    @OnClick(R.id.logout) void logout() {
        if (mFirebaseAuth.getCurrentUser() != null) {
            mFirebaseAuth.signOut();
            Toast.makeText(getApplicationContext(), getString(R.string.msg_logout), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
