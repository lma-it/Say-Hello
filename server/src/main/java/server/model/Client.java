package server.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clientId;

    @Column(name = "login")
    private String login;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "client")
    private List<GlobalChat> globalChats;

    @OneToMany(mappedBy = "client")
    private List<PrivateChat> privateChats;

    public Client() {}

    public Client(String login, String name, String password, String status) {
        this.login = login;
        this.name = name;
        this.password = password;
        this.status = status;
    }


    @Override
    public String toString() {
        return String.format("Логин: %s, Имя: %s, Статус: %s", this.login, this.name, this.status);
    }
}

