package com.x.jeotourist.scene.mainScene;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.x.jeotourist.R;
import com.x.jeotourist.application.AppController;
import com.x.jeotourist.data.local.dao.TourDao;
import com.x.jeotourist.data.local.entity.TourDataEntity;
import com.x.jeotourist.scene.mainScene.adapter.TourListAdapter;
import com.x.jeotourist.services.BackgroundLocationService;
import com.x.jeotourist.utils.Utils;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static android.content.Context.LOCATION_SERVICE;
import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static java.sql.DriverManager.println;

public class TourListFragment extends DaggerFragment implements TourListAdapter.ItemListener, View.OnClickListener {

    @Inject
    TourDao contentDao;
    EditText tourTitle;
    private BottomSheetBehavior sheetBehavior;
    View root;
    TourListListener tourListListener;
    RecyclerView list;
    TourListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tour_list, container, false);
        tourTitle = root.findViewById(R.id.edit);
        // Inflate the layout for this fragment
        sheetBehavior = BottomSheetBehavior.from(root.findViewById(R.id.bottomSheet));
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            tourListListener = (TourListListener) getActivity();
        }
        setListeners();
        setRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPerMission(false);
    }


    private void setListeners() {
        root.findViewById(R.id.fab).setOnClickListener(this);
        root.findViewById(R.id.create).setOnClickListener(this);
        root.findViewById(R.id.cancel).setOnClickListener(this);
    }


    @Override
    public void onItemClickListener(TourDataEntity entity, int position) {
        tourListListener.onTourClicked(entity);
    }

    @Override
    public void onItemDeleteClickListener(TourDataEntity entity, int position) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                checkPerMission(true);
                return;

            case R.id.create:
                if (TextUtils.isEmpty(tourTitle.getText())) {
                    Toast.makeText(getActivity(), R.string.valid_tour_name, Toast.LENGTH_LONG).show();
                    return;
                }
                TourDataEntity tourEntity = new TourDataEntity();
                tourEntity.setCreatedAt(new Date().getTime());
                tourEntity.setTitle(tourTitle.getText().toString());
                adapter.addItem(contentDao.getTour(contentDao.insertContents(tourEntity)));
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                root.findViewById(R.id.no_content).setVisibility((adapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
                tourTitle.setText(null);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(tourTitle.getWindowToken(), 0);
                    }
                },500);

                return;
            case R.id.cancel:
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
        }
    }

    private void setRecyclerView() {
        list = root.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.addItemDecoration(new DividerItemDecoration(getActivity(), RecyclerView.VERTICAL));
        adapter = new TourListAdapter(getActivity(), this);
        list.setAdapter(adapter);
        adapter.setItems((ArrayList<TourDataEntity>) contentDao.getTours());
        root.findViewById(R.id.no_content).setVisibility((adapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }

    private void checkPerMission(boolean openBottomSheet) {
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        Utils.PermissionStatus status = Utils.checkPermissions(
                getActivity(),
                Utils.PERMISSION_REQUEST,
                PERMISSIONS,
                this, R.string.storage_permission_msg
        );
        if (status == Utils.PermissionStatus.SUCCESS) {
            if (openBottomSheet) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                startLocationService();
            }
        }
    }

    private void startLocationService() {
        LocationManager service = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings
        if (!enabled) {
            Intent intent = new Intent(ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            ContextCompat.startForegroundService(
                    AppController.getInstance(),
                    new Intent(AppController.getInstance(), BackgroundLocationService.class));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length >= 2) {
            startLocationService();
            // sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}