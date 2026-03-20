package org.example.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String school;
    private Integer gender;
    private String address;
}
