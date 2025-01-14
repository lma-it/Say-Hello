package server.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "private_chat")
public class PrivateChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long privateId;

    @OneToOne(mappedBy = "privateChat")
    private Client client;

    @OneToMany(mappedBy = "privateChat")
    private List<PrivateMessage> messages;

    public PrivateChat() {}

    public PrivateChat(Client client, List<PrivateMessage> messages) {
        this.client = client;
        this.messages = messages;
    }


}

