package com.raf.gaminglobbygamingservice.service.impl;

import com.raf.gaminglobbygamingservice.client.userservice.UserServiceClient;
import com.raf.gaminglobbygamingservice.client.userservice.dto.UserEligibilityDto;
import com.raf.gaminglobbygamingservice.client.userservice.dto.UserStatsDto;
import com.raf.gaminglobbygamingservice.dto.*;
import com.raf.gaminglobbygamingservice.mapper.GamingMapper;
import com.raf.gaminglobbygamingservice.messaging.CancleSessionNotification;
import com.raf.gaminglobbygamingservice.messaging.Notification;
import com.raf.gaminglobbygamingservice.model.*;
import com.raf.gaminglobbygamingservice.repository.GamingRepository;
import com.raf.gaminglobbygamingservice.repository.InvitationRepository;
import com.raf.gaminglobbygamingservice.repository.SessionParticipantRepository;
import com.raf.gaminglobbygamingservice.repository.SessionRepository;
import com.raf.gaminglobbygamingservice.security.service.TokenService;
import com.raf.gaminglobbygamingservice.service.GamingService;
import com.raf.jms.MessageHelper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GamingServiceImpl implements GamingService {

    private SessionRepository sessionRepository;
    private GamingRepository gamingRepository;
    private GamingMapper gamingMapper;
    private TokenService tokenService;
    private InvitationRepository invitationRepository;
    private MessageHelper messageHelper;
    private JmsTemplate jmsTemplate;
    private SessionParticipantRepository sessionParticipantRepository;
    private UserServiceClient userServiceClient;
    @Value("${destination.notification}")
    private String destinationNotification;




    public GamingServiceImpl(SessionRepository sessionRepository, GamingRepository gamingRepository, GamingMapper gamingMapper, TokenService tokenService, InvitationRepository invitationRepository, JmsTemplate jmsTemplate, SessionParticipantRepository sessionParticipantRepository, MessageHelper messageHelper, UserServiceClient userServiceClient) {
        this.sessionRepository = sessionRepository;
        this.gamingRepository = gamingRepository;
        this.gamingMapper = gamingMapper;
        this.tokenService = tokenService;
        this.invitationRepository = invitationRepository;
        this.jmsTemplate = jmsTemplate;
        this.sessionParticipantRepository = sessionParticipantRepository;
        this.messageHelper = messageHelper;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public void createGame(String token, GameDto game) {

        Game g = gamingRepository.findGameByName(game.getName());
        if (g != null) throw new RuntimeException("Game already exists");

        gamingRepository.save(gamingMapper.GameDtoToGame(game));

    }

    @Override
    public Page<GameDto> getGames(Pageable pageable) {
        return gamingRepository.findAll(pageable)
                .map(gamingMapper::gameToGameDto);
    }

    @Override
    public GameDto updateGame(String token, Long id, GameDto game) {

        Game g = gamingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        g.setName(game.getName());
        g.setDescription(game.getDescription());
        g.setGener(game.getGenre());

        gamingRepository.save(g);

        return gamingMapper.gameToGameDto(g);
    }

    @Override
    public SessionDto createSession(String token, SessionDto sessionDto) {

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        Claims claims = tokenService.parse(jwt);

        String role = claims.get("role", String.class);
        Long userId = claims.get("userId", Long.class);

        System.out.println("====================");
        System.out.println(role);
        System.out.println("==================");
        System.out.println(userId);
        System.out.println("====================");

        if (!role.equals("USER")) {
            throw new RuntimeException("Only USER can create sessions");
        }

        if (sessionDto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Session start time must be in the future");
        }

        UserEligibilityDto eligibility =
                userServiceClient.checkEligibility("Bearer " + jwt, userId);


        if (eligibility.isBlocked()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Blocked users cannot create sessions"
            );
        }

        if (eligibility.getAttendancePercentage() < 90.0) {

            sendRejectedSessionNotification(userId, sessionDto);

            throw new RuntimeException(
                    "Attendance percentage below 90%"
            );
        }


        Session session = gamingMapper.sessionDtoToSession(sessionDto);

        session.setOrganizer(userId);
        session.setStatus(SessionStatus.SCHEDULED);

        Game game = gamingRepository.findById(sessionDto.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));

        session.setGame(game);

        Session saved = sessionRepository.save(session);

        SessionParticipant organizer = new SessionParticipant();
        organizer.setSession(session);
        organizer.setUserId(userId);
        organizer.setRoleInSession(RoleInSession.ORGANIZATION);
        organizer.setAttended(false);
        organizer.setLeftEarly(false);

        sessionParticipantRepository.save(organizer);

        return gamingMapper.sessionToDto(saved);
    }

    @Override
    public Page<SessionDto> serchSession(String authorization, Long gameId, SessionType sessionType, Integer maxPlayers, String description, Boolean joined, Pageable pageable) {

        Specification<Session> spec = Specification.where(null);

        if (gameId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("game").get("id"), gameId));
        }

        if (sessionType != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("sessionType"),  sessionType));
        }

        if (maxPlayers != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("maxPlayers"), maxPlayers));
        }

        if (description != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("description")),
                            "%" + description.toLowerCase() + "%"
                    ));
        }

        Page<Session> page = sessionRepository.findAll(spec, pageable);

        return page.map(gamingMapper::sessionToDto);
    }

    @Override
    public Void sendInvite(String token, InvitationRequestDto invitationRequestDto) {

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        Claims claims = tokenService.parse(jwt);

        Long organizerId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);

        if (!role.equals("USER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        UserStatsDto userStatsDto;
        try {
            userStatsDto = userServiceClient.getUserStats(organizerId, token);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "User service unavailable"
            );
        }

//        if (userStatsDto.()) {
//            throw new ResponseStatusException(
//                    HttpStatus.FORBIDDEN,
//                    "Blocked users cannot send invitations"
//            );
//        }

        if (userStatsDto.getAttendedPct() < 90.0) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Attendance percentage below 90%"
            );
        }

        Session session = sessionRepository.findById(invitationRequestDto.getSessionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"
                ));

        if (!session.getOrganizer().equals(organizerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only session organizer can send invitations"
            );
        }

        if (session.getSessionType() != SessionType.CLOSED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invitations are allowed only for private sessions"
            );
        }

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot invite users to inactive session"
            );
        }

        Invitation invitation = new Invitation();
        invitation.setSession(session);
        invitation.setInvitedUserId(invitationRequestDto.getInvitedUserId());
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setUsed(false);

        invitationRepository.save(invitation);

        Notification event = new Notification(
                invitationRequestDto.getInvitedUserId(),
                "SESSION_INVITE",
                "Join session: http://localhost:8082/games/invitations/accept?token=" + invitation.getToken()
        );

        String json = messageHelper.createTextMessage(event);

        jmsTemplate.send(destinationNotification,
                s -> s.createTextMessage(json)
        );

        return null;
    }


    @Override
    public void acceptInvitation(String authorization, String token) {

        String jwt = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;

        Claims claims = tokenService.parse(jwt);

        Long userId = claims.get("userId", Long.class);
        Boolean blocked = claims.get("blocked", Boolean.class);

        if (blocked != null && blocked) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Blocked users cannot accept invitations"
            );
        }

        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invitation not found"
                ));

        if (invitation.isUsed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invitation already used"
            );
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invitation expired"
            );
        }

        if (!invitation.getInvitedUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This invitation is not for you"
            );
        }

        Session session = invitation.getSession();

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Session is not active"
            );
        }

        SessionParticipant participant = new SessionParticipant();
        participant.setSession(session);
        participant.setUserId(userId);
        participant.setAttended(false);
        participant.setLeftEarly(false);
        participant.setRoleInSession(RoleInSession.PLAYER);

        sessionParticipantRepository.save(participant);

        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }

    @Override
    public Void cancleSession(String token, Long sessionId) {

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        Claims claim = tokenService.parse(jwt);
        Long userId = claim.get("userId", Long.class);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->  new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"
                ));

        if (!session.getOrganizer().equals(userId)) {
            throw new RuntimeException("You are not allowed to cancle this session");
        }

        if (!session.getStatus().equals(SessionStatus.SCHEDULED)) {
            throw new RuntimeException("You cannot cancel this session");
        }

        session.setStatus(SessionStatus.CANCELLED);
        sessionRepository.save(session);

        List<Long> userIds = invitationRepository.findInvitedUserIdsBySessionId(session.getId());

        userIds.forEach( uId -> {
            CancleSessionNotification event = new CancleSessionNotification();
            event.setUserId(uId);
            event.setType("SESSION_CANCELLED");
            event.setContent("Session " + session.getDescription() + " is cancelled");

            String json = messageHelper.createTextMessage(event);

            jmsTemplate.send(destinationNotification, s ->
                    s.createTextMessage(json)
            );

        });


        return null;
    }

    @Override
    public SessionDto joinSession(String token, Long sessionId) {

        String bearerToken = token.startsWith("Bearer ")
                ? token
                : "Bearer " + token;

        String jwt = bearerToken.substring(7);

        Claims claims = tokenService.parse(jwt);
        Long userId = claims.get("userId", Long.class);

        UserEligibilityDto eligibility =
                userServiceClient.checkEligibility(bearerToken, userId);

        if (eligibility.isBlocked()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Blocked users cannot join sessions"
            );
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Session not found"
                        )
                );

        if (session.getSessionType() == SessionType.CLOSED) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This session is private"
            );
        }

        SessionParticipant participant = new SessionParticipant();
        participant.setSession(session);
        participant.setUserId(userId);
        participant.setRoleInSession(RoleInSession.PLAYER);
        participant.setAttended(false);
        participant.setLeftEarly(false);

        sessionParticipantRepository.save(participant);

        userServiceClient.incrementJoined(bearerToken, userId);

        return gamingMapper.sessionToDto(session);
    }


    @Override
    public Void lockSession(String token, Long sessionId) {

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        Claims claims = tokenService.parse(jwt);

        Long userId = claims.get("userId", Long.class);
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (!userId.equals(session.getOrganizer())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        session.setSessionType(SessionType.CLOSED);

        return null;
    }

    @Override
    public Void recordAttendance(String token, Long sessionId, Long userId) {

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        Claims claims = tokenService.parse(jwt);

        Long uId = claims.get("userId", Long.class);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!uId.equals(session.getOrganizer())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        for (SessionParticipant participant : session.getParticipants()) {
            if (participant.getId().equals(userId)) {
                participant.setAttended(true);
                return null;
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    }

    @Override
    @Transactional
    public void finishSession(String token, Long sessionId, List<Long> attendedUserIds) {

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        Claims claims = tokenService.parse(jwt);
        Long organizerId = claims.get("userId", Long.class);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));


        if (!organizerId.equals(session.getOrganizer())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only organizer can finish session");
        }

//        if (LocalDateTime.now().isBefore(session.getStartTime())) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Session has not started yet"
//            );
//        }

        boolean hasAttendedPlayer = false;

        for (SessionParticipant participant : session.getParticipants()) {

            boolean attended = attendedUserIds.contains(participant.getUserId());

            participant.setAttended(attended);
            participant.setLeftEarly(!attended);


            if (attended && !participant.getUserId().equals(organizerId)) {
                hasAttendedPlayer = true;
            }


        }

        if (!hasAttendedPlayer) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Session must have at least one attending player"
            );
        }

        List<Long> allUsers = session.getParticipants()
                .stream()
                .map(SessionParticipant::getUserId)
                .filter(id -> !id.equals(organizerId))
                .collect(Collectors.toList());;

        List<Long> absentUsers = allUsers.stream()
                .filter(id -> !attendedUserIds.contains(id))
                .collect(Collectors.toList());

        SessionFinishStatsDto dto = new SessionFinishStatsDto();
        dto.setSessionId(sessionId);
        dto.setOrganizerId(organizerId);
        dto.setAttendedUserIds(attendedUserIds);
        dto.setAbsentUserIds(absentUsers);

        userServiceClient.notifySessionFinished(token, dto);
        session.setStatus(SessionStatus.COMPLETED);

    }

    @Override
    public List<SessionDto> getMySessions(String token) {

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        Claims claims = tokenService.parse(jwt);
        Long userId = claims.get("userId", Long.class);

        List<Session> sessions = sessionRepository.findByOrganizer(userId);

        return sessions.stream()
                .map(gamingMapper::sessionToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SessionDetailsDto getMySessionDetails(String token, Long sessionId) {

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        Claims claims = tokenService.parse(jwt);
        Long organizerId = claims.get("userId", Long.class);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"
                ));

        if (!session.getOrganizer().equals(organizerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not organizer of this session"
            );
        }

        List<SessionParticipant> participants =
                sessionParticipantRepository.findBySessionId(sessionId);

        List<Long> userIds = participants.stream()
                .map(SessionParticipant::getUserId)
                .collect(Collectors.toList());

        Map<Long, String> usernames =
                userServiceClient.getUsernamesMap(userIds, token);

        List<SessionParticipantViewDto> participantViews =
                participants.stream()
                        .map(p -> new SessionParticipantViewDto(
                                p.getUserId(),
                                usernames.get(p.getUserId()),
                                p.getRoleInSession().toString()
                        ))
                        .collect(Collectors.toList());


        SessionDetailsDto dto = new SessionDetailsDto();
        dto.setId(session.getId());
        dto.setName(session.getName());
        dto.setSessionType(session.getSessionType());
        dto.setStatus(session.getStatus());
        dto.setStartTime(session.getStartTime());
        dto.setParticipants(participantViews);

        return dto;
    }




    private void sendRejectedSessionNotification(Long userId, SessionDto sessionDto) {

        Notification event = new Notification(
                userId,
                "REJECTED_SESSION",
                "Session '" + sessionDto.getDescription()
                        + "' rejected because attendance is below 90%"
        );

        String json = messageHelper.createTextMessage(event);

        jmsTemplate.send(destinationNotification, s ->
                s.createTextMessage(json)
        );
    }



}
