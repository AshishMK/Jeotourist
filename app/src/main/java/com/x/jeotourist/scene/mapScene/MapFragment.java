package com.x.jeotourist.scene.mapScene;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapSettings;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.x.jeotourist.R;
import com.x.jeotourist.application.AppController;
import com.x.jeotourist.data.local.dao.TourDao;
import com.x.jeotourist.data.local.entity.MarkerEntity;
import com.x.jeotourist.data.local.entity.TourDataEntity;
import com.x.jeotourist.scene.playerScene.PlayerActivity;
import com.x.jeotourist.utils.Utils;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import timber.log.Timber;

public class MapFragment extends DaggerFragment implements View.OnClickListener {
    @Inject
    TourDao contentDao;

    // map embedded in the map fragment
    private com.here.android.mpa.mapping.Map map;

    // map fragment embedded in this activity
    private AndroidXMapFragment mapFragment;
    private BottomSheetBehavior sheetBehavior;
    View root;
    EditText markerTitle;
    long tourId = 0;
    String videoFile = null;
    TextView colorTextView;
    androidx.appcompat.widget.PopupMenu popupMenu;
    String[] colorNames;
    GeoCoordinate selectedGeoCoordinate = null;
    MarkerEntity updateMarkerEntity = null;
    MapMarker mapMarkerUpdate = null;
    boolean gestureListenerAdded = false;
    android.location.Location lastLocation = null;
    private int selectedColorIndex = -1;
    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_map, container, false);
        // Inflate the layout for this fragment
        markerTitle = root.findViewById(R.id.edit);
        colorTextView = root.findViewById(R.id.selectColor);
        sheetBehavior = BottomSheetBehavior.from(root.findViewById(R.id.bottomSheet));
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lastLocation = location;
                if (lastLocation == null) {
                    Toast.makeText(AppController.getInstance(), "Can't get location", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (map != null) {
                    map.setCenter(new GeoCoordinate(lastLocation.getLatitude(), lastLocation.getLongitude(), 0.0), Map.Animation.NONE);
                }
            }
        });

        colorNames = getResources().getStringArray(R.array.color_names);
        setUpColorMenu();
        showSnackView();
        initialize();
        setListeners();
    }


    private void setMarkers() {
        if (mapFragment.getMapGesture() != null && !gestureListenerAdded) {
            gestureListenerAdded = true;
            mapFragment.getMapGesture().addOnGestureListener(listener, 0, false);
        }
        List<MarkerEntity> markers = contentDao.getMarkers(tourId);
        map.removeAllMapObjects();
        for (MarkerEntity marker : markers) {
            addMarker(marker);
        }
        if (markers.size() > 0) {
            //set map focus on first marker
            map.setCenter(new GeoCoordinate(markers.get(0).getLat(), markers.get(0).getLng(), 0.0), Map.Animation.NONE);
        } else {
            // Set the map center to the Vancouver region (no animation)
            ///set map to users current location or a default when current location not found
            if (lastLocation != null) {
                map.setCenter(new GeoCoordinate(lastLocation.getLatitude(), lastLocation.getLongitude(), 0.0), Map.Animation.NONE);
            } else {
                map.setCenter(new GeoCoordinate(49.196261, -123.004773, 0.0), Map.Animation.NONE);
            }
        }
    }

    public void setTourIdFromActivity(long idTour) {
        tourId = idTour;
        if (map != null) {
            setMarkers();
        }
    }

    private void setListeners() {
        root.findViewById(R.id.cancel).setOnClickListener(this);
        root.findViewById(R.id.recordVideo).setOnClickListener(this);
        root.findViewById(R.id.playVideo).setOnClickListener(this);
        colorTextView.setOnClickListener(this);
        root.findViewById(R.id.add).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playVideo:
                if (videoFile != null) {
                    startActivity(new Intent(getActivity(), PlayerActivity.class).putExtra("url", videoFile));
                } else {
                    // If video is null.
                    Toast.makeText(getActivity(), "Please record a video first before playing", Toast.LENGTH_LONG).show();
                }
                return;

            case R.id.recordVideo:
                checkPerMission();
                return;
            case R.id.cancel:
                updateMarkerEntity = null;
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                resetSheet();
                return;
            case R.id.selectColor:
                popupMenu.show();
                return;
            case R.id.add:
                if (TextUtils.isEmpty(markerTitle.getText())) {
                    Toast.makeText(getActivity(), R.string.valid_marker_name, Toast.LENGTH_LONG).show();
                    return;
                }
                if (selectedColorIndex == -1) {
                    Toast.makeText(getActivity(), R.string.valid_color_name, Toast.LENGTH_LONG).show();
                    return;
                }

                if (map != null) {
                    if (updateMarkerEntity != null) {
                        updateMarker();
                    } else {
                        createMarker();
                    }
                    resetSheet();
                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                } else {
                    // If map is null.
                }
                return;
        }
    }

    private void resetSheet() {
        selectedColorIndex = -1;
        markerTitle.setText(null);
        videoFile = null;
        colorTextView.setText(R.string.select_color);
    }

    private void updateMarker() {
        updateMarkerEntity.setTitle(markerTitle.getText().toString());
        updateMarkerEntity.setColor(selectedColorIndex);
        updateMarkerEntity.setVideo(videoFile);
        contentDao.updateMarker(updateMarkerEntity);
        map.removeMapObject(mapMarkerUpdate);
        addMarker(updateMarkerEntity);
        updateMarkerEntity = null;
    }

    private void createMarker() {
        MarkerEntity marker = new MarkerEntity();
        marker.setTourId(this.tourId);
        marker.setTitle(markerTitle.getText().toString());
        marker.setColor(selectedColorIndex);
        marker.setLat(selectedGeoCoordinate.getLatitude());
        marker.setLng(selectedGeoCoordinate.getLongitude());
        marker.setVideo(videoFile);
        contentDao.insertMarker(marker);
        addMarker(contentDao.getLastMarker(this.tourId));
    }

    private void addMarker(MarkerEntity markerEntity) {
        Image image = new Image();
        image.setBitmap(Utils.getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_baseline_location_on_24, markerEntity.getColor()));
        MapMarker marker = new MapMarker(new GeoCoordinate(markerEntity.getLat(), markerEntity.getLng(), 0.0), image);
        marker.setTitle(markerEntity.getTitle());
        map.addMapObject(marker);
        marker.setDescription("" + markerEntity.getId());
    }

    private void initialize() {

        // Search for the map fragment to finish setup by calling init().
        mapFragment =
                (AndroidXMapFragment) getChildFragmentManager().findFragmentById(R.id.mapfragment);

        //activity.supportFragmentManagerchildFragmentManager.beginTransaction().replace(R.id.mapfragmentContainer,mapFragment!!).commit()

        // Set up disk cache path for the map service for this application
        // It is recommended to use a path under your application folder for storing the disk cache
        boolean success = MapSettings.setIsolatedDiskCacheRootPath(AppController.getInstance().getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + File.separator.toString() + ".here-maps");
        if (!success) {
            Toast.makeText(AppController.getInstance(), "Unable to set isolated disk cache path.", Toast.LENGTH_LONG).show();
        } else {
            mapFragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(Error error) {
                    root.findViewById(R.id.pb).setVisibility(View.GONE);
                    if (error == OnEngineInitListener.Error.NONE) {
                        if (mapFragment == null) {
                            return;
                        }
                        map = mapFragment.getMap();
                        map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
                        setMarkers();
                    }
                }
            });

        }
    }

    private MapGesture.OnGestureListener listener = new MapGesture.OnGestureListener.OnGestureListenerAdapter() {
        @Override
        public boolean onMapObjectsSelected(@NonNull List<ViewObject> list) {
            for (ViewObject viewObject : list) {
                if (viewObject.getBaseType() == ViewObject.Type.USER_OBJECT) {
                    MapObject mapObject = (MapObject) viewObject;
                    if (mapObject.getType() == MapObject.Type.MARKER) {
                        mapMarkerUpdate = (MapMarker) mapObject;
                        showUpdateMarkerDialog(contentDao.getMarker(Long.parseLong(mapMarkerUpdate.getDescription())));
                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean onTapEvent(@NonNull PointF pointF) {
            if (updateMarkerEntity == null) {
                selectedGeoCoordinate = map.pixelToGeo(pointF);
                ((Button) root.findViewById(R.id.add)).setText(R.string.add);
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            return false;
        }
    };

    private void showSnackView() {
        Snackbar.make(root, R.string.add_marker_info, Snackbar.LENGTH_LONG).show();
    }

    private void setUpColorMenu() {
        popupMenu = new androidx.appcompat.widget.PopupMenu(getActivity(), colorTextView);
        for (int i = 0; i < colorNames.length; i++) {
            popupMenu.getMenu().add(0, i, 0, colorNames[i]);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                selectedColorIndex = item.getItemId();
                colorTextView.setText(colorNames[item.getItemId()]);
                return true;
            }
        });
    }

    private void checkPerMission() {
        String[] PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        Utils.PermissionStatus status = Utils.checkPermissions(
                requireActivity(),
                Utils.PERMISSION_REQUEST,
                PERMISSIONS,
                this, R.string.storage_permission_msg
        );

        if (status == Utils.PermissionStatus.SUCCESS) {
            videoFile = Utils.pickVideoFromCameraFragment(MapFragment.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 2) {
            videoFile = Utils.pickVideoFromCameraFragment(MapFragment.this);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            videoFile = null;
            return;
        }

    }



    private void showUpdateMarkerDialog(MarkerEntity markerEntity) {
        updateMarkerEntity = markerEntity;
        ((Button)root.findViewById(R.id.add)).setText(R.string.update);
        selectedColorIndex = markerEntity.getColor();
        colorTextView.setText(colorNames[selectedColorIndex]);
        markerTitle.setText(markerEntity.getTitle());
        selectedGeoCoordinate = new GeoCoordinate(markerEntity.getLat(), markerEntity.getLng(), 0.0);
        videoFile = markerEntity.getVideo();
    }

}
