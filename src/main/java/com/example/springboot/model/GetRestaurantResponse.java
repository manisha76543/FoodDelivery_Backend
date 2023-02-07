package com.example.springboot.model;

import java.util.List;

public class GetRestaurantResponse {
    private String message;
    private List<Restaurant> allRestaurant;
    private boolean status;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public List<Restaurant> getAllRestaurant() {
        return allRestaurant;
    }

    public void setAllRestaurant(List<Restaurant> allRestaurant) {
        this.allRestaurant = allRestaurant;
    }
}
