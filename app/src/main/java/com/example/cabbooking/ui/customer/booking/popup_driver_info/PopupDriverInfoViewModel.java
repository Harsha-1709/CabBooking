package com.example.cabbooking.ui.customer.booking.popup_driver_info;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cabbooking.model.User;

public class PopupDriverInfoViewModel extends ViewModel {
    private MutableLiveData<User> driver;

    public PopupDriverInfoViewModel() {
        driver = new MutableLiveData<>();
    }

    public void setDriver(User driver) {
        this.driver.setValue(driver);
    }

    public MutableLiveData<User> getDriver() {
        return driver;
    }
}