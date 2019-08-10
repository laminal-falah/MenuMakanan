package com.laminalfalah.menu.makanan.ui;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.laminalfalah.menu.makanan.R;
import com.laminalfalah.menu.makanan.utils.ProgressBarUtils;
import com.laminalfalah.menu.makanan.utils.SnackBarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.tlEmail) TextInputLayout tlEmail;
    @BindView(R.id.tlPassword) TextInputLayout tlPassword;
    @BindView(R.id.txtEmail) TextInputEditText txtEmail;
    @BindView(R.id.txtPassword) TextInputEditText txtPassword;

    private ProgressBarUtils mProgressBarUtils;
    private SnackBarUtils mSnackBarUtils;

    private FirebaseAuth mFirebaseAuth;

    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mProgressBarUtils = new ProgressBarUtils(this);
        mSnackBarUtils = new SnackBarUtils(this);

        txtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                login();
                return true;
            }
            return false;
        });
    }

    private boolean validate() {
        boolean valid = true;

        email = tlEmail.getEditText().getText().toString();
        password = tlPassword.getEditText().getText().toString();

        if (TextUtils.isEmpty(email)) {
            tlEmail.setErrorEnabled(true);
            tlEmail.setError(getString(R.string.error_login, getString(R.string.hint_email)));
            valid = false;
        } else if (!email.contains("@")) {
            tlEmail.setErrorEnabled(true);
            tlEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else {
            tlEmail.setError(null);
            tlEmail.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(password)) {
            tlPassword.setErrorEnabled(true);
            tlPassword.setError(getString(R.string.error_login, getString(R.string.hint_kata_sandi)));
            valid = false;
        } else if (password.length() < 7) {
            tlPassword.setErrorEnabled(true);
            tlPassword.setError(getString(R.string.error_invalid_password));
            valid = false;
        } else {
            tlPassword.setError(null);
            tlPassword.setErrorEnabled(false);
        }

        return valid;
    }

    @OnClick(R.id.btnBack) void back() {
        finish();
    }

    @OnClick(R.id.btnLogin) void login() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (!validate()) return;

        mProgressBarUtils.show();

        new Handler().postDelayed(() -> {
            mFirebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, getString(R.string.msg_login_success), Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            mSnackBarUtils.snackBarLong(getString(R.string.msg_login_failed));
                        }
                    })
                    .addOnFailureListener(e -> {
                        try {
                            throw e;
                        } catch (FirebaseAuthInvalidUserException invalidEmail) {
                            tlEmail.setErrorEnabled(true);
                            tlEmail.setError(getString(R.string.error_credentials_email));
                            tlEmail.requestFocus();
                        } catch (FirebaseAuthInvalidCredentialsException invalidPassword) {
                            tlPassword.setErrorEnabled(true);
                            tlPassword.setError(getString(R.string.error_incorrect_password));
                            tlPassword.requestFocus();
                        } catch (Exception ex) {
                            Log.e(TAG, "login: ", e);
                        }
                    });

            mProgressBarUtils.hide();

        }, 2000);
    }
}
