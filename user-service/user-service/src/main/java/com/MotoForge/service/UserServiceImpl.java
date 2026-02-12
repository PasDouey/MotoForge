package com.MotoForge.service;

import com.MotoForge.dto.UserDTO;
import com.MotoForge.model.User;
import com.MotoForge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Keycloak keycloakAdmin;

    @Override
    public User createUser(UserDTO dto) {

    // -------------------------------
    // 1️⃣ Création dans Keycloak
    // -------------------------------
    UserRepresentation kcUser = new UserRepresentation();
    kcUser.setUsername(dto.getEmail());
    kcUser.setFirstName(dto.getFirstName());
    kcUser.setLastName(dto.getLastName());
    kcUser.setEmail(dto.getEmail());
    kcUser.setEnabled(true);

    // Mot de passe par défaut
    CredentialRepresentation passwordCred = new CredentialRepresentation();
    passwordCred.setTemporary(false);
    passwordCred.setType(CredentialRepresentation.PASSWORD);
    passwordCred.setValue("Password123");
    kcUser.setCredentials(List.of(passwordCred));

    // Crée le user dans Keycloak
    keycloakAdmin.realm("MotoForge").users().create(kcUser);

    // -------------------------------
    // 2️⃣ Récupérer l'user créé et attribuer un rôle
    // -------------------------------
    // Récupérer ID de l'utilisateur
    String userId = keycloakAdmin.realm("MotoForge")
            .users()
            .search(dto.getEmail())
            .get(0)
            .getId();

    // Récupérer l'ID du client
    String clientId = keycloakAdmin.realm("MotoForge")
            .clients()
            .findByClientId("user-service")
            .get(0)
            .getId();

    // Récupérer la RoleRepresentation
    RoleRepresentation roleRep = keycloakAdmin.realm("MotoForge")
            .clients()
            .get(clientId)
            .roles()
            .get(dto.getRole())  // dto.getRole() = "ADMIN"/"SELLER"/"BUYER"
            .toRepresentation();

    // Ajouter le rôle au user Keycloak
    keycloakAdmin.realm("MotoForge")
            .users()
            .get(userId)
            .roles()
            .clientLevel(clientId)
            .add(List.of(roleRep));

    // -------------------------------
    // 3️⃣ Création dans PostgreSQL
    // -------------------------------
    User user = User.builder()
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .email(dto.getEmail())
            .role(dto.getRole())
            .build();

    return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User updateUser(UUID id, UserDTO dto) {
        User user = getUserById(id);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = getUserById(id);

        // Supprimer aussi de Keycloak
        keycloakAdmin.realm("MotoForge")
                .users()
                .search(user.getEmail())
                .forEach(u -> keycloakAdmin.realm("MotoForge").users().get(u.getId()).remove());

        userRepository.delete(user);
    }
}
