package com.raf.gaminglobbyuserservice.service;

import com.raf.gaminglobbyuserservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Page<UserDto> findAll(Pageable pageable);

    Page<UserStatsDto> findStats(Pageable pageable);

    UserDto register(UserCreateDto userCreateDto);

    UserDto updateProfileData(String token, UserUpdateDto userUpdateDto);

    UserDto blockUser(String username);

    UserDto unblockUser(String username);

    TokenResponseDto login(TokenRequestDto tokenRequestDto);

    void verifyUser(String token);

    Optional<UserDto> getUserByUId(Long id);

    Void activateUser(String token);

    UserEligibilityDto checkEligibility(Long userId);

    void incrementJoinedSessions(Long userId);

    void processFinishedSession(SessionFinishStatsDto dto);

    UserStatsDto getUserStats(Long userId);

    List<UserDto> getUsersByIds(List<Long> userIds);

}
