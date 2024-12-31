package ru.max.common.models.login;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCredential {
    private String username;
    private String password;
    private String token;
}
