package th.ac.dusit.dbizcom.bikeparking.etc;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class Utils {

    public static void showOkDialog(Context context, String title, String msg) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void showShortToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static String formatThaiDate(String date) {
        String[] dateParts = date.split("-");
        return dateParts[2] + "." + dateParts[1] + "." + String.valueOf(Integer.parseInt(dateParts[0]) + 543);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static boolean isValidPid(String pid) {
        String pid12Digits = pid.substring(0, 12);
        String pidLastDigit = pid.substring(12);
        long id = Long.valueOf(pid12Digits);
        long base = 100000000000l; //สร้างตัวแปร เพื่อสำหรับให้หารเพื่อเอาหลักที่ต้องการ
        int basenow; //สร้างตัวแปรเพื่อเก็บค่าประจำหลัก
        int sum = 0; //สร้างตัวแปรเริ่มตัวผลบวกให้เท่ากับ 0
        for (int i = 13; i > 1; i--) { //วนรอบตั้งแต่ 13 ลงมาจนถึง 2
            basenow = (int) Math.floor(id / base); //หาค่าประจำตำแหน่งนั้น ๆ
            id = id - basenow * base; //ลดค่า id ลงทีละหลัก
            System.out.println(basenow + "x" + i + " = " + (basenow * i)); //แสดงค่าเมื่อคูณแล้วของแต่ละหลัก
            sum += basenow * i; //บวกค่า sum ไปเรื่อย ๆ ทีละหลัก
            base = base / 10; //ตัดค่าที่ใช้สำหรับการหาเลขแต่ละหลัก
        }
        System.out.println("Sum is " + sum); //แสดงค่า sum
        int checkbit = (11 - (sum % 11)) % 10; //คำนวณค่า checkbit

        return (checkbit == Integer.parseInt(pidLastDigit));
    }
}
