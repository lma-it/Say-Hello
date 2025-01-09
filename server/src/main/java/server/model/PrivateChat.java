package server.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "private_chat")
public class PrivateChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long privateId;

    @Column(name = "clientId")
    private Long clientId;

    @Column(name = "private_messages")
    private List<String> messages = new ArrayList<>();

    public PrivateChat(){}

    public PrivateChat(Long id, List<String> messages){
        this.clientId = id;
        this.messages = messages;
    }
}
