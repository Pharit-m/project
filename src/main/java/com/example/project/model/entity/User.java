package com.example.project.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

@Data
public class User extends AbstractDocument {
    private static final long serialVersionUID = 1L;

    public enum UserLevel {
        SILVER,
        GOLD,
        PLATINUM
    }

    private String name;
    @JsonIgnore
    private String password;
    private String phoneNumber;
    private UserLevel userLevel;
    private Integer salary;
    private String refCode;
    private UserLevel level;

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(final String password) {
        this.password = password;
    }

}
