package com.example.attnd.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.attnd.R;
import com.example.attnd.viewmodel.MainViewModel;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MenuFragment extends Fragment {

    private TextView tvMenuName;
    private final View[] fileItems = new View[4];
    private final View[] separators = new View[4];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMenuName = view.findViewById(R.id.tvMenuName);

        fileItems[0] = view.findViewById(R.id.file_1);
        fileItems[1] = view.findViewById(R.id.file_2);
        fileItems[2] = view.findViewById(R.id.file_3);
        fileItems[3] = view.findViewById(R.id.file_4);

        separators[0] = view.findViewById(R.id.sep_1);
        separators[1] = view.findViewById(R.id.sep_2);
        separators[2] = view.findViewById(R.id.sep_3);
        separators[3] = view.findViewById(R.id.sep_4);

        loadProfileData();
        loadRecentFiles();

        view.findViewById(R.id.cardProfileName).setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.nav_profile)
        );

        view.findViewById(R.id.btnMoreFiles).setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.nav_file)
        );

        view.findViewById(R.id.btnThemeMenu).setOnClickListener(this::showThemePopup);
    }

    private void showThemePopup(View v) {
        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_theme_picker, null);
        LinearLayout themeContainer = popupView.findViewById(R.id.themeContainer);

        String[] themes = {"blue", "green", "orange", "purple", "pink"};
        int[] colors = {
                0xFF426F97, // Blue
                0xFF536500, // Green
                0xFF6E4628, // Orange
                0xFF6A1B9A, // Purple
                0xFF68355D  // Pink
        };

        SharedPreferences prefs = requireContext().getSharedPreferences("AttndPrefs", Context.MODE_PRIVATE);
        String currentTheme = prefs.getString("app_theme", "blue");

        final android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Required for PopupWindow background and shadow to work correctly on older versions
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);


        for (int i = 0; i < themes.length; i++) {
            final String themeKey = themes[i];
            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_theme_color, themeContainer, false);
            View colorCircle = itemView.findViewById(R.id.themeColorCircle);
            View selectionRing = itemView.findViewById(R.id.themeSelectionRing);

            colorCircle.getBackground().setColorFilter(colors[i], android.graphics.PorterDuff.Mode.SRC_IN);
            
            if (themeKey.equals(currentTheme)) {
                selectionRing.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(view -> {
                saveTheme(themeKey);
                popupWindow.dismiss();
            });

            themeContainer.addView(itemView);
        }

        popupWindow.setElevation(10);
        popupWindow.showAsDropDown(v, -140, -30); // Added negative Y offset to move it up slightly
    }

    private void saveTheme(String themeKey) {
        SharedPreferences prefs = requireContext().getSharedPreferences("AttndPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("app_theme", themeKey).apply();
        requireActivity().recreate();
    }

    private void loadProfileData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("AttndPrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("userName", "User");
        tvMenuName.setText(name);
    }

    private void loadRecentFiles() {
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File attndDir = new File(documentsDir, "ATTND");
        
        List<File> allFiles = new ArrayList<>();
        if (attndDir.exists() && attndDir.isDirectory()) {
            File[] files = attndDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".pdf") || name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv")) {
                        allFiles.add(file);
                    }
                }
            }
        }

        allFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy | hh:mm a", Locale.getDefault());

        for (int i = 0; i < 3; i++) {
            if (i < allFiles.size()) {
                File file = allFiles.get(i);
                View itemView = fileItems[i];
                itemView.setVisibility(View.VISIBLE);
                if (i < separators.length) separators[i].setVisibility(View.VISIBLE);

                TextView tvName = itemView.findViewById(R.id.tvFileName);
                TextView tvInfo = itemView.findViewById(R.id.tvFileInfo);
                ImageView ivIcon = itemView.findViewById(R.id.ivFileIcon);

                tvName.setText(file.getName());
                tvInfo.setText(sdf.format(new Date(file.lastModified())));

                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    ivIcon.setImageResource(R.drawable.file_pdf);
                } else {
                    ivIcon.setImageResource(R.drawable.file_excel);
                }

                itemView.setOnClickListener(v -> openFile(file));
            } else if (fileItems[i] != null) {
                fileItems[i].setVisibility(View.GONE);
                if (i < separators.length && separators[i] != null) separators[i].setVisibility(View.GONE);
            }
        }
    }

    private void openFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String name = file.getName().toLowerCase();
            String mimeType = "*/*";
            if (name.endsWith(".pdf")) {
                mimeType = "application/pdf";
            } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                mimeType = "application/vnd.ms-excel";
            } else if (name.endsWith(".csv")) {
                mimeType = "text/csv";
            }
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }
}
