package com.raf.gaminglobbyuserservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raf.gaminglobbyuserservice.dto.*;
import com.raf.gaminglobbyuserservice.mapper.UserMapper;
import com.raf.gaminglobbyuserservice.model.*;
import com.raf.gaminglobbyuserservice.repository.*;
import com.raf.gaminglobbyuserservice.security.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.raf.gaminglobbyuserservice.service.UserService;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Service
public class UserServiceImpl implements UserService {

    private TokenService tokenService;
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private TitleRepository titleRepository;
    private UserMapper userMapper;
    private VerificationTokenRepository verificationTokenRepository;
    private JmsTemplate jmsTemplate;
    private UserStatsRepository userStatsRepository;

    public UserServiceImpl(TokenService tokenService, RoleRepository roleRepository, UserRepository userRepository, TitleRepository titleRepository, UserMapper userMapper, VerificationTokenRepository verificationTokenRepository, JmsTemplate jmsTemplate,  UserStatsRepository userStatsRepository) {
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.titleRepository = titleRepository;
        this.userMapper = userMapper;
        this.verificationTokenRepository = verificationTokenRepository;
        this.jmsTemplate = jmsTemplate;
        this.userStatsRepository = userStatsRepository;
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


        NotificationEventDto event = new NotificationEventDto();
        event.setUserId(user.getId());
        event.setType("USER_ACTIVATION");
        event.setContent(token);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(event);

            jmsTemplate.convertAndSend("notification.queue", json);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize notification event", e);
        }


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
    public UserDto unblockUser(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        user.setBlocked(false);
        userRepository.save(user);

        return userMapper.userToUserDto(user);
    }


    @Override
    public TokenResponseDto login(TokenRequestDto tokenRequestDto) {

        User user = userRepository
                .findUserByUsernameAndPassword(
                        tokenRequestDto.getUsername(),
                        tokenRequestDto.getPassword()
                )
                .orElseThrow(() -> new RuntimeException(
                        "User not found"
                ));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified.");
        }

        Claims claims = Jwts.claims();
        claims.setSubject(user.getUsername());
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole().getName().name());

        return new TokenResponseDto(
                tokenService.generate(claims)
        );
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
    public Optional<UserDto> getUserByUId(Long id) {
        return userRepository.findById(id)
                .map(userMapper::userToUserDto);
    }

    @Override
    @Transactional
    public Void activateUser(String token) {

        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() -> new RuntimeException("Invalid activation token"));

        User user = verificationToken.getUser();

        if (user.isEnabled()) {
            throw new RuntimeException("Account already activated");
        }

        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        return null;
    }

    @Override
    public UserEligibilityDto checkEligibility(Long userId) {
        System.out.println("-------");
        System.out.println(userId);
        System.out.println("--------------");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("-------");
        System.out.println(user.toString());
        System.out.println("--------------");

        UserEligibilityDto dto = new UserEligibilityDto();
        dto.setBlocked(user.isBlocked());
        dto.setAttendancePercentage(user.getUserStats().getAttendedPct());

        return dto;
    }

    @Override
    @Transactional
    public void incrementJoinedSessions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserStats stats = user.getUserStats();
        stats.setReporteSessions(stats.getReporteSessions() + 1);
    }

    @Override
    @Transactional
    public void processFinishedSession(SessionFinishStatsDto dto) {

        for (Long userId : dto.getAttendedUserIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserStats stats = user.getUserStats();
            stats.setAttendedSessions(stats.getAttendedSessions() + 1);

            recalculateAttendance(stats);
        }

        for (Long userId : dto.getAbsentUserIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserStats stats = user.getUserStats();
            stats.setLeftSessions(stats.getLeftSessions() + 1);

            recalculateAttendance(stats);
        }

        User organizer = userRepository.findById(dto.getOrganizerId())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        UserStats orgStats = organizer.getUserStats();

        boolean successfulSession =
                dto.getAttendedUserIds() != null &&
                        !dto.getAttendedUserIds().isEmpty();

        if (successfulSession) {
            orgStats.setReporteSessions(
                    orgStats.getReporteSessions() + 1
            );
            updateTitleIfNeeded(orgStats);
        } else {
            orgStats.setReporteSessions(
                    Math.max(0, orgStats.getReporteSessions() - 1)
            );
        }
    }

    @Override
    public UserStatsDto getUserStats(Long userId) {
        UserStats dto = userStatsRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Cant find user stats"));

        return userMapper.userStatsToUserStatsDto(dto);
    }

    @Override
    public List<UserDto> getUsersByIds(List<Long> userIds) {

        List<User> users = userRepository.findAllById(userIds);

        return users.stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }



    private void recalculateAttendance(UserStats stats) {

        int total =
                stats.getAttendedSessions() + stats.getLeftSessions();

        if (total == 0) {
            stats.setAttendedPct(100.0);
            return;
        }

        double pct =
                (stats.getAttendedSessions() * 100.0) / total;

        stats.setAttendedPct(pct);
    }

    private void updateTitleIfNeeded(UserStats stats) {

        int success = stats.getReporteSessions();
        Title newTitle;

        if (success >= 100) {
            newTitle = titleRepository.findTitleByName(TitleName.TITULA5).orElseThrow();
        } else if (success >= 50) {
            newTitle = titleRepository.findTitleByName(TitleName.TITULA4).orElseThrow();
        } else if (success >= 25) {
            newTitle = titleRepository.findTitleByName(TitleName.TITULA3).orElseThrow();
        } else if (success >= 10) {
            newTitle = titleRepository.findTitleByName(TitleName.TITULA2).orElseThrow();
        } else {
            return;
        }

        stats.setTitle(newTitle);
    }




}
