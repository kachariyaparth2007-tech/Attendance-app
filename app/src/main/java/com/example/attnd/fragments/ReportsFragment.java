package com.example.attnd.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.attnd.R;
import com.example.attnd.database.ClassEntity;
import com.example.attnd.viewmodel.MainViewModel;
import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {
    private ListPopupWindow listPopupWindow;
    private TextView tvClassSelector;
    private View btnSendData;
    private MainViewModel viewModel;
    private String selectedClass = null;
    private List<ClassEntity> classList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        this.viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        this.tvClassSelector = view.findViewById(R.id.tvReportClassSelector);
        this.btnSendData = view.findViewById(R.id.btnSendData);
        this.listPopupWindow = new ListPopupWindow(requireContext());
        this.listPopupWindow.setAnchorView(this.tvClassSelector);
        this.listPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_popup_rounded));
        this.listPopupWindow.setModal(true);
        this.listPopupWindow.setVerticalOffset(16);

        this.viewModel.getAllClasses().observe(getViewLifecycleOwner(), classes -> {
            this.classList = classes;
            final List<String> classNames = new ArrayList<>();
            for (ClassEntity c : classes) {
                classNames.add(c.className);
            }
            if (classNames.size() > 5) {
                this.listPopupWindow.setHeight(500);
            } else {
                this.listPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, classNames);
            this.listPopupWindow.setAdapter(adapter);
            if (this.viewModel.currentSelectedClass != null && classNames.contains(this.viewModel.currentSelectedClass)) {
                this.selectedClass = this.viewModel.currentSelectedClass;
                this.tvClassSelector.setText(this.selectedClass);
            } else {
                this.tvClassSelector.setText("Select Class");
            }
            this.listPopupWindow.setOnItemClickListener((parent, view1, position, id) -> {
                this.selectedClass = classNames.get(position);
                this.viewModel.currentSelectedClass = this.selectedClass;
                this.tvClassSelector.setText(this.selectedClass);
                this.listPopupWindow.dismiss();
            });
        });

        this.tvClassSelector.setOnClickListener(v -> this.listPopupWindow.show());
        this.btnSendData.setOnClickListener(v -> handleSendData());

        view.findViewById(R.id.cardViewDetails).setOnClickListener(v -> navigateTo(v, R.id.nav_daily_report));
        view.findViewById(R.id.cardSummary).setOnClickListener(v -> navigateTo(v, R.id.nav_summary_report));
        view.findViewById(R.id.cardMonthly).setOnClickListener(v -> navigateTo(v, R.id.nav_monthly_report));

        return view;
    }

    private void navigateTo(View v, int actionId) {
        if (checkClass()) {
            Bundle args = new Bundle();
            args.putString("className", this.selectedClass);
            Navigation.findNavController(v).navigate(actionId, args);
        }
    }

    private void handleSendData() {
        btnSendData.setEnabled(false);
        viewModel.getClassNamesDirect(classNames -> {
            if (classNames.isEmpty()) {
                Toast.makeText(getContext(), "No classes found to send.", Toast.LENGTH_SHORT).show();
                btnSendData.setEnabled(true);
                return;
            }
            final String[] classArray = classNames.toArray(new String[0]);
            final boolean[] checkedItems = new boolean[classArray.length];
            for (int i = 0; i < checkedItems.length; i++) checkedItems[i] = true;

            new MaterialAlertDialogBuilder(requireContext())
                    .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg_rounded))
                    .setTitle("Select Classes to Send")
                    .setMultiChoiceItems(classArray, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                    .setPositiveButton("Send", (dialog, which) -> {
                        List<String> selectedClasses = new ArrayList<>();
                        for (int i = 0; i < classArray.length; i++) {
                            if (checkedItems[i]) selectedClasses.add(classArray[i]);
                        }
                        if (selectedClasses.isEmpty()) {
                            Toast.makeText(getContext(), "Please select at least one class", Toast.LENGTH_SHORT).show();
                            btnSendData.setEnabled(true);
                        } else {
                            Toast.makeText(getContext(), "Uploading data...", Toast.LENGTH_SHORT).show();
                            viewModel.uploadDataToFirebase(selectedClasses, new MainViewModel.OnSyncListener() {
                                @Override
                                public void onSuccess() {
                                    requireActivity().runOnUiThread(() -> {
                                        if (getContext() != null) {
                                            Toast.makeText(getContext(), "Data sent successfully!", Toast.LENGTH_LONG).show();
                                            btnSendData.setEnabled(true);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    requireActivity().runOnUiThread(() -> {
                                        if (getContext() != null) {
                                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                                            btnSendData.setEnabled(true);
                                        }
                                    });
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> btnSendData.setEnabled(true))
                    .setCancelable(false)
                    .show();
        });
    }

    private boolean checkClass() {
        if (this.selectedClass == null) {
            Toast.makeText(requireContext(), "Please select a class first", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
