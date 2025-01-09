package org.client.util;

public enum Status {

    ONLINE("В сети"),
    OFFLINE("Не в сети"),
    INVISIBLE("Невидимка"),
    BUSY("Занят");

    private final String status;

    Status(String status){
        this.status = status;

    }

    public String getStatus() {
        return status;
    }
}
