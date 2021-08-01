package fmg.core.types.model;

import java.util.Objects;
import java.util.UUID;

/** User model */
public class User {

    /** Unique user ID. const */
    private UUID id;
    /** User name. May be changed */
    private String name;
    /** User password */
    private String password;
    /** link to image */
    public String imgAvatar;


    public UUID getId() { return id; }
    public void setId(UUID newId) {
        if (this.id != null)
            throw new UnsupportedOperationException("Illegal usage - con not change existed id");

        this.id = Objects.requireNonNull(newId, "Unique ID must be exist");
    }

    public String getName() { return name; }
    public void setName(String name) {
        if ((name == null) || name.isEmpty())
            throw new IllegalArgumentException("Invalid player name. Need not empty.");

        this.name = name;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        if ((password != null) && password.isEmpty())
            password = null;
        this.password = password;
    }

    @Override
    public String toString() {
        return name + "; passw " + (((password!=null) && !password.isEmpty()) ? "is exist" : "not exist");
    }

}
