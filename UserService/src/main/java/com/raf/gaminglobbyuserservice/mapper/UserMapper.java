package com.raf.gaminglobbyuserservice.mapper;

import com.raf.gaminglobbyuserservice.dto.UserCreateDto;
import com.raf.gaminglobbyuserservice.dto.UserDto;
import com.raf.gaminglobbyuserservice.dto.UserStatsDto;
import com.raf.gaminglobbyuserservice.dto.UserUpdateDto;
import com.raf.gaminglobbyuserservice.model.*;
import com.raf.gaminglobbyuserservice.repository.RoleRepository;
import com.raf.gaminglobbyuserservice.repository.TitleRepository;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto userToUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setBirthDate(user.getDateOfBirth());
        userDto.setBlocked(user.isBlocked());
        userDto.setId(user.getId());

        return  userDto;
    }

    public User userCreateDtoToUser(UserCreateDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDateOfBirth(dto.getBirthDate());
        user.setPassword(dto.getPassword());
        user.setBlocked(false);

        UserStats stats = new UserStats();
        stats.setUser(user);
        stats.setReporteSessions(0);
        stats.setAttendedSessions(0);
        stats.setLeftSessions(0);
        stats.setAttendedPct(100);

        user.setUserStats(stats);

        return user;
    }



    public UserStatsDto userToUserStatsDto(User user) {
        UserStats stats = user.getUserStats();

        UserStatsDto dto = new UserStatsDto();
        dto.setUsername(user.getUsername());

        dto.setReportedSessions(stats.getReporteSessions());
        dto.setAttendedSessions(stats.getAttendedSessions());
        dto.setLeftSessions(stats.getLeftSessions());
        dto.setAttendedPct(stats.getAttendedPct());

        return dto;
    }

    public User updateProfile(User user, UserUpdateDto dto) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDateOfBirth(dto.getBirthDate());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(dto.getPassword());
        }

        return user;
    }

    public UserStatsDto userStatsToUserStatsDto(UserStats userStats) {
        UserStatsDto dto = new UserStatsDto();
        dto.setUsername(userStats.getUser().getUsername());
        dto.setLeftSessions(userStats.getLeftSessions());
        dto.setReportedSessions(userStats.getReporteSessions());;
        dto.setAttendedSessions(userStats.getAttendedSessions());
        dto.setRoleName(userStats.getUser().getRole().getName().name());
        dto.setAttendedPct(userStats.getAttendedPct());

        return dto;
    }

}
