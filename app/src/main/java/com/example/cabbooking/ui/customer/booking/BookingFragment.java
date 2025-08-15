package com.example.cabbooking.ui.customer.booking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.cabbooking.Constants;
import com.example.cabbooking.R;
import com.example.cabbooking.model.Booking;
import com.example.cabbooking.model.DriverLocation;
import com.example.cabbooking.model.GoogleMaps.MyClusterItem;
import com.example.cabbooking.model.User;
import com.example.cabbooking.ui.customer.booking.checkout.CheckoutFragment;
import com.example.cabbooking.ui.customer.booking.checkout.CheckoutViewModel;
import com.example.cabbooking.ui.customer.booking.driver_info_bar.DriverInfoBarFragment;
import com.example.cabbooking.ui.customer.booking.driver_info_bar.DriverInfoBarViewModel;
import com.example.cabbooking.ui.customer.booking.dropoff.DropoffFragment;
import com.example.cabbooking.ui.customer.booking.pickup.PickupFragment;
import com.example.cabbooking.ui.customer.booking.popup_driver_arrived.PopupDriverArrivalFragment;
import com.example.cabbooking.ui.customer.booking.popup_driver_arrived.PopupDriverArrivalViewModel;
import com.example.cabbooking.ui.customer.booking.popup_driver_info.PopupDriverInfoFragment;
import com.example.cabbooking.ui.customer.booking.popup_driver_info.PopupDriverInfoViewModel;
import com.example.cabbooking.ui.customer.booking.processing_booking.ProcessingBookingFragment;
import com.example.cabbooking.ui.customer.booking.processing_booking.ProcessingBookingViewModel;
import com.example.cabbooking.ui.customer.booking.rating.RatingFragment;
import com.example.cabbooking.ui.customer.booking.rating.RatingViewModel;
import com.example.cabbooking.utilities.DirectionsJSONParser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BookingFragment extends Fragment implements OnMapReadyCallback {

    private BookingViewModel mViewModel;

    //View elements
    private FloatingActionButton getMyLocationBtn;
    private FloatingActionButton restartBookingBtn;

    //Maps marker clustering
    private ClusterManager<MyClusterItem> clusterManager;

    //Google maps variables
    private static final int MY_LOCATION_REQUEST = 99;
    private SupportMapFragment supportMapFragment; //maps view
    private GoogleMap mMap;
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private Marker currentPickupLocationMarker;
    private Marker currentDropOffLocationMarker;
    private Marker currentUserLocationMarker;
    private Marker currentDriverLocationMarker;
    private Location currentUserLocation;
    private LatLng prevUserLocation;
    private MyClusterItem currentTargetLocationClusterItem;
    private LatLng prevTargetLocation;
    private ArrayList<Polyline> currentRoute = new ArrayList<>();
    private PlacesClient placesClient;

    //Firebase, FireStore
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    User currentUserObject = null;

    //Booking info
    Place customerDropOffPlace;
    Place customerPickupPlace;
    String transportationType;
    Double distanceInKm;
    String distanceInKmString;
    String priceInVNDString;

    //Booking flow
    Boolean bookBtnPressed;
    Boolean cancelBookingBtnPressed;
    DocumentReference currentBookingDocRef;
    User currentDriver;
    ListenerRegistration currentBookingListener;
    ListenerRegistration currentDriverListener;

    public static BookingFragment newInstance() {
        return new BookingFragment();
    }

    /**
     * Init Google MapsFragment
     */
    private void initMapsFragment() {
        supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.fragment_maps);
        supportMapFragment.getMapAsync(this);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_customer_booking, container, false);
        linkViewElements(view); //Link view elements to class properties
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        initMapsFragment();
        setActionHandlers();


        return view;
    }

    /**
     * Connect view elements of layout to this class variable
     *
     * @param rootView
     */
    private void linkViewElements(View rootView) {
        getMyLocationBtn = rootView.findViewById(R.id.fragmentMapsFindMyLocationBtn);
        restartBookingBtn = rootView.findViewById(R.id.fragmentMapsBackBtn);
    }

    /**
     * Set Action Handlers
     */
    private void setActionHandlers() {
        setGetMyLocationBtnHandler(); //Find My location Button listener
        setRestartBtnHandler();
    }

    /**
     * Set event listener for restart btn
     */
    private void setRestartBtnHandler() {
        restartBookingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBookingFlow();
            }
        });
    }

    /**
     * Reset booking
     */
    private void resetBookingFlow() {
        //Remove listener to driver driver marker
        removeListenerForDrawingDriverMarker();
        //Remove listener to current booking
        removeListenerForCurrentBooking();
        //Remove all markers if existed
        removeAllMarkers();
        //Remove current route
        removeCurrentRoute();
        //Go back to the picking drop-off place step
        loadDropOffPlacePickerFragment();
        //Hide back btn
        restartBookingBtn.setVisibility(View.GONE);
    }

    /**
     * Remove all the marker existing in the map fragment
     */
    private void removeAllMarkers() {
        //Clear pickup/drop-off markers if exists
        if (currentPickupLocationMarker != null) {
            currentPickupLocationMarker.remove();
            currentPickupLocationMarker = null;
        }

        //Clear drop-off markers if exists
        if (currentDropOffLocationMarker != null) {
            currentDropOffLocationMarker.remove();
            currentDropOffLocationMarker = null;
        }

        if (currentDriverLocationMarker != null) {
            currentDriverLocationMarker.remove();
            currentDriverLocationMarker = null;
        }

    }

    /**
     * Load drop-off picker fragment
     */
    private void loadDropOffPlacePickerFragment() {
        //Load drop-off picker fragment
        DropoffFragment dropoffFragment = new DropoffFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.booking_info, dropoffFragment).commit();
    }

    /**
     * Load pick up picker fragment
     */
    private void loadPickupPlacePickerFragment() {
        PickupFragment pickupFragment = new PickupFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.booking_info, pickupFragment).commit();
    }

    /**
     * Load checkout fragment
     */
    private void loadCheckoutFragment() {
        CheckoutFragment checkoutFragment = new CheckoutFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.booking_info, checkoutFragment).commit();
    }
    /**
     * Draw marker on dropoff and pickup map fragment
     */
    private void drawDropOffAndPickupMarkers() {
        currentPickupLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(Objects.requireNonNull(customerPickupPlace.getLatLng()))
                .icon(bitmapDescriptorFromVector(
                        getActivity(),
                        R.drawable.ic_location_blue, Color.BLUE)
                )
                .title(customerPickupPlace.getAddress()));

        currentDropOffLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(customerDropOffPlace.getLatLng())
                .icon(bitmapDescriptorFromVector(
                        getActivity(),
                        R.drawable.ic_location_red, Color.RED)
                )
                .title(customerDropOffPlace.getAddress()));

        currentPickupLocationMarker.showInfoWindow();
        currentDropOffLocationMarker.showInfoWindow();

        //Smoothly move camera to include 2 points in the map
        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
        latLngBounds.include(customerDropOffPlace.getLatLng());
        latLngBounds.include(customerPickupPlace.getLatLng());
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 200));
    }

    /**
     * Draw route from pickup location to drop off location on the map fragment
     */
    private void drawRouteFromPickupToDropOff() {
        // Checks, whether start and end locations are captured
        // Getting URL to the Google Directions API
        String url = getRouteUrl(customerPickupPlace.getLatLng(), customerDropOffPlace.getLatLng(), "driving");

        FetchRouteDataTask fetchRouteDataTask = new FetchRouteDataTask();

        // Start fetching json data from Google Directions API
        fetchRouteDataTask.execute(url);
    }


    /**
     * //Find My location Button listener
     */
    private void setGetMyLocationBtnHandler() {
        getMyLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGetPositionClick();
            }
        });
    }

    /**
     * Smoothly change camera position with zoom level
     *
     * @param latLng
     * @param zoomLevel
     */
    private void smoothlyMoveCameraToPosition(LatLng latLng, float zoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.animateCamera(cameraUpdate);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }


    /**
     * Find my position action handler
     */
    @SuppressLint("MissingPermission")
    public void onGetPositionClick() {
        locationClient.getLastLocation().
                addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Toast.makeText(getActivity(),
                                    Constants.ToastMessage.currentLocationNotUpdatedYet,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        currentUserLocation = location;
                        if (currentUserLocationMarker == null) {
                            updateCurrentUserLocationMarker(latLng);
                        }
                        smoothlyMoveCameraToPosition(latLng, Constants.GoogleMaps.CameraZoomLevel.streets);
                    }
                });
    }


    /**
     * Update current user location marker
     *
     * @param newLatLng
     */
    private void updateCurrentUserLocationMarker(LatLng newLatLng) {
        if (currentUserLocationMarker != null) {
            currentUserLocationMarker.remove();
        }
        currentUserLocationMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(newLatLng)
                        .icon(bitmapDescriptorFromVector(
                                getActivity(),
                                R.drawable.ic_current_location_marker, Color.BLUE)
                        )
                        .title("You are here!")
        );
    }

    /**
     * Get BitmapDescriptor from drawable vector asset, for custom cluster marker
     *
     * @param context
     * @param vectorResId
     * @param color
     * @return
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int color) {
        if (context == null) {
            return null;
        }
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        DrawableCompat.setTint(vectorDrawable, color);
        DrawableCompat.setTintMode(vectorDrawable, PorterDuff.Mode.SRC_IN);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        loadDropOffPlacePickerFragment();
        resetBookingFlow();
    }

    /**
     * Request user for location permission
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_LOCATION_REQUEST);
    }

    /**
     * //Start location update listener
     */
    @SuppressLint({"MissingPermission", "RestrictedApi"})
    private void startLocationUpdate() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000); //5s
        locationRequest.setFastestInterval(5 * 1000); //5s
        locationClient.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        Location location = locationResult.getLastLocation();
                        LatLng latLng = new LatLng(location.getLatitude(),
                                location.getLongitude());
                        updateCurrentUserLocationMarker(latLng);
//                        updateCurrentRoute();

                    }
                }
                , null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) { //Init GooglePlaceAutocomplete if not existed
            Places.initialize(requireActivity().getApplicationContext(), apiKey);
        }
        this.placesClient = Places.createClient(requireActivity().getApplicationContext());
        mMap = googleMap;
        requestPermission(); //Request user for location permission
        locationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mMap.getUiSettings().setZoomControlsEnabled(true);
        startLocationUpdate(); //Start location update listener
//        setUpCluster(); //Set up cluster on Google Map
        onGetPositionClick();  // Position the map.
    }

    /**
     * Clear the route in the map
     */
    private void removeCurrentRoute() {
        //Clear current route
        if (currentRoute.isEmpty()) return;
        for (Polyline polyline : currentRoute) {
            polyline.remove();
        }
        currentRoute.clear();
    }

    /**
     * Sent the required data to checkout fragment
     */
    @SuppressLint("DefaultLocale")
    private void sendCheckoutInfoToCheckoutFragment() {
        CheckoutViewModel checkoutViewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        String distanceStr = String.format("%.1fkm", distanceInKm);  // assuming distance variable name
        checkoutViewModel.setDistanceInKmString(distanceStr);

        if (transportationType.equals(Constants.Transportation.Type.carType)) {
            int price = (int) (distanceInKm * Constants.Transportation.UnitPrice.carType);
            String priceStr = "Rs. " + price;
            checkoutViewModel.setPriceInVNDString(priceStr);
        } else {
            // For bike, send null or empty to hide price
            checkoutViewModel.setPriceInVNDString(null);
        }
    }


    /**
     * Reset all BookingViewModel data to null to prevent caching
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.setCustomerSelectedDropOffPlace(null);
        mViewModel.setCustomerSelectedPickupPlace(null);
        mViewModel.setTransportationType(null);
        mViewModel.setBookBtnPressed(null);
        mViewModel.setCancelBookingBtnPressed(null);
        mViewModel.setFeedBackRating(null);
    }

    /**
     * Send data to ProcessBookingViewModel
     */
    private void sendDataToProcessBookingViewModel() {
        ProcessingBookingViewModel processingBookingViewModel = new ViewModelProvider(requireActivity()).get(ProcessingBookingViewModel.class);
        processingBookingViewModel.setDropOffPlaceString(customerDropOffPlace.getName());
        processingBookingViewModel.setPickupPlaceString(customerPickupPlace.getName());
        processingBookingViewModel.setPriceInVNDString(priceInVNDString);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);

        mViewModel.getCurrentUserObject().observe(requireActivity(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUserObject = user;
            }
        });

        //Action handler when customer's chosen drop off place is selected
        mViewModel.getCustomerSelectedDropOffPlace().observe(getViewLifecycleOwner(), new Observer<Place>() {
            @Override
            public void onChanged(Place place) {
                if (place == null) return;
                customerDropOffPlace = place;
                restartBookingBtn.setVisibility(View.VISIBLE); //Show back button

                //TODO Move to customerPickUpPlace fragment
                loadPickupPlacePickerFragment();

                smoothlyMoveCameraToPosition(
                        new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()),
                        Constants.GoogleMaps.CameraZoomLevel.betweenStreetsAndBuildings);
            }
        });

        //Action handler when customer's chosen pickup place is selected
        mViewModel.getCustomerSelectedPickupPlace().observe(getViewLifecycleOwner(), new Observer<Place>() {
            @Override
            public void onChanged(Place place) {
                if (place == null) return;
                customerPickupPlace = place;

                //TODO load checkout fragment
                loadCheckoutFragment();

                //TODO Draw 2 pickup/drop-off markers
                drawDropOffAndPickupMarkers();

                //TODO Draw route from pickup place to drop-off place
                drawRouteFromPickupToDropOff();
            }
        });

        mViewModel.getTransportationType().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s == null) return;
                transportationType = s;
            }
        });

        //*********************** For booking synchronization between user and driver flow *********************** //

        //Book btn pressed
        mViewModel.getBookBtnPressed().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == null) return;
                restartBookingBtn.setVisibility(View.GONE);
                removeCurrentRoute(); //Remove drawn route
                createNewBookingInDB(); //Create new booking in DB, set listener to update for driver accepting this booking
                sendDataToProcessBookingViewModel();
                loadProcessingBookingFragment(); //Load processing booking fragment
            }
        });

        //Cancel booking btn pressed
        mViewModel.getCancelBookingBtnPressed().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == null) return;
                resetBookingFlow();
                cancelBooking();
            }
        });

        mViewModel.getFeedBackRating().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == null) return;
                ArrayList<Integer> ratingList = (ArrayList<Integer>) currentDriver.getRating();
                ratingList.add(integer);
                db.collection(Constants.FSUser.userCollection)
                        .whereEqualTo(Constants.FSUser.emailField, currentDriver.getEmail())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    User driver = doc.toObject(User.class);
                                    db.collection(Constants.FSUser.userCollection)
                                            .document(driver.getDocId())
                                            .update(Constants.FSUser.rating, ratingList)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    System.out.println("Update Rating success");
                                                }
                                            });
                                    resetBookingFlow();
                                }
                            }
                        });
            }
        });
    }

    /*************************************************** For booking synchronization *****************************************************/
    /**
     * Load process booking fragment
     */
    private void loadProcessingBookingFragment() {
        //Load drop-off picker fragment
        ProcessingBookingFragment processingBookingFragment = new ProcessingBookingFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.booking_info, processingBookingFragment).commit();
    }

    /**
     * Load DriverInfoBarFragment
     */
    private void loadDriverInfoBarFragment() {
        //Load driver info bar fragment
        DriverInfoBarFragment driverInfoBarFragment = new DriverInfoBarFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.booking_info, driverInfoBarFragment).commit();
    }

    /**
     * load PopupFounderDriverInfo
     */
    private void loadPopupFoundedDriverInfo() {
        FragmentManager fm = getChildFragmentManager();
        PopupDriverInfoFragment popupDriverInfoFragment = PopupDriverInfoFragment.newInstance();
        popupDriverInfoFragment.show(fm, "fragment_notify_founded_driver");
    }

    /**
     * Load PopupDriverArrivalFragment
     */
    private void loadPopupDriverArrivalFragment() {
        FragmentManager fm = getChildFragmentManager();
        PopupDriverArrivalFragment popUpDriverArrivalFragment = PopupDriverArrivalFragment.newInstance();
        popUpDriverArrivalFragment.show(fm, "fragment_notify_driver_arrived");
    }

    /**
     * load RatingFragment
     */
    private void loadCustomerRatingFragment() {
        //Load customer rating fragment
//        RatingFragment ratingFragment = new RatingFragment();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.replace(R.id.booking_info, ratingFragment).commit();
        FragmentManager fm = getChildFragmentManager();
        RatingFragment ratingFragment = RatingFragment.newInstance();
        ratingFragment.show(fm, "fragment_feedback_rating");
    }

    /**
     * Create booking in db
     */
    private void createNewBookingInDB() {
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.FSBooking.pickupPlaceAddress, customerPickupPlace.getAddress());
        data.put(Constants.FSBooking.pickUpPlaceLatitude, customerPickupPlace.getLatLng().latitude);
        data.put(Constants.FSBooking.pickUpPlaceLongitude, customerPickupPlace.getLatLng().longitude);
        data.put(Constants.FSBooking.dropOffPlaceAddress, customerDropOffPlace.getAddress());
        data.put(Constants.FSBooking.dropOffPlaceLatitude, customerDropOffPlace.getLatLng().latitude);
        data.put(Constants.FSBooking.dropOffPlaceLongitude, customerDropOffPlace.getLatLng().longitude);

        data.put(Constants.FSBooking.distanceInKm, distanceInKmString);
        data.put(Constants.FSBooking.priceInVND, priceInVNDString);
        data.put(Constants.FSBooking.transportationType, transportationType);
        data.put(Constants.FSBooking.available, true);
        data.put(Constants.FSBooking.finished, false);
        data.put(Constants.FSBooking.arrived, false);
        data.put(Constants.FSBooking.driver, null);

        db.collection(Constants.FSBooking.bookingCollection)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        currentBookingDocRef = documentReference;
                        setDetectAcceptedDriver();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireActivity(), Constants.ToastMessage.addNewBookingToDbFail, Toast.LENGTH_SHORT).show();
                        resetBookingFlow();
                    }
                });
    }

    /**
     * send data to driverInfoBarViewModel
     */
    private void sendDataToInfoBarViewModel() {
        DriverInfoBarViewModel driverInfoBarViewModel = new ViewModelProvider(requireActivity()).get(DriverInfoBarViewModel.class);
        driverInfoBarViewModel.setDriver(currentDriver);
    }

    /**
     * Send data to popupDriverArrivalViewModel
     */
    private void sendDataToPopupDriverArrivalViewModel() {
        PopupDriverArrivalViewModel popupDriverArrivalViewModel = new ViewModelProvider(requireActivity()).get(PopupDriverArrivalViewModel.class);
        popupDriverArrivalViewModel.setDriver(currentDriver);
    }

    /**
     * Send data to ratingViewModel
     */
    private void sendDataToRatingViewModel(){
        RatingViewModel ratingViewModel = new ViewModelProvider(requireActivity()).get(RatingViewModel.class);
        ratingViewModel.setDriver(currentDriver);
    }

    /**
     * Set driver for a booking
     */
    private void setDetectAcceptedDriver() {
        currentBookingListener = currentBookingDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null && value.exists()) {
                    Booking booking = value.toObject(Booking.class);
                    User driver = booking.getDriver();
                    if (driver != null) {
                        currentDriver = driver;
                        sendDriverObjectToPopupDriverViewModel();
                        loadPopupFoundedDriverInfo();
                        setListenerForDrawingDriverMarker();
                        setListenerForDriverArrival();
                        sendDataToInfoBarViewModel();
                        loadDriverInfoBarFragment();
                    }

                }
            }
        });
    }

    /**
     * Event listener for driver arrival
     */
    private void setListenerForDriverArrival() {
        currentBookingListener.remove();
        currentBookingListener = currentBookingDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null && value.exists()) {
                    Booking booking = value.toObject(Booking.class);
                    //If driver has arrived
                    if (booking.getArrived()) {
                        sendDataToPopupDriverArrivalViewModel();
                        loadPopupDriverArrivalFragment();
                        setListenerForBookingFinished();
                    }
                }
            }
        });
    }

    /**
     * Event listener for finishing booking
     */
    private void setListenerForBookingFinished() {
        currentBookingListener.remove();
        currentBookingListener = currentBookingDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null && value.exists()) {
                    Booking booking = value.toObject(Booking.class);
                    //If driver has finished this booking
                    if (booking.getFinished()) {
                        //TODO aaaaaaahhhhhhhhhhhhhhhhhhhhhhhhhh
                        System.out.println("Finisheddddddddd this trip");
                        sendDataToRatingViewModel();
                        loadCustomerRatingFragment();
                    }
                }
            }
        });
    }

    /**
     * Event listener for driver marker
     */
    private void setListenerForDrawingDriverMarker() {
        int resourceType;
        if (transportationType.equals(Constants.Transportation.Type.carType)) {
            resourceType = R.drawable.ic_checkout_car;
        } else {
            resourceType = R.drawable.ic_checkout_bike;

        }
        db.collection(Constants.FSUser.userCollection)
                .whereEqualTo(Constants.FSUser.emailField, currentDriver.getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            User driver = doc.toObject(User.class);
                            currentDriverListener = db.collection(Constants.FSDriverLocation.driverLocationCollection)
                                    .document(driver.getDocId())
                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if (error != null) {
                                                return;
                                            }

                                            if (value != null && value.exists()) {

                                                DriverLocation driverLocation = value.toObject(DriverLocation.class);
                                                if (currentDriverLocationMarker != null) {
                                                    currentDriverLocationMarker.remove();
                                                    currentDriverLocationMarker = null;
                                                }

                                                currentDriverLocationMarker = mMap.addMarker(
                                                        new MarkerOptions()
                                                                .position(new LatLng(driverLocation.getCurrentPositionLatitude(),
                                                                        driverLocation.getCurrentPositionLongitude()))
                                                                .icon(bitmapDescriptorFromVector(
                                                                        getActivity(),
                                                                        resourceType, Color.RED)
                                                                )
                                                                .title("Driver is here!")
                                                );
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * Remove listener for driver marker
     */
    private void removeListenerForDrawingDriverMarker() {
        if (currentDriverListener == null) return;
        currentDriverListener.remove();
        currentDriverListener = null;
    }

    /**
     * Send data to PopupDriverInfoViewModel
     */
    private void sendDriverObjectToPopupDriverViewModel() {
        PopupDriverInfoViewModel popupDriverInfoViewModel = new ViewModelProvider(requireActivity()).get(PopupDriverInfoViewModel.class);
        popupDriverInfoViewModel.setDriver(currentDriver);
    }

    /**
     * Remove listener for current booking
     */
    private void removeListenerForCurrentBooking() {
        if (currentBookingListener == null) return;
        currentBookingListener.remove();
        currentBookingListener = null;
    }

    /**
     * Cancel booking
     */
    private void cancelBooking() {
        currentBookingDocRef.update(Constants.FSBooking.available, false); //Set available field to false
        if (currentBookingListener != null) currentBookingListener.remove(); //Remove listener
    }

    /*************************************************** For booking synchronization *****************************************************/

    private void drawRoute(List<List<HashMap<String, String>>> result) {
        // Clear current route
        for (Polyline polyline : currentRoute) {
            polyline.remove();
        }
        currentRoute.clear();

        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;

        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> route = result.get(i);

            for (int j = 0; j < route.size(); j++) {
                HashMap<String, String> point = route.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            if (!points.isEmpty()) {
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);
            } else {
                Log.e("drawRoute", "No points found for route index " + i);
            }
        }

        // Ensure `lineOptions` is not null before adding it to the map
        if (lineOptions != null && !lineOptions.getPoints().isEmpty()) {
            currentRoute.add(mMap.addPolyline(lineOptions));
        } else {
            Log.e("drawRoute", "PolylineOptions is null or has no points.");
        }
    }


    /**
     * A Class to call Google Directions API with callback
     */
    private class FetchRouteDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = fetchDataFromURL(url[0]);
            } catch (Exception ignored) {
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            RouteParserTask routeParserTask = new RouteParserTask();
            routeParserTask.execute(result);
        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class RouteParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser(jObject);

                routes = parser.getRoutes();

                distanceInKm = parser.getTotalDistanceInKm();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            sendCheckoutInfoToCheckoutFragment(); //Send calculated checkout info to checkout fragment
            drawRoute(result); //Draw new route
        }
    }

    /**
     * Method to get URL for fetching data from Google Directions API (finding direction from origin to destination)
     *
     * @param origin
     * @param destination
     * @param directionMode
     * @return
     */
    private String getRouteUrl(LatLng origin, LatLng destination, String directionMode) {
        String originParam = Constants.GoogleMaps.DirectionApi.originParam +
                "=" + origin.latitude + "," + origin.longitude;
        String destinationParam = Constants.GoogleMaps.DirectionApi.destinationParam +
                "=" + destination.latitude + "," + destination.longitude;
        String modeParam = Constants.GoogleMaps.DirectionApi.modeParam + "=" + directionMode;
        String params = originParam + "&" + destinationParam + "&" + modeParam;
        String output = Constants.GoogleMaps.DirectionApi.outputParam;
        return Constants.GoogleMaps.DirectionApi.baseUrl + output + "?" + params
                + "&key=" + getString(R.string.google_maps_key);
    }

    /**
     * A method to fetch json data from url
     */
    private String fetchDataFromURL(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}