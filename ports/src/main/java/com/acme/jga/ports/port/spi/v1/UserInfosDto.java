package com.acme.jga.ports.port.spi.v1;

public class UserInfosDto {
    private String uid;
    private String login;
    private String firstName;
    private String lastName;
    private String encryptedPassword;
    private String email;

    public UserInfosDto() {
        // Default constructor
    }

    public UserInfosDto(String uid, String login, String firstName, String lastName, String encryptedPassword, String email) {
        this.uid = uid;
        this.login = login;
        this.firstName = firstName;
        this.lastName = lastName;
        this.encryptedPassword = encryptedPassword;
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String defaultPassword) {
        this.encryptedPassword = defaultPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
