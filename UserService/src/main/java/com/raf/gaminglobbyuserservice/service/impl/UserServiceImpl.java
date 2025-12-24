package com.raf.gaminglobbyuserservice.service.impl;

import com.raf.gaminglobbyuserservice.dto.*;
import com.raf.gaminglobbyuserservice.mapper.UserMapper;
import com.raf.gaminglobbyuserservice.model.*;
import com.raf.gaminglobbyuserservice.repository.RoleRepository;
import com.raf.gaminglobbyuserservice.repository.TitleRepository;
import com.raf.gaminglobbyuserservice.repository.VerificationTokenRepository;
import com.raf.gaminglobbyuserservice.security.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.NotFoundException;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.raf.gaminglobbyuserservice.repository.UserRepository;
import com.raf.gaminglobbyuserservice.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Service
public class UserServiceImpl implements UserService {

    private TokenService tokenService;
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private TitleRepository titleRepository;
    private UserMapper userMapper;
    private VerificationTokenRepository verificationTokenRepository;

    public UserServiceImpl(TokenService tokenService, RoleRepository roleRepository, UserRepository userRepository, TitleRepository titleRepository, UserMapper userMapper, VerificationTokenRepository verificationTokenRepository) {
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.titleRepository = titleRepository;
        this.userMapper = userMapper;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::userToUserDto);
    }

    @Override
    public Page<UserStatsDto> findStats(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::userToUserStatsDto);
    }

    @Override
    @Transactional
    public UserDto register(UserCreateDto dto) {

        User user = userMapper.userCreateDtoToUser(dto);
        user.setEnabled(false);

        Role role = roleRepository.findRoleByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Title title = titleRepository.findTitleByName(TitleName.TITULA1)
                .orElseThrow(() -> new RuntimeException("Title not found"));

        user.setRole(role);
        user.getUserStats().setTitle(title);

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        verificationTokenRepository.save(
                new VerificationToken(user, token)
        );


        return userMapper.userToUserDto(user);
    }



    @Override
    public UserDto updateProfileData(String token, UserUpdateDto userUpdateDto) {

        String usernameFromToken = tokenService.extractUsername(token);

        User user = userRepository.findUserByUsername(usernameFromToken)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println(user.toString());
        System.out.println(user.getUserStats().getUser());
        System.out.println(user.getUserStats().getTitle().toString());

        userMapper.updateProfile(user, userUpdateDto);

        userRepository.save(user);

        return userMapper.userToUserDto(user);
    }

    @Override
    public UserDto blockUser(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        user.setBlocked(true);
        userRepository.save(user);

        return userMapper.userToUserDto(user);

    }

    @Override
    public TokenResponseDto login(TokenRequestDto tokenRequestDto) {
        User user = userRepository
                .findUserByUsernameAndPassword(tokenRequestDto.getUsername(), tokenRequestDto.getPassword())
                .orElseThrow(() -> new RuntimeException(String
                        .format("User with username: %s and password: %s not found.", tokenRequestDto.getUsername(),
                                tokenRequestDto.getPassword())));


        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified.");
        }

        Claims claims = Jwts.claims();
        claims.setSubject(user.getUsername());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole().getName().name());

        System.out.println("CLAIMS = " + claims);

        return new TokenResponseDto(tokenService.generate(claims));
    }

    @Override
    public void verifyUser(String token) {
        VerificationToken vt = verificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token!"));

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = vt.getUser();
        user.setEnabled(true);

        userRepository.save(user);
        verificationTokenRepository.delete(vt);
    }

    @Override
    public AuthUserDto getAuthUserInfo(String authorization) {

        String username = tokenService.extractUsername(authorization);

        System.out.println("EXTRACTED USERNAME = " + username);


        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthUserDto dto = new AuthUserDto();
        dto.setUserId(user.getId());
        dto.setRole(user.getRole().getName().name());
        dto.setBlocked(user.getBlocked());

        if (user.getRole().getName() == RoleName.ADMIN) {
            dto.setAttendancePercentage(null);
        } else {
            dto.setAttendancePercentage(Double.valueOf(user.getUserStats().getAttendedPct()));
        }


        return dto;
    }

}
