package com.itmo.chgk.model.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="users")
public class User {

    @Id
    @Column(name="username")
    private String username;
    @Column(name="password")
    private String password;
    @Column(name="token")
    private String token;
    @Column(name="enabled")
    private boolean enabled;

    //   @JsonManagedReference
    //    @JsonIgnore
    @OneToMany(mappedBy="username", fetch = FetchType.EAGER)
    private List <Authority> authorities = new ArrayList<Authority>();

    public void addAuthority (Authority auth){
        authorities.add(auth);
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
