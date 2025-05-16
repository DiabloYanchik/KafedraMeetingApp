//package com.example.kafedrameetingapp.models;
//
//import java.io.Serializable;
//
//public class Archive implements Serializable {
//    public String id;
//    public String topic;
//    public String agenda;
//    public String date;
//    public String time;
//    public int protocolNumber;
//    public String archivedDate; // Дата архивации
//    public String roomNumber; // Новое поле для номера кабинета
//
//    public Archive() {
//        // Пустой конструктор для Firebase
//    }
//
//    public Archive(String topic, String agenda, String date, String time, int protocolNumber, String archivedDate, String roomNumber) {
//        this.topic = topic;
//        this.agenda = agenda;
//        this.date = date;
//        this.time = time;
//        this.protocolNumber = protocolNumber;
//        this.archivedDate = archivedDate;
//        this.roomNumber = roomNumber;
//    }
//
//    // Конструктор для преобразования из Meeting
//    public Archive(Meeting meeting, String archivedDate) {
//        this.id = meeting.getId();
//        this.topic = meeting.getTopic();
//        this.agenda = meeting.getAgenda();
//        this.date = meeting.getDate();
//        this.time = meeting.getTime();
//        this.protocolNumber = meeting.getProtocolNumber();
//        this.archivedDate = archivedDate;
//    }
//
//    // Геттеры и сеттеры
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getTopic() {
//        return topic;
//    }
//
//    public void setTopic(String topic) {
//        this.topic = topic;
//    }
//
//    public String getAgenda() {
//        return agenda;
//    }
//
//    public void setAgenda(String agenda) {
//        this.agenda = agenda;
//    }
//
//    public String getDate() {
//        return date;
//    }
//
//    public void setDate(String date) {
//        this.date = date;
//    }
//
//    public String getTime() {
//        return time;
//    }
//
//    public void setTime(String time) {
//        this.time = time;
//    }
//
//    public int getProtocolNumber() {
//        return protocolNumber;
//    }
//
//    public void setProtocolNumber(int protocolNumber) {
//        this.protocolNumber = protocolNumber;
//    }
//
//    public String getArchivedDate() {
//        return archivedDate;
//    }
//
//    public void setArchivedDate(String archivedDate) {
//        this.archivedDate = archivedDate;
//    }
//
//    public String getRoomNumber() {
//        return roomNumber;
//    }
//
//    public void setRoomNumber(String roomNumber) {
//        this.roomNumber = roomNumber;
//    }
//
//    // Метод для преобразования обратно в Meeting
//    public Meeting toMeeting() {
//        return new Meeting(topic, agenda, date, time, protocolNumber, roomNumber);
//    }
//}