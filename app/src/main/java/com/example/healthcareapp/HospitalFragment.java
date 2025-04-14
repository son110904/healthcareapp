package com.example.healthcareapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText searchEditText;
    private Button findNearestButton;
    private List<Hospital> hospitalList;
    private Map<String, Marker> hospitalMarkers;
    private boolean locationPermissionGranted = false;
    private LatLng currentUserLocation;
    private Polyline currentRoute;
    private Marker nearestHospitalMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hospital, container, false);

        // Khởi tạo components
        initComponents(view);

        return view;
    }

    private void initComponents(View view) {
        // Khởi tạo danh sách bệnh viện
        hospitalList = new ArrayList<>();

        // Khởi tạo Map để lưu trữ các marker
        hospitalMarkers = new HashMap<>();

        // Khởi tạo FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Thiết lập thanh tìm kiếm
        searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchHospitals(s.toString());
            }
        });

        // Thiết lập nút tìm bệnh viện gần nhất
        findNearestButton = view.findViewById(R.id.findNearestButton);
        findNearestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findAndDisplayNearestHospital();
            }
        });
        // Thiết lập nút quay lại
        ImageView backButton = view.findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Quay lại fragment trước đó
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        // Khởi tạo bản đồ
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Tải dữ liệu bệnh viện
        loadHospitalData();
    }

    private void loadHospitalData() {
        // Miền Bắc
        hospitalList.add(new Hospital(21.0022, 105.8418, "Bệnh viện Bạch Mai", "Số 78 Giải Phóng, Phương Mai, Đống Đa, Hà Nội", true));
        hospitalList.add(new Hospital(21.0137, 105.8546, "Bệnh viện 108", "Số 1 Trần Hưng Đạo, Hai Bà Trưng, Hà Nội", true));
        hospitalList.add(new Hospital(21.0373, 105.8346, "Bệnh viện Việt Đức", "40 Tràng Thi, Hàng Bông, Hoàn Kiếm, Hà Nội", true));
        hospitalList.add(new Hospital(21.0228, 105.8416, "Bệnh viện Hữu nghị", "Số 1 Trần Khánh Dư, Hai Bà Trưng, Hà Nội", true));
        hospitalList.add(new Hospital(21.0016, 105.8606, "Bệnh viện E", "Số 87-89 Trần Cung, Nghĩa Tân, Cầu Giấy, Hà Nội", false));
        hospitalList.add(new Hospital(20.8507, 106.6881, "Bệnh viện Việt Tiệp Hải Phòng", "Số 1 Nhà Thương, Cát Dài, Lê Chân, Hải Phòng", false));
        hospitalList.add(new Hospital(21.0543, 105.8995, "Bệnh viện Đa khoa Đức Giang", "Số 1 Trương Lâm, Đức Giang, Long Biên, Hà Nội", false));

        // Miền Trung
        hospitalList.add(new Hospital(16.0678, 108.2208, "Bệnh viện Đà Nẵng", "124 Hải Phòng, Thạch Thang, Hải Châu, Đà Nẵng", true));
        hospitalList.add(new Hospital(16.0552, 108.2122, "Bệnh viện C Đà Nẵng", "122 Hải Phòng, Thạch Thang, Hải Châu, Đà Nẵng", false));
        hospitalList.add(new Hospital(12.2388, 109.1967, "Bệnh viện Đa khoa Khánh Hòa", "19 Yersin, Phước Tân, Nha Trang, Khánh Hòa", false));
        hospitalList.add(new Hospital(13.7751, 109.2252, "Bệnh viện Đa khoa tỉnh Bình Định", "Số 1 Nguyễn Tất Thành, Qui Nhơn, Bình Định", false));

        // Miền Nam
        hospitalList.add(new Hospital(10.7568, 106.6573, "Bệnh viện Chợ Rẫy", "201B Nguyễn Chí Thanh, Phường 12, Quận 5, TP. HCM", true));
        hospitalList.add(new Hospital(10.7840, 106.6786, "Bệnh viện Đại học Y Dược TP.HCM", "215 Hồng Bàng, Phường 11, Quận 5, TP. HCM", true));
        hospitalList.add(new Hospital(10.7555, 106.6651, "Bệnh viện Nhân dân 115", "527 Sư Vạn Hạnh, Phường 12, Quận 10, TP. HCM", true));
        hospitalList.add(new Hospital(10.8039, 106.6438, "Bệnh viện Thống Nhất", "1 Lý Thường Kiệt, Phường 7, Tân Bình, TP. HCM", false));
        hospitalList.add(new Hospital(10.0301, 105.7762, "Bệnh viện Đa khoa Cần Thơ", "Số 30 Nguyễn Văn Cừ, An Bình, Ninh Kiều, Cần Thơ", false));

        // Miền Núi Bắc Bộ
        hospitalList.add(new Hospital(22.3431, 103.9623, "Bệnh viện Đa khoa Lai Châu", "Đội 6, Tổ 2, Phường Quyết Thắng, Lai Châu", false));
        hospitalList.add(new Hospital(22.8521, 104.8722, "Bệnh viện Đa khoa Hà Giang", "Số 2, Đường Nguyễn Trãi, Phường Ngọc Hà, Hà Giang", false));
        hospitalList.add(new Hospital(22.1538, 106.2497, "Bệnh viện Đa khoa Cao Bằng", "Đường Hoàng Như Tiếp, Phường Sông Hiến, Cao Bằng", false));

        // Miền Quê
        hospitalList.add(new Hospital(20.4505, 106.3442, "Bệnh viện Đa khoa Hưng Yên", "Phường An Tảo, Hưng Yên", false));
        hospitalList.add(new Hospital(20.9588, 105.7702, "Bệnh viện Đa khoa Sóc Sơn", "Xã Xuân Thu, Sóc Sơn, Hà Nội", false));
        hospitalList.add(new Hospital(10.3031, 106.3328, "Bệnh viện Đa khoa Đức Hòa", "Đức Hòa, Long An", false));
        hospitalList.add(new Hospital(9.7648, 106.3269, "Bệnh viện Đa khoa Châu Thành", "Châu Thành, Tiền Giang", false));
        hospitalList.add(new Hospital(9.9741, 106.3665, "Bệnh viện Đa khoa Cái Bè", "Cái Bè, Tiền Giang", false));
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Cấu hình Google Map
        setupGoogleMap();

        // Kiểm tra và yêu cầu quyền truy cập vị trí nếu chưa có
        checkLocationPermission();

        // Thêm marker cho các bệnh viện
        addHospitalMarkers();
    }

    private void setupGoogleMap() {
        if (mMap != null) {
            // Thiết lập UI settings của map
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);

            // Thiết lập sự kiện khi click vào marker
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    // Hiển thị thông tin chi tiết khi click vào marker
                    marker.showInfoWindow();

                    // Nếu có vị trí người dùng, vẽ đường đi đến bệnh viện
                    if (currentUserLocation != null) {
                        drawRouteToBvHospital(marker.getPosition());
                    }

                    return false;
                }
            });

            // Thiết lập kiểu bản đồ
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    private void addHospitalMarkers() {
        if (mMap == null) return;

        // Xóa tất cả các marker hiện tại
        mMap.clear();
        hospitalMarkers.clear();

        // Tạo LatLngBounds để fit tất cả markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Hospital hospital : hospitalList) {
            LatLng position = new LatLng(hospital.getLatitude(), hospital.getLongitude());
            builder.include(position);

            // Sử dụng màu sắc khác nhau cho các bệnh viện lớn và nhỏ
            float markerColor = hospital.isMajor() ?
                    BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_AZURE;

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(hospital.getName())
                    .snippet(hospital.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

            Marker marker = mMap.addMarker(markerOptions);
            hospitalMarkers.put(hospital.getName(), marker);
        }

        // Nếu đã có quyền vị trí và vị trí đã được xác định, hiển thị vị trí người dùng
        if (locationPermissionGranted) {
            displayUserLocation();
        } else {
            // Nếu không có quyền vị trí, hiển thị tất cả markers
            // Thêm padding cho bounds
            int padding = 100; // pixels

            if (hospitalList.size() > 0) {
                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } else {
                // Nếu không có bệnh viện nào, hiển thị toàn bộ Việt Nam
                LatLng vietnam = new LatLng(16.0, 108.0);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vietnam, 5.5f));
            }
        }
    }

    private void searchHospitals(String query) {
        if (mMap == null) return;

        query = query.toLowerCase().trim();
        boolean found = false;

        if (query.isEmpty()) {
            // Nếu thanh tìm kiếm trống, hiển thị tất cả bệnh viện
            for (String hospitalName : hospitalMarkers.keySet()) {
                Marker marker = hospitalMarkers.get(hospitalName);
                if (marker != null) {
                    marker.setVisible(true);
                }
            }

            // Xóa polyline (đường đi) nếu có
            if (currentRoute != null) {
                currentRoute.remove();
                currentRoute = null;
            }

            // Reset highlight của bệnh viện gần nhất
            if (nearestHospitalMarker != null) {
                Hospital hospital = findHospitalByLatLng(nearestHospitalMarker.getPosition());
                if (hospital != null) {
                    nearestHospitalMarker.setIcon(BitmapDescriptorFactory.defaultMarker(
                            hospital.isMajor() ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_AZURE));
                }
                nearestHospitalMarker = null;
            }
        } else {
            // Nếu có từ khóa tìm kiếm, chỉ hiển thị các bệnh viện phù hợp
            for (Hospital hospital : hospitalList) {
                Marker marker = hospitalMarkers.get(hospital.getName());
                if (marker != null) {
                    boolean isMatch = hospital.getName().toLowerCase().contains(query) ||
                            hospital.getAddress().toLowerCase().contains(query);
                    marker.setVisible(isMatch);

                    // Nếu tìm thấy, di chuyển camera đến bệnh viện đầu tiên phù hợp
                    if (isMatch && !found) {
                        found = true;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(hospital.getLatitude(), hospital.getLongitude()), 15));

                        // Vẽ đường đi nếu có vị trí người dùng
                        if (currentUserLocation != null) {
                            drawRouteToBvHospital(marker.getPosition());
                        }
                    }
                }
            }

            // Nếu không tìm thấy kết quả nào
            if (!found) {
                Toast.makeText(requireContext(), "Không tìm thấy bệnh viện phù hợp", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền truy cập vị trí
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Đã có quyền, hiển thị vị trí người dùng
            locationPermissionGranted = true;
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (mMap == null) return;

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true); // Hiển thị nút "My Location"
            displayUserLocation();
        }
    }

    private void displayUserLocation() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Lấy vị trí hiện tại và di chuyển camera đến đó
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentUserLocation = new LatLng(
                                    location.getLatitude(), location.getLongitude());

                            // Zoom đến vị trí người dùng với mức zoom đủ lớn để thấy được các bệnh viện xung quanh
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    currentUserLocation, 13));

                            // Kích hoạt nút tìm bệnh viện gần nhất
                            findNearestButton.setEnabled(true);
                        } else {
                            // Nếu không lấy được vị trí, hiển thị toàn bộ Việt Nam
                            currentUserLocation = null;
                            LatLng vietnam = new LatLng(16.0, 108.0);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vietnam, 5.5f));

                            // Vô hiệu hóa nút tìm bệnh viện gần nhất
                            findNearestButton.setEnabled(false);

                            Toast.makeText(requireContext(),
                                    "Không thể xác định vị trí hiện tại của bạn",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void findAndDisplayNearestHospital() {
        if (mMap == null || currentUserLocation == null) {
            Toast.makeText(requireContext(), "Vui lòng đợi vị trí của bạn được xác định", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tìm bệnh viện gần nhất
        Hospital nearestHospital = findNearestHospital(currentUserLocation);

        if (nearestHospital != null) {
            // Hiển thị thông tin của bệnh viện gần nhất
            Toast.makeText(requireContext(),
                    "Bệnh viện gần nhất: " + nearestHospital.getName(),
                    Toast.LENGTH_SHORT).show();

            // Lấy marker của bệnh viện gần nhất
            nearestHospitalMarker = hospitalMarkers.get(nearestHospital.getName());

            // Đánh dấu bệnh viện gần nhất trên bản đồ (màu vàng)
            if (nearestHospitalMarker != null) {
                nearestHospitalMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                nearestHospitalMarker.showInfoWindow();

                // Zoom để nhìn thấy cả người dùng và bệnh viện
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(currentUserLocation);
                builder.include(nearestHospitalMarker.getPosition());

                // Di chuyển camera và zoom hợp lý
                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

                // Vẽ đường đi đến bệnh viện gần nhất
                drawRouteToBvHospital(nearestHospitalMarker.getPosition());
            }
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy bệnh viện nào", Toast.LENGTH_SHORT).show();
        }
    }

    private Hospital findNearestHospital(final LatLng userLocation) {
        if (hospitalList.isEmpty()) return null;

        // Sắp xếp danh sách bệnh viện theo khoảng cách gần nhất
        List<Hospital> sortedHospitals = new ArrayList<>(hospitalList);
        Collections.sort(sortedHospitals, new Comparator<Hospital>() {
            @Override
            public int compare(Hospital h1, Hospital h2) {
                float[] results1 = new float[1];
                Location.distanceBetween(
                        userLocation.latitude, userLocation.longitude,
                        h1.getLatitude(), h1.getLongitude(), results1);

                float[] results2 = new float[1];
                Location.distanceBetween(
                        userLocation.latitude, userLocation.longitude,
                        h2.getLatitude(), h2.getLongitude(), results2);

                return Float.compare(results1[0], results2[0]);
            }
        });

        // Trả về bệnh viện gần nhất
        return sortedHospitals.get(0);
    }

    private Hospital findHospitalByLatLng(LatLng position) {
        for (Hospital hospital : hospitalList) {
            if (Math.abs(hospital.getLatitude() - position.latitude) < 0.0001 &&
                    Math.abs(hospital.getLongitude() - position.longitude) < 0.0001) {
                return hospital;
            }
        }
        return null;
    }

    private void drawRouteToBvHospital(LatLng destination) {
        // Xóa polyline (đường đi) cũ nếu có
        if (currentRoute != null) {
            currentRoute.remove();
        }

        // Vẽ đường thẳng từ vị trí người dùng đến bệnh viện
        if (currentUserLocation != null) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(currentUserLocation)
                    .add(destination)
                    .width(5)
                    .color(Color.BLUE)
                    .geodesic(true);

            currentRoute = mMap.addPolyline(polylineOptions);

            // Tính và hiển thị khoảng cách
            float[] results = new float[1];
            Location.distanceBetween(
                    currentUserLocation.latitude, currentUserLocation.longitude,
                    destination.latitude, destination.longitude,
                    results);

            float distanceInKm = results[0] / 1000; // Chuyển từ mét sang km

            // Hiển thị khoảng cách
            Toast.makeText(requireContext(),
                    String.format("Khoảng cách: %.2f km", distanceInKm),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp
                locationPermissionGranted = true;
                enableMyLocation();
            } else {
                // Quyền bị từ chối
                locationPermissionGranted = false;
                Toast.makeText(requireActivity(),
                        "Vui lòng cấp quyền truy cập vị trí để sử dụng đầy đủ tính năng",
                        Toast.LENGTH_LONG).show();

                // Vẫn hiển thị bản đồ với các bệnh viện nhưng không hiển thị vị trí người dùng
                addHospitalMarkers();

                // Vô hiệu hóa nút tìm bệnh viện gần nhất
                findNearestButton.setEnabled(false);
            }
        }
    }

    // Lớp Hospital để lưu trữ thông tin bệnh viện
    private static class Hospital {
        private double latitude;
        private double longitude;
        private String name;
        private String address;
        private boolean isMajor; // Bệnh viện lớn hay không

        public Hospital(double latitude, double longitude, String name, String address, boolean isMajor) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.address = address;
            this.isMajor = isMajor;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public boolean isMajor() {
            return isMajor;
        }
    }

}