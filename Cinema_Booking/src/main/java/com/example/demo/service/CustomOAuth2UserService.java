package com.example.demo.service;

import com.example.demo.enums.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" hoặc "facebook"
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = "";
        String name = "";
        String providerId = "";
        String avatar = "";

        if ("google".equalsIgnoreCase(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            providerId = (String) attributes.get("sub");
            avatar = (String) attributes.get("picture");
        } else if ("facebook".equalsIgnoreCase(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            providerId = (String) attributes.get("id");
            if (attributes.containsKey("picture")) {
                Object pictureObj = attributes.get("picture");
                if (pictureObj instanceof Map) {
                    Map<?, ?> pictureMap = (Map<?, ?>) pictureObj;
                    if (pictureMap.containsKey("data")) {
                        Map<?, ?> dataMap = (Map<?, ?>) pictureMap.get("data");
                        avatar = (String) dataMap.get("url");
                    }
                }
            }
        }

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email không được cung cấp từ " + registrationId);
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() == null || "LOCAL".equals(user.getProvider())) {
                user.setProvider(registrationId.toUpperCase());
                user.setProviderId(providerId);
                userRepository.save(user);
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0] + "_" + providerId.substring(0, 4));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setFullName(name);
            user.setAvatarUrl(avatar);
            user.setRole(Role.CUSTOMER);
            user.setEnabled(true);
            user.setProvider(registrationId.toUpperCase());
            user.setProviderId(providerId);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        Map<String, Object> newAttributes = new java.util.HashMap<>(attributes);
        newAttributes.put("cinema_username", user.getUsername());

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                newAttributes,
                "cinema_username"
        );
    }
}
