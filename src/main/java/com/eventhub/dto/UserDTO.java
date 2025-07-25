package com.eventhub.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private Boolean isActive;
}