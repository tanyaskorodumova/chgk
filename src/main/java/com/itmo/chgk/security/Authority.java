package com.itmo.chgk.security;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name="authorities")
public class Authority
        // implements GrantedAuthority
        {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name="username", nullable=true)
    private User username;

    @Column(name="authority")
    private String authority;

    public Authority(String authority) {
        this.authority = authority;
    }
}
