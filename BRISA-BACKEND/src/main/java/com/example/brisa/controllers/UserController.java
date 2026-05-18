package com.example.brisa.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.brisa.dtos.user.ProfilePictureDTO;
import com.example.brisa.dtos.user.UserResponseDTO;
import com.example.brisa.dtos.user.UserSearchResponseDTO;
import com.example.brisa.enums.UserRole;
import com.example.brisa.models.auth.UserModel;
import com.example.brisa.repositories.UserRepository;
import com.example.brisa.services.LogHelper;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Sort;



@RestController
@RequestMapping("api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LogHelper logHelper;

    
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        List<UserModel> users = (search != null && !search.trim().isEmpty())
                ? userRepository.findByLoginContainingIgnoreCase(search.trim())
                : userRepository.findAll(Sort.by(Sort.Direction.ASC, "login"));

        int safeLimit = Math.max(1, Math.min(limit == null ? 20 : limit, 100));

        List<UserResponseDTO> response = users.stream()
                .filter(user -> role == null || user.getRole() == role)
                .limit(safeLimit)
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getLogin(),
                        user.getEmail(),
                        user.getRole(),
                        user.isVerifiedEmail(),
                        user.getGender(),
                        user.getDateOfBirth(),
                        user.isEnabled(),
                        user.getProfilePicture()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
            .map(user -> new UserResponseDTO(
                user.getId(),
                user.getLogin(),
                user.getEmail(),
                user.getRole(),
                user.isVerifiedEmail(),
                user.getGender(),
                user.getDateOfBirth(),
                user.isEnabled(),
                user.getProfilePicture()
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/{username}")
    public ResponseEntity<List<UserSearchResponseDTO>> searchUsersByUsername(@PathVariable String username) {
        List<UserModel> users = userRepository.findByLoginContainingIgnoreCase(username);

        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<UserSearchResponseDTO> response = users.stream()
            .map(user -> new UserSearchResponseDTO(
                user.getId(),
                user.getLogin(),
                user.getProfilePicture()
            ))
            .toList();

        return ResponseEntity.ok(response);
    }


    
 

    @PutMapping("/{id}/profile-pic")
    public ResponseEntity putProfilePicturePath(
            @PathVariable UUID id, 
            @RequestBody ProfilePictureDTO entity,
            HttpServletRequest request) {
        Optional<UserModel> userOptional = userRepository.findById(id);
        if(!userOptional.isPresent()){
            return ResponseEntity.notFound().build();
        }
        UserModel user = userOptional.get();
        user.setProfilePicture(entity.getProfilePicture());
        userRepository.save(user);
        
        try {
            UUID userId = getUserId();
            logHelper.logAsync(com.example.brisa.enums.LogAction.USER_UPDATE,
                "Foto de perfil atualizada: " + user.getLogin(),
                "User", user.getId().toString(), userId, request);
        } catch (Exception e) {
            // Log error but don't fail the request
        }
        
        UserResponseDTO responseDTO = new UserResponseDTO(
            user.getId(),
            user.getProfilePicture()
        );
        return ResponseEntity.ok(responseDTO);
    }
    

    @GetMapping("/search/{query}/exclude/{userId}")
public ResponseEntity<List<UserSearchResponseDTO>> searchUsersByUsernameExcludingCurrent(
        @PathVariable String query,
        @PathVariable UUID userId) {

    List<UserModel> users = userRepository.findByLoginContainingIgnoreCase(query);

    List<UserSearchResponseDTO> response = users.stream()
        .filter(user -> !user.getId().equals(userId)) 
        .map(user -> new UserSearchResponseDTO(
            user.getId(),
            user.getLogin(),
            user.getProfilePicture()
        ))
        .toList();

    if (response.isEmpty()) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(response);
}

    private UUID getUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.example.brisa.models.auth.UserModel) {
                return ((com.example.brisa.models.auth.UserModel) auth.getPrincipal()).getId();
            }
        } catch (Exception e) {
            // Return null if can't get user
        }
        return null;
    }

    
}
