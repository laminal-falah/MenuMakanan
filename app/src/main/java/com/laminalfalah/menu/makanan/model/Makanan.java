package com.laminalfalah.menu.makanan.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class Makanan {
    public static final String COLLECTION = "makanan";
    public static final String TIMESTAMPS = "timestamps";
    public static final String FIELD_NAMA_MAKANAN = "namaMakanan";
    public static final String FIELD_IMAGE_MAKANAN = "fotoMakanan";
    public static final String FIELD_HARGA_MAKANAN = "hargaMakanan";
    public static final String FIELD_RATING_MAKANAN = "ratingMakanan";
    public static final String FIELD_DESKRIPSI_MAKANAN = "deskripsiMakanan";

    private String namaMakanan;
    private String fotoMakanan;
    private double hargaMakanan;
    private double ratingMakanan;
    private String deskripsiMakanan;
    private @ServerTimestamp Date timestamps;

    public Makanan() {
    }

    public Makanan(String namaMakanan, String fotoMakanan, double hargaMakanan, double ratingMakanan, String deskripsiMakanan) {
        this.namaMakanan = namaMakanan;
        this.fotoMakanan = fotoMakanan;
        this.hargaMakanan = hargaMakanan;
        this.ratingMakanan = ratingMakanan;
        this.deskripsiMakanan = deskripsiMakanan;
    }

    public String getNamaMakanan() {
        return namaMakanan;
    }

    public void setNamaMakanan(String namaMakanan) {
        this.namaMakanan = namaMakanan;
    }

    public String getFotoMakanan() {
        return fotoMakanan;
    }

    public void setFotoMakanan(String fotoMakanan) {
        this.fotoMakanan = fotoMakanan;
    }

    public double getHargaMakanan() {
        return hargaMakanan;
    }

    public void setHargaMakanan(double hargaMakanan) {
        this.hargaMakanan = hargaMakanan;
    }

    public double getRatingMakanan() {
        return ratingMakanan;
    }

    public void setRatingMakanan(double ratingMakanan) {
        this.ratingMakanan = ratingMakanan;
    }

    public String getDeskripsiMakanan() {
        return deskripsiMakanan;
    }

    public void setDeskripsiMakanan(String deskripsiMakanan) {
        this.deskripsiMakanan = deskripsiMakanan;
    }

    public Date getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(Date timestamps) {
        this.timestamps = timestamps;
    }

    @NonNull
    @Override
    public String toString() {
        return "Makanan{" +
                "namaMakanan='" + namaMakanan + '\'' +
                ", fotoMakanan='" + fotoMakanan + '\'' +
                ", hargaMakanan=" + hargaMakanan +
                ", ratingMakanan=" + ratingMakanan +
                ", deskripsiMakanan='" + deskripsiMakanan + '\'' +
                ", timestamps=" + timestamps +
                '}';
    }
}
