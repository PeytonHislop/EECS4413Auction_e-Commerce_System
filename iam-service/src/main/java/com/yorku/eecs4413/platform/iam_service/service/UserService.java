package com.yorku.eecs4413.platform.iam_service.service;

import com.yorku.eecs4413.platform.iam_service.dto.AddressDto;
import com.yorku.eecs4413.platform.iam_service.dto.UserProfileResponse;
import com.yorku.eecs4413.platform.iam_service.exception.ResourceNotFoundException;
import com.yorku.eecs4413.platform.iam_service.model.User;
import com.yorku.eecs4413.platform.iam_service.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfileResponse resp = new UserProfileResponse();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setEmail(user.getEmail());
        resp.setRole(user.getRole().name());

        AddressDto a = new AddressDto();
        if (user.getShippingAddress() != null) {
            a.setStreetNumber(user.getShippingAddress().getStreetNumber());
            a.setStreetName(user.getShippingAddress().getStreetName());
            a.setCity(user.getShippingAddress().getCity());
            a.setCountry(user.getShippingAddress().getCountry());
            a.setPostalCode(user.getShippingAddress().getPostalCode());
        }
        resp.setShippingAddress(a);

        return resp;
    }
}