package com.ecommerce.user.services;

import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.models.Address;
import com.ecommerce.user.models.User;
import com.ecommerce.user.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> fetchAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public Optional<UserResponse> fetchUser(Long id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse);


    }

    public List<UserResponse> addUser(UserRequest userRequest) {


        User user = new User();
        updateUserFromUserRequest(user,userRequest);
        userRepository.save(user);

        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private void updateUserFromUserRequest(User user,UserRequest userRequest){
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());

        if(userRequest.getAddress()!=null){
            AddressDTO addressDTO = userRequest.getAddress();
            Address address = new Address();
            address.setState(addressDTO.getState());
            address.setCity(addressDTO.getCity());
            address.setStreet(addressDTO.getStreet());
            address.setZipcode(addressDTO.getZipcode());
            address.setCountry(addressDTO.getCountry());
            user.setAddress(address);
        }
    }

    public boolean updateUser(Long id,UserRequest updatedUserRequest) {

        return userRepository.findById(id)
                .map(existingUser -> {
                    updateUserFromUserRequest(existingUser,updatedUserRequest);
                    userRepository.save(existingUser);
                    return true;
                }).orElse(false);
    }

    private UserResponse mapToUserResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setId(String.valueOf(user.getId()));
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setRole(user.getRole());
        // Map address to AddressDTO and set it to userResponse

        if(user.getAddress()!=null){
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setState(user.getAddress().getState());
            addressDTO.setCity(user.getAddress().getCity());
            addressDTO.setStreet(user.getAddress().getStreet());
            addressDTO.setZipcode(user.getAddress().getZipcode());
            userResponse.setAddress(addressDTO);
        }
        return userResponse;
    }

}

//MapStruct should be implemented for mapping between User and UserResponse, and between Address and AddressDTO to avoid boilerplate code in the service layer.