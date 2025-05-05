package com.sketchcolor;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.balsikandar.crashreporter.CrashReporter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> listdata = new ArrayList<>();
    public ArrayList<HashMap<String, String>> newArrray = new ArrayList<>();
    ArrayList<HashMap<String, ArrayList<String>>> arrayList = new ArrayList<>();
    ArrayList<String> customcolor = new ArrayList<>();

    SharedPreferences sp;

    public String dataset1;
    final int REQUEST_STORAGE = 101;

    public ListView list, list2;
    public RecyclerView resh;
    public ConstraintLayout no_color_linear;
    boolean check_position = false;
    Button button1, import_button;
    EditText edittext;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set window flags before setting content view
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#F57F17"));
            
            // Make status bar icons visible if the bar color is light
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        
        setContentView(R.layout.activity_main);
        CrashReporter.initialize(this);
        hideKeyboard(this);

        button1 = findViewById(R.id.button1);
        resh = findViewById(R.id.recyclerview1);
        list = findViewById(R.id.list);
        list2 = findViewById(R.id.list2);
        no_color_linear = findViewById(R.id.no_color_linear);
        edittext = findViewById(R.id.edittext1);
        import_button = findViewById(R.id.import_button);

        button1.setVisibility(View.INVISIBLE);
        sp = this.getSharedPreferences("sp", Activity.MODE_PRIVATE);

        import_button.setOnClickListener(
                v -> importColor());

        list2.setOnItemLongClickListener(
                (adp, view, position, arg3) -> {
                    BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
                    dialog.setContentView(R.layout.bottom_sheet);
                    Button button = dialog.findViewById(R.id.button);
                    Button button1 = dialog.findViewById(R.id.button1);
                    Button import_button = dialog.findViewById(R.id.import_button);
                    Button deleteAll = dialog.findViewById(R.id.deleteAll);

                    deleteAll.setOnClickListener(v -> {
                        sp.edit().remove("colors").apply();
                        list.setVisibility(View.GONE);
                        list2.setVisibility(View.GONE);
                        no_color_linear.setVisibility(View.VISIBLE);
                        dialog.dismiss();

                    });

                    import_button.setOnClickListener(
                            v -> {
                                importColor();
                                dialog.dismiss();
                            });
                    button.setOnClickListener(
                            v -> {
                                requestPermission();
                                Toast.makeText(MainActivity.this, "Backuped all colors", Toast.LENGTH_SHORT)
                                        .show();
                                dialog.dismiss();
                            });
                    button1.setOnClickListener(
                            v -> {
                                customcolor.remove(position);

                                try {
                                    if (!(customcolor.isEmpty())) {
                                        sp.edit().putString("colors", new Gson().toJson(customcolor)).apply();
                                        list2.setAdapter(new listviewAdaptor2(customcolor));
                                    } else {
                                        sp.edit().remove("colors").apply();
                                        list.setVisibility(View.GONE);
                                        list2.setVisibility(View.GONE);
                                        no_color_linear.setVisibility(View.VISIBLE);
                                    }
                                } catch (Exception ignored) {
                                }

                                dialog.dismiss();
                            });

                    dialog.setCancelable(true);
                    dialog.show();
                    return true;
                });

        list2.setOnItemClickListener(
                (adp, view, position, arg3) -> {

                    TextView text = view.findViewById(R.id.textview1);
                    ImageView img = view.findViewById(R.id.check);

                    ((ClipboardManager) getSystemService(MainActivity.CLIPBOARD_SERVICE))
                            .setPrimaryClip(ClipData.newPlainText("clipboard", text.getText().toString()));
                    Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                    img.setVisibility(View.VISIBLE);



                    if (!(isDark(text.getText().toString()))) {
                        Drawable mIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.check);
                        mIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, com.balsikandar.crashreporter.R.color.black), PorterDuff.Mode.MULTIPLY);
                        img.setImageDrawable(mIcon);
                    }else{
                        Drawable mIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.check);
                        mIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, android.R.color.white), PorterDuff.Mode.MULTIPLY);
                        img.setImageDrawable(mIcon);
                    }
                });

        button1.setOnClickListener(
                a -> {
                    if (edittext.getText().toString().contains("#")) {
                        if (sp.contains("colors")) {
                            if (!(customcolor.contains(edittext.getText().toString().trim()))) {

                                customcolor.add(0, edittext.getText().toString().trim());
                            }
                        } else {
                            customcolor.add(edittext.getText().toString().trim());
                        }

                    } else {
                        if (sp.contains("colors")) {
                            if (!(customcolor.contains("#" + edittext.getText().toString().trim()))) {

                                customcolor.add(0, "#" + edittext.getText().toString().trim());
                            }
                        } else {
                            customcolor.add("#" + edittext.getText().toString().trim());
                        }
                    }

                    list2.setAdapter(new listviewAdaptor2(customcolor));
                    sp.edit().remove("colors").apply();
                    sp.edit().putString("colors", new Gson().toJson(customcolor)).apply();
                    resh.smoothScrollToPosition(0);
                    list.setVisibility(View.GONE);
                    list2.setVisibility(View.VISIBLE);
                    no_color_linear.setVisibility(View.INVISIBLE);
                    edittext.setText("");
                });

        edittext.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                        try {
                            if ((arg0.toString()).contains("#")) {
                                Window w = MainActivity.this.getWindow();
                                w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                                w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                w.setStatusBarColor(Color.parseColor(edittext.getText().toString().trim()));
                                Toast.makeText(MainActivity.this, "Color found "+edittext.getText().toString().trim()  , Toast.LENGTH_SHORT).show();
                            } else {
                                Window w = MainActivity.this.getWindow();
                                w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                                w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                w.setStatusBarColor(Color.parseColor("#" + edittext.getText().toString().trim()));
                                Toast.makeText(MainActivity.this, "Color found "+"#"+edittext.getText().toString().trim()  , Toast.LENGTH_SHORT).show();
                            }

                            button1.setVisibility(View.VISIBLE);

                        } catch (Exception e) {
                            button1.setVisibility(View.INVISIBLE);
                            Window w = MainActivity.this.getWindow();
                            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            w.setStatusBarColor(Color.parseColor("#F57F17"));
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void afterTextChanged(Editable arg0) {
                    }
                });
        String dataset =
                "[{\n"
                        + "   \n"
                        + "    \"colorName\" : \"Custom\",\n"
                        + "    \"bgColor\": \"FFFFFF\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Red\",\n"
                        + "\t\"bgColor\": \"F44336\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "\t\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Pink\",\n"
                        + "\t\"bgColor\": \"E91E63\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Purple\",\n"
                        + "\t\"bgColor\": \"9C27B0\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Deep purple\",\n"
                        + "\t\"bgColor\": \"673AB7\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Indigo\",\n"
                        + "\t\"bgColor\": \"3F51B5\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Blue\",\n"
                        + "\t\"bgColor\": \"2196F3\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Light blue\",\n"
                        + "\t\"bgColor\": \"03A9F4\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Cyan\",\n"
                        + "\t\"bgColor\": \"00BCD4\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Teal\",\n"
                        + "\t\"bgColor\": \"009688\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Green\",\n"
                        + "\t\"bgColor\": \"4CAF50\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Light green\",\n"
                        + "\t\"bgColor\": \"8BC34A\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Lime\",\n"
                        + "\t\"bgColor\": \"CDDC39\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Yellow\",\n"
                        + "\t\"bgColor\": \"FFEB3B\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Amber\",\n"
                        + "\t\"bgColor\": \"FFC107\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Orange\",\n"
                        + "\t\"bgColor\": \"FF9800\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Deep orange\",\n"
                        + "\t\"bgColor\": \"FF5722\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Brown\",\n"
                        + "\t\"bgColor\": \"795548\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Grey\",\n"
                        + "\t\"bgColor\": \"9E9E9E\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Blue grey\",\n"
                        + "\t\"bgColor\": \"607D8B\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Black\",\n"
                        + "\t\"bgColor\": \"000000\",\n"
                        + "    \"txtColor\": \"FFFFFF\"\n"
                        + "\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"White\",\n"
                        + "\t\"bgColor\": \"FFFFFF\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "},\n"
                        + "{\n"
                        + "\t\"colorName\" : \"Transparent\",\n"
                        + "\t\"bgColor\": \"00FFFFFF\",\n"
                        + "    \"txtColor\": \"000000\"\n"
                        + "\n"
                        + "}]";

        dataset1 =
                "[{\n"
                        + "\"colors\":[\"00FFFFFF\"]\n"
                        + "},\n"
                        + "{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"F44336\",\"FFEBEE\",\"FFCDD2\",\n"
                        + "    \"EF9A9A\",\"E57373\",\"EF5350\",\n"
                        + "    \"F44336\",\"E53935\",\"D32F2F\",\n"
                        + "    \"C62828\",\"B71C1C\",\"FF8A80\",\n"
                        + "\t\"FF5252\",\"FF1744\",\"D50000\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"E91E63\",\"FCE4EC\",\"F8BBD0\",\n"
                        + "    \"F48FB1\",\"F06292\",\"EC407A\",\n"
                        + "    \"E91E63\",\"D81B60\",\"C2185B\",\n"
                        + "    \"AD1457\",\"880E4F\",\"FF80AB\",\n"
                        + "\t\"FF4081\",\"F50057\",\"C51162\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"9C27B0\",\"F3E5F5\",\"E1BEE7\",\n"
                        + "    \"CE93D8\",\"BA68CB\",\"AB47BC\",\n"
                        + "    \"9C27B0\",\"8E24AA\",\"7B1FA2\",\n"
                        + "    \"6A1BA2\",\"4A148C\",\"EA80FC\",\n"
                        + "\t\"E040F8\",\"D500F9\",\"AA00FF\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"673AB7\",\"EDE7F6\",\"D1C4E9\",\n"
                        + "    \"B39DDB\",\"9575CD\",\"7E57C2\",\n"
                        + "    \"673AB7\",\"5E35B1\",\"512DA8\",\n"
                        + "    \"4527A0\",\"311B92\",\"B399FF\",\n"
                        + "\t\"7C4DFF\",\"651FFF\",\"6200EA\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \"colors\":[\n"
                        + "\t\"3F51B5\",\"E8EAF6\",\"C5CAE9\",\n"
                        + "    \"9FA8DA\",\"7986CB\",\"5C6BC0\",\n"
                        + "    \"3F51B5\",\"3949AB\",\"303F9F\",\n"
                        + "    \"283593\",\"1A237E\",\"8C9EFF\",\n"
                        + "\t\"536DFE\",\"3D5AFE\",\"304FFE\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"2196F3\",\"E3F2FD\",\"BBDEFB\",\n"
                        + "    \"90CAF9\",\"64B5F6\",\"42A5F5\",\n"
                        + "    \"2196F3\",\"1E88E5\",\"1976D2\",\n"
                        + "    \"1565C0\",\"0D47A1\",\"82B1FF\",\n"
                        + "\t\"448AFF\",\"2979FF\",\"2962FF\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \"colors\":[\n"
                        + "\t\"03A9F4\",\"E1F5FE\",\"B3E5FC\",\n"
                        + "    \"81D4FA\",\"4FC3F7\",\"29B6F6\",\n"
                        + "    \"03A9F4\",\"039BE5\",\"0288D1\",\n"
                        + "    \"0277BD\",\"01579B\",\"80D8FF\",\n"
                        + "\t\"40C4FF\",\"00B0FF\",\"0091EA\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"00BCD4\",\"E0F7FA\",\"B2EBF2\",\n"
                        + "    \"80EDEA\",\"4DD0E1\",\"24C6DA\",\n"
                        + "    \"00BCD4\",\"00ACC1\",\"0097A7\",\n"
                        + "    \"00838F\",\"006064\",\"84FFFF\",\n"
                        + "\t\"18FFFF\",\"00E5FF\",\"00B8D4\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"009688\",\"E0F2F1\",\"B2DFDB\",\n"
                        + "    \"80CBC4\",\"4DB6AC\",\"26A69A\",\n"
                        + "    \"009688\",\"00897B\",\"00796B\",\n"
                        + "    \"00695C\",\"004D40\",\"A7FFEB\",\n"
                        + "\t\"64FFDA\",\"1DE9B6\",\"00BFA5\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "\t\"colors\":[\n"
                        + "\t\"4CAF50\",\"E8F5E9\",\"C8E6C9\",\n"
                        + "    \"A5D6A7\",\"81C784\",\"66BB6A\",\n"
                        + "    \"4CAF50\",\"43A047\",\"388E3C\",\n"
                        + "    \"2E7D32\",\"1B5E20\",\"B9F6CA\",\n"
                        + "\t\"69F0AE\",\"00E676\",\"00C853\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"8BC34A\",\"F1F8E9\",\"DCEDC8\",\n"
                        + "    \"C5E1A5\",\"AED581\",\"9CCC65\",\n"
                        + "    \"8BC34A\",\"7CB342\",\"689F38\",\n"
                        + "    \"558B2F\",\"33691E\",\"CCFF90\",\n"
                        + "\t\"B2FF59\",\"76FF03\",\"64DD17\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"CDDC39\",\"F9FBE7\",\"F0F4C3\",\n"
                        + "    \"E6EE9C\",\"DCE775\",\"D4E157\",\n"
                        + "    \"CDDC39\",\"C0CA33\",\"AFB42B\",\n"
                        + "    \"9E9D24\",\"827717\",\"F4FF81\",\n"
                        + "\t\"EEFF41\",\"C6FF00\",\"AEEA00\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"FFEB3B\",\"FFFDE7\",\"FFF9C4\",\n"
                        + "    \"FFF59D\",\"FFF176\",\"FFEE58\",\n"
                        + "    \"FFEB3B\",\"FDD835\",\"FBC02D\",\n"
                        + "    \"F9A825\",\"F57F17\",\"FFFF8D\",\n"
                        + "\t\"FFFF00\",\"FFEA00\",\"FFD600\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"FFC107\",\"FFF8EA\",\"FFECB3\",\n"
                        + "    \"FFE082\",\"FFD54F\",\"FFCA28\",\n"
                        + "    \"FFC107\",\"FFB300\",\"FFA000\",\n"
                        + "    \"FF8F00\",\"FF6F00\",\"FFE57F\",\n"
                        + "\t\"FFD740\",\"FFC400\",\"FFAB00\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"FF9800\",\"FFF3E0\",\"FFE0B2\",\n"
                        + "    \"FFCC80\",\"FFB74D\",\"FFA726\",\n"
                        + "    \"FF9800\",\"FB8C00\",\"F57C00\",\n"
                        + "    \"EF6C00\",\"E65100\",\"FFD180\",\n"
                        + "\t\"FFAB40\",\"FF9100\",\"FF6D00\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"FF5722\",\"F9E9E7\",\"FFCCBC\",\n"
                        + "    \"FFAB91\",\"FF8A65\",\"FF7043\",\n"
                        + "    \"FF5722\",\"F4511E\",\"E64A19\",\n"
                        + "    \"D84315\",\"BF360C\",\"FF9E80\",\n"
                        + "\t\"FF6E40\",\"FF3D00\",\"DD2C00\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"795548\",\"EFEBE9\",\"D7CCC8\",\n"
                        + "    \"BCAAA4\",\"A1887F\",\"8D6E63\",\n"
                        + "    \"795548\",\"6D4C41\",\"5D4037\",\n"
                        + "    \"4E342E\",\"3E2723\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"9E9E9E\",\"FAFAFA\",\"F5F5F5\",\n"
                        + "    \"EEEEEE\",\"E0E0E0\",\"BDBDBD\",\n"
                        + "    \"9E9E9E\",\"757575\",\"616161\",\n"
                        + "    \"424242\",\"212121\"\n"
                        + "\t]\n"
                        + "\t\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t\"607D8B\",\"ECEFF1\",\"CFD8DC\",\n"
                        + "    \"B0BEC5\",\"90A4AE\",\"78909C\",\n"
                        + "    \"607D8B\",\"546E7A\",\"455A64\",\n"
                        + "    \"37474F\",\"263238\"\n"
                        + "\t]\n"
                        + "\t\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t  \"000000\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t  \"FFFFFF\"\n"
                        + "\t]\n"
                        + "},{\n"
                        + "    \t\"colors\":[\n"
                        + "\t \"00FFFFFF\"\n"
                        + "\t]\n"
                        + "}]";

        button1.setBackground(
                new GradientDrawable() {
                    public GradientDrawable getIns(int a, int b, int c, int d) {
                        this.setStroke(a, b);
                        this.setColor(c);
                        this.setCornerRadius(d);
                        return this;
                    }
                }.getIns(8, 0xFFFF5722, 0xFFFFC107, 10));

        resh.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newArrray =
                new Gson()
                        .fromJson(dataset, new TypeToken<ArrayList<HashMap<String, String>>>() {
                        }.getType());
        resAdaptor res = new resAdaptor(newArrray);
        DividerItemDecoration divider =
                new DividerItemDecoration(resh.getContext(), LinearLayoutManager.HORIZONTAL);
        resh.addItemDecoration(divider);
        resh.setAdapter(res);
    }

    public class resAdaptor extends RecyclerView.Adapter<resAdaptor.ViewHolder> {

        ArrayList<HashMap<String, String>> array;
        Context context;

        public resAdaptor(ArrayList<HashMap<String, String>> array) {
            this.array = array;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            this.context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.colorh, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder view, int position) {

            view.t.setText(array.get(position).get("colorName").toUpperCase());
            view.t.setTextColor(Color.parseColor("#" + array.get(position).get("txtColor")));
            
            // Set background color directly using the drawable resource
            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.recycler_item_background);
            if (background != null) {
                background.setColor(Color.parseColor("#" + array.get(position).get("bgColor")));
                view.l.setBackground(background);
            }
            
            view.l.setOnClickListener(
                    arg0 -> {
                        if ((position < 1)) {

                            if (sp.contains("colors")) {
                                list.setVisibility(View.GONE);
                                list2.setVisibility(View.VISIBLE);
                                no_color_linear.setVisibility(View.GONE);
                                list2.setAdapter(new listviewAdaptor2(customcolor));
                            } else {
                                no_color_linear.setVisibility(View.VISIBLE);
                                list2.setVisibility(View.GONE);
                                list.setVisibility(View.GONE);
                            }

                        } else {
                            arrayList =
                                    new Gson()
                                            .fromJson(
                                                    dataset1,
                                                    new TypeToken<
                                                            ArrayList<HashMap<String, ArrayList<String>>>>() {
                                                    }.getType());
                            no_color_linear.setVisibility(View.INVISIBLE);
                            list.setVisibility(View.VISIBLE);
                            list2.setVisibility(View.GONE);
                            listdata = arrayList.get(position).get("colors");
                            ListAdapter b = new listviewAdaptor(listdata, position);
                            list.setAdapter(b);
                        }
                    });

            if ((position < 1)) {
                if (!(check_position)) {
                    if (sp.contains("colors")) {
                        customcolor =
                                new Gson()
                                        .fromJson(
                                                sp.getString("colors", ""), new TypeToken<ArrayList<String>>() {
                                                }.getType());

                        list.setVisibility(View.GONE);
                        list2.setVisibility(View.VISIBLE);
                        no_color_linear.setVisibility(View.GONE);
                        list2.setAdapter(new listviewAdaptor2(customcolor));
                        check_position = true;
                    } else {
                        no_color_linear.setVisibility(View.VISIBLE);
                        list2.setVisibility(View.GONE);
                        list.setVisibility(View.GONE);
                        check_position = true;
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return array.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView t;
            LinearLayout l;

            public ViewHolder(View view) {
                super(view);

                t = view.findViewById(R.id.textview1);
                l = view.findViewById(R.id.linear1);
            }
        }
    }

    public class listviewAdaptor extends BaseAdapter {
        ArrayList<String> listdata;
        int oo;

        public listviewAdaptor(ArrayList<String> arr, int ClickPostion) {
            this.listdata = arr;
            this.oo = ClickPostion;
        }

        @Override
        public int getCount() {
            return listdata.size();
        }

        @Override
        public String getItem(int position) {
            return listdata.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.colorv, parent, false);
            }
            TextView text = view.findViewById(R.id.textview1);
            ImageView img = view.findViewById(R.id.check);
            LinearLayout linear = view.findViewById(R.id.linear1);
            img.setVisibility(View.INVISIBLE);

            if (isDark("#"+listdata.get(position))) {
                text.setTextColor(Color.WHITE);
            } else {
                text.setTextColor(Color.BLACK);
            }

            linear.setOnClickListener(
                    v -> {
                        ((ClipboardManager) getSystemService(MainActivity.CLIPBOARD_SERVICE))
                                .setPrimaryClip(ClipData.newPlainText("clipboard", text.getText().toString()));
                        Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                        img.setVisibility(View.VISIBLE);

                        if (isDark("#"+listdata.get(position))) {
                            Drawable mIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.check);
                            mIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, android.R.color.white), PorterDuff.Mode.MULTIPLY);
                            img.setImageDrawable(mIcon);
                        } else {
                            Drawable mIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.check);
                            mIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, com.balsikandar.crashreporter.R.color.black), PorterDuff.Mode.MULTIPLY);
                            img.setImageDrawable(mIcon);
                        }

                    });

            text.setText("#" + listdata.get(position));
            
            // Create GradientDrawable with rounded corners for the color item
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(Color.parseColor("#" + listdata.get(position)));
            shape.setCornerRadius(8); // 8dp corner radius
            linear.setBackground(shape);

            return view;
        }
    }

    public class listviewAdaptor2 extends BaseAdapter {
        ArrayList<String> listdata;

        public listviewAdaptor2(ArrayList<String> arr) {
            this.listdata = arr;
        }

        @Override
        public int getCount() {
            return listdata.size();
        }

        @Override
        public String getItem(int position) {
            return listdata.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.colorv, parent, false);
            }
            TextView text = view.findViewById(R.id.textview1);
            ImageView img = view.findViewById(R.id.check);
            LinearLayout linear = view.findViewById(R.id.linear1);
            img.setVisibility(View.INVISIBLE);

            text.setText(listdata.get(position));
            
            try {
                // Create GradientDrawable with rounded corners for the color item
                GradientDrawable shape = new GradientDrawable();
                shape.setColor(Color.parseColor(listdata.get(position)));
                shape.setCornerRadius(8); // 8dp corner radius
                linear.setBackground(shape);
                
                if (isDark(listdata.get(position))) {
                    text.setTextColor(Color.WHITE);
                } else {
                    text.setTextColor(Color.BLACK);
                }
            } catch (Exception e) {
                linear.setBackgroundColor(Color.WHITE);
                text.setTextColor(Color.BLACK);
            }

            linear.setOnClickListener(
                    v -> {
                        ((ClipboardManager) getSystemService(MainActivity.CLIPBOARD_SERVICE))
                                .setPrimaryClip(ClipData.newPlainText("clipboard", text.getText().toString()));
                        Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                        img.setVisibility(View.VISIBLE);

                        if (isDark(listdata.get(position))) {
                            Drawable mIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.check);
                            mIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, android.R.color.white), PorterDuff.Mode.MULTIPLY);
                            img.setImageDrawable(mIcon);
                        } else {
                            Drawable mIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.check);
                            mIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, com.balsikandar.crashreporter.R.color.black), PorterDuff.Mode.MULTIPLY);
                            img.setImageDrawable(mIcon);
                        }
                    });

            return view;
        }
    }

    public Boolean isDark(String _hex) {

        final double darkness =
                1
                        - (0.299 * Color.red(Color.parseColor(_hex))
                        + 0.587 * Color.green(Color.parseColor(_hex))
                        + 0.114 * Color.blue(Color.parseColor(_hex)))
                        / 255;

        return !(darkness < 0.35);
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!(Environment.isExternalStorageManager())) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                Toast.makeText(this, "provide storage permission", Toast.LENGTH_SHORT).show();
                startActivity(intent);

            } else {
                refreshColorFile();
            }
        }
    }

    public void refreshColorFile() {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/" + "colors.txt");
            if (!(file.createNewFile())) {
                Toast.makeText(this, "creted", Toast.LENGTH_SHORT).show();

                FileWriter writer =
                        new FileWriter(Environment.getExternalStorageDirectory() + "/" + "colors.txt");
                writer.write(sp.getString("colors", ""));
                writer.close();
            } else {
                FileWriter writer =
                        new FileWriter(Environment.getExternalStorageDirectory() + "/" + "colors.txt");
                writer.write(sp.getString("colors", ""));
                writer.close();
            }
        } catch (Exception e) {
            CrashReporter.logException(e);
        }
    }

    public void importColor() {
        Intent i = new Intent();
        i.setType("text/*");
        i.setAction(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(i, "select exported file"), REQUEST_STORAGE);
    }

    @Override
    @CallSuper
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_STORAGE) {

            Uri fileuri = data.getData();

            if (fileuri != null) {
                try {
                    byte[] bb = getBytes(this, fileuri);
                    String content = new String(bb);
                    if (sp.contains("colors")) {
                        ArrayList<String> shap = new ArrayList<>();
                        shap = new Gson().fromJson(content, new TypeToken<ArrayList<String>>() {
                        }.getType());
                        customcolor.addAll(0, shap);
                        Set<String> jk = new HashSet<>();
                        jk.addAll(customcolor);
                        customcolor.clear();
                        customcolor.addAll(jk);
                        list2.setAdapter(new listviewAdaptor2(customcolor));
                        sp.edit().remove("coloes").apply();
                        sp.edit().putString("colors", new Gson().toJson(customcolor)).apply();
                        list2.setVisibility(View.VISIBLE);
                        no_color_linear.setVisibility(View.GONE);
                    } else {
                        customcolor =
                                new Gson().fromJson(content, new TypeToken<ArrayList<String>>() {
                                }.getType());
                        list2.setAdapter(new listviewAdaptor2(customcolor));
                        sp.edit().putString("colors", new Gson().toJson(customcolor)).apply();
                        list2.setVisibility(View.VISIBLE);
                        no_color_linear.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    CrashReporter.logException(e);
                }
            }
        }
    }

    byte[] getBytes(Context context, Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (Exception ex) {
            CrashReporter.logException(ex);
            return null;
        }
    }

    public  void hideKeyboard(Context context){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }



}
