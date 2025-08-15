package com.example.cabbooking.ui.customer.booking.popup_driver_arrived;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cabbooking.model.User;

/**
 * View model for PopupDriverArrivalFragment
 */
public class PopupDriverArrivalViewModel extends ViewModel {
    private MutableLiveData<User> driver;

    public PopupDriverArrivalViewModel() {
        driver = new MutableLiveData<>();
    }

    public void setDriver(User driver) {
        this.driver.setValue(driver);
    }

    public MutableLiveData<User> getDriver() {
        return driver;
    }
}