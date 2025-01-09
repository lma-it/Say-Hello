package org.client.client;

import lombok.Getter;
import lombok.Setter;
import org.client.util.Status;

@Getter
@Setter
public class Client {

    private Long id;
    private String login;
    private String name;
    private String password;
    private Status status;

    public Client(){
        this.status = Status.OFFLINE;
    }

    public Client(String login, String name, String password){
        this.login = login;
        this.name = name;
        this.password = password;
        this.status = Status.OFFLINE;
    }


    public String getStatus(){
        return this.status.getStatus();
    }
}
