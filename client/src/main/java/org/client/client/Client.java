package org.client.client;

import lombok.Getter;
import lombok.Setter;
import org.client.util.Status;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Client {

    private Long id;
    private String login;
    private String name;
    private String password;
    private Status status;
    private List<Client> blockClients;

    public Client(){
        this.status = Status.OFFLINE;
    }

    public Client(String login, String name, String password){
        this.login = login;
        this.name = name;
        this.password = password;
        this.status = Status.OFFLINE;
        blockClients = new ArrayList<>();
    }

    public Client(Long id, String name) {
        this.id = id;
        this.name = name;
    }


    public String getStatus(){
        return this.status.getStatus();
    }

    @Override
    public String toString() {
        return String.format("Имя: %s", this.name);
    }

    public void setBlockClient(Client client){
        if(client != null){
            blockClients.add(client);
        }
    }

    public void removeFromBlockList(Client client){
        blockClients.remove(client);
    }

    public void setStatus(String status){
        switch (status){
            case "В сети" -> this.status = Status.ONLINE;
            case "Занят" -> this.status = Status.BUSY;
            case "Невидимка" -> this.status = Status.INVISIBLE;
            case "Не в сети" -> this.status = Status.OFFLINE;
        }
    }

}
