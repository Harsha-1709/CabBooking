package com.example.cabbooking;

public class Constants {

    public static class Transportation {
        public static class Type {
            public static final String carType = "car";
            public static final String bikeType = "bike";
        }

        public static class UnitPrice {
            public static final double carType = 20.0;
            public static final double bikeType = 10.0;
        }
    }

    // Fields of FireStore 'users' collection
    public static class FSUser {
        public static final String userCollection = "users";
        public static final String usernameField = "username";
        public static final String phoneField = "phone";
        public static final String birthDateField = "birthDate";
        public static final String genderField = "gender";
        public static final String emailField = "email";
        public static final String roleField = "role";
        public static final String transportationType = "transportationType";
        public static final String vehiclePlateNumber = "vehiclePlateNumber";
        public static final String rating = "rating";
        public static final String currentPositionLatitude = "currentPositionLatitude";
        public static final String currentPositionLongitude = "currentPositionLongitude";

        // **New Fields for Aadhar and Insurance**
        public static final String aadharNumber = "aadharNumber";
        public static final String insuranceNumber = "insuranceNumber";
        public static final String insuranceType = "insuranceType"; // Private/Government

        public static final String roleCustomerVal = "Customer";
        public static final String roleDriverVal = "Driver";
    }

    public static class FSBooking {
        public static final String bookingCollection = "bookings";
        public static final String pickupPlaceAddress = "pickupPlaceAddress";
        public static final String dropOffPlaceAddress = "dropOffPlaceAddress";
        public static final String pickUpPlaceLatitude = "pickUpPlaceLatitude";
        public static final String pickUpPlaceLongitude = "pickUpPlaceLongitude";
        public static final String dropOffPlaceLatitude = "dropOffPlaceLatitude";
        public static final String dropOffPlaceLongitude = "dropOffPlaceLongitude";

        public static final String driver = "driver";
        public static final String distanceInKm = "distanceInKm";
        public static final String priceInVND = "priceInVND";
        public static final String transportationType = "transportationType";
        public static final String available = "available";
        public static final String arrived = "arrived";
        public static final String finished = "finished";
    }

    public static class FSDriverLocation {
        public static final String driverLocationCollection = "driverLocations";
        public static final String currentPositionLatitude = "currentPositionLatitude";
        public static final String currentPositionLongitude = "currentPositionLongitude";
    }

    // All Toast messages being used
    public static class ToastMessage {
        public static final String emptyInputError = "Please fill in your account authentication.";
        public static final String signInSuccess = "Sign in successfully!";
        public static final String signInFailure = "Invalid email/password!";
        public static final String registerSuccess = "Successfully registered";
        public static final String registerFailure = "Authentication failed, email must be unique and has correct form!";
        public static final String retrieveUsersInfoFailure = "Error querying for all users' information!";
        public static final String emptyMessageInputError = "Please type your message to send!";

        // Create site validation message
        public static final String placeAutocompleteError = "Google PlaceAutocomplete error with code: ";

        // Maps Error Handling
        public static final String currentLocationNotUpdatedYet = "Please wait for a few seconds for current location to be updated!";
        public static final String routeRenderingInProgress = "Please wait, the route is being rendered!";

        // Edit site Message
        public static final String editSiteSuccess = "Edit site successfully!";

        // Booking error
        public static final String addNewBookingToDbFail = "Fail to create new booking";
    }

    public static class PlaceAddressComponentTypes {
        public static final String premise = "premise";
        public static final String streetNumber = "street_number";
        public static final String route = "route";
        public static final String adminAreaLv1 = "administrative_area_level_1";
        public static final String adminAreaLv2 = "administrative_area_level_2";
        public static final String country = "country";
    }

    public static class MenuItemsIndex {
        public static final int myCreatedSitesItemIndex = 0;
        public static final int joinSitesItemIndex = 1;
        public static final int createSiteItemIndex = 2;
    }

    public static class GoogleMaps {
        public static class CameraZoomLevel {
            public static final int city = 10;
            public static final int streets = 15;
            public static final int buildings = 20;

            public static final float betweenCityAndStreets = 12.5f;
            public static final float betweenStreetsAndBuildings = 17.5f;
        }

        public static class DirectionApi {
            public static final String baseUrl = "https://maps.googleapis.com/maps/api/directions/";
            public static final String originParam = "origin";
            public static final String destinationParam = "destination";
            public static final String modeParam = "mode";
            public static final String outputParam = "json";
        }
    }

    public static class Notification {
        public static final String CHANNEL_ID = "GAC";
        public static final String CHANNEL_NAME = "GreenAndClean notification";
        public static final String CHANNEL_DES = "GreenAndClean app notification";
        public static final String title = "Green&Clean notification";
        public static final String onSiteChangeTextContent = "There has been some changes made to one of your participating site, click to see...";
    }
}
