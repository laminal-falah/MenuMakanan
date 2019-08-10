package com.laminalfalah.menu.makanan.utils;

import com.laminalfalah.menu.makanan.App;
import com.laminalfalah.menu.makanan.R;

public final class Config {
    private static final String s = App.mActivity.getString(R.string.development);
    public static final boolean DEVELOPMENT = Boolean.parseBoolean(s);
    public static final String DIRECTORY = "makanan/";
    public static final String NAMA = "Laminal Falah";
    public static final String EMAIL = "laminalfalah08@gmail.com";
}
