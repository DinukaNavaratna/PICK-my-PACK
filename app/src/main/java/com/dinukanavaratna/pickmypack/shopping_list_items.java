package com.dinukanavaratna.pickmypack;

public class shopping_list_items {

    private String price, description, status;
    private Integer id, groupId;
    private Double lat, lng;

    public shopping_list_items(Integer id, String price, String description, String status, Integer groupId, Double lat, Double lng) {
        this.id = id;
        this.price = price;
        this.description = description;
        this.status = status;
        this.groupId = groupId;
        this.lat = lat;
        this.lng = lng;
    }

    public Integer getId() {
        return id;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Integer getGroupId() {
        return groupId;
    }

}
