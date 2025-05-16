package com.example.kafedrameetingapp.models;

import java.io.Serializable;

public class Meeting implements Serializable {
    public String id;
    public String topic;
    public String agenda;
    public String date;
    public String time;
    public int protocolNumber;

    public Meeting() {
        // Пустой конструктор для Firebase
    }

    public Meeting(String topic, String agenda, String date, String time, int protocolNumber) {
        this.topic = topic;
        this.agenda = agenda;
        this.date = date;
        this.time = time;
        this.protocolNumber = protocolNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setAgenda(String agenda) {
        this.agenda = agenda;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getProtocolNumber() {
        return protocolNumber;
    }

    public void setProtocolNumber(int protocolNumber) {
        this.protocolNumber = protocolNumber;
    }
}