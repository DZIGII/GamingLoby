package com.raf.gaminglobbyuserservice.service;

import com.raf.gaminglobbyuserservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserDto> findAll(Pageable pageable);

    Page<UserStatsDto> findStats(Pageable pageable);

    UserDto register(UserCreateDto userCreateDto);

    UserDto updateProfileData(String token, UserUpdateDto userUpdateDto);

    UserDto blockUser(String username);

    TokenResponseDto login(TokenRequestDto tokenRequestDto);

    void verifyUser(String token);

}
