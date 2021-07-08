package com.codeplay.scanner_demo.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Tham on 26/07/16.
 */
public class Barcode implements Parcelable {
    private int id;
    private String barcode;
    private int format;
    private long datetime;

    public Barcode() {}

    protected Barcode(Parcel in) {
        id = in.readInt();
        barcode = in.readString();
        format = in.readInt();
        datetime = in.readLong();
    }

    public static final Creator<Barcode> CREATOR = new Creator<Barcode>() {
        @Override
        public Barcode createFromParcel(Parcel in) {
            return new Barcode(in);
        }

        @Override
        public Barcode[] newArray(int size) {
            return new Barcode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(barcode);
        dest.writeInt(format);
        dest.writeLong(datetime);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String get() {
        return barcode;
    }

    public void set(String barcode) {
        this.barcode = barcode;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
}
