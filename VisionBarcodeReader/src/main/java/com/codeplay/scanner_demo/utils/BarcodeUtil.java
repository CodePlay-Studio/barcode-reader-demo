package com.codeplay.scanner_demo.utils;

import android.text.TextUtils;

/**
 * Created by Tham on 27/07/16.
 */
public class BarcodeUtil {
    public static final int CODE_INVALID = -1;
    public static final int CODE_MPS_11 = 101;
    public static final int CODE_MPS_13 = 102;
    public static final int CODE_CN_10  = 201;
    public static final int CODE_CN_13  = 202;
    public static final int CODE_PSO_13 = 301;
    public static final int CODE_TQB_12 = 401;
    public static final int CODE_FDX_12 = 501;

    public static int getFormat(String barcode) {
        int code = CODE_INVALID;

        if (barcode==null)
            return code;

        final int length = barcode.length();
        switch (length) {
            case 10:
                if (TextUtils.isDigitsOnly(barcode))
                    code = CODE_CN_10;
                break;
            case 11:
                if (TextUtils.isDigitsOnly(barcode))
                    code = CODE_MPS_11;
                break;
            case 12:
                if (TextUtils.isDigitsOnly(barcode))
                    code = CODE_TQB_12;
                break;
            case 13:
                if (TextUtils.isDigitsOnly(barcode))
                    code = CODE_MPS_13;
                else if (isAlpha(barcode.substring(0, 2)) &&
                        isAlphaNumeric(barcode.substring(2, 4)) &&
                        TextUtils.isDigitsOnly(barcode.substring(4)))
                    code = CODE_CN_13;
                else if (barcode.substring(0, 2).equals("EE") &&
                        barcode.substring(11).equals("ID") &&
                        TextUtils.isDigitsOnly(barcode.substring(2, 11)))
                    code = CODE_PSO_13;
                break;
            case 16:
            case 32:
            case 34:
                if (TextUtils.isDigitsOnly(barcode))
                    code = CODE_FDX_12;
                break;
        }
        return code;
    }

    public static String trimFDXBarocde(String barcode) {
        final int length = barcode.length();
        switch (length) {
            case 16:
                return barcode.substring(0, 12);
            case 32:
                return barcode.substring(16, 28);
            case 34:
                return barcode.substring(22, 34);
            default:
                return barcode;
        }
    }

    private static boolean isAlphaNumeric(String source) {
        char[] chars = source.toCharArray();

        for (char c : chars) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isAlpha(String source) {
        char[] chars = source.toCharArray();

        for (char c : chars) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }
}
