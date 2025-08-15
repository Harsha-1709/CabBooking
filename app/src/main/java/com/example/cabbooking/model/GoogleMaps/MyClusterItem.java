package com.example.cabbooking.model.GoogleMaps;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * ClusterItem class
 */
public class MyClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final BitmapDescriptor iconBitMapDescriptor;

    public MyClusterItem(double lat, double lng, BitmapDescriptor iconBitMapDescriptor) {
        position = new LatLng(lat, lng);
        this.title = "cc";
        this.snippet = "cl";
//        this.title = site.getSiteName();
//        this.snippet = site.getSiteName();;
        this.iconBitMapDescriptor = iconBitMapDescriptor;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public BitmapDescriptor getIconBitMapDescriptor() {
        return iconBitMapDescriptor;
    }


}
