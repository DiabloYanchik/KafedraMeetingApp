package com.example.kafedrameetingapp;
//модель заседания
public class Meeting {
    public String topic;
    public String agenda;
    public String date;
    public String time;
    public int protocolNumber;

    public Meeting(String topic, String agenda, String date, String time, int protocolNumber) {
        this.topic = topic;
        this.agenda = agenda;
        this.date = date;
        this.time = time;
        this.protocolNumber = protocolNumber;
    }
}
