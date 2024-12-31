package ru.max.common.models.login;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class UsersCredentials {
    private HashMap<String, UserCredential> credentials;
}
