package com.MotoForge.service;

import com.MotoForge.dto.UserDTO;
import com.MotoForge.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User createUser(UserDTO userDTO);

    List<User> getAllUsers();

    User getUserById(UUID id);

    User updateUser(UUID id, UserDTO userDTO);

    void deleteUser(UUID id);
}
