package com.dinukanavaratna.pickmypack;

public class shopping_groups_lists {

    private int id;
    private String topic, description, date, status;

    public shopping_groups_lists(int id, String topic, String description, String date, String status) {
        this.id = id;
        this.topic = topic;
        this.description = description;
        this.date = date;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }
}
