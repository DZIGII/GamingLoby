package com.raf.gaminglobbygamingservice.service.impl;

import com.raf.gaminglobbygamingservice.dto.GameDto;
import com.raf.gaminglobbygamingservice.dto.InvitationRequestDto;
import com.raf.gaminglobbygamingservice.dto.SessionDto;
import com.raf.gaminglobbygamingservice.mapper.GamingMapper;
import com.raf.gaminglobbygamingservice.messaging.InvitationNotificationEvent;
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
import java.util.UUID;

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
    @Value("${destination.notification}")
    private String destinationNotification;



    public GamingServiceImpl(SessionRepository sessionRepository, GamingRepository gamingRepository, GamingMapper gamingMapper, TokenService tokenService, InvitationRepository invitationRepository, JmsTemplate jmsTemplate, SessionParticipantRepository sessionParticipantRepository, MessageHelper messageHelper) {
        this.sessionRepository = sessionRepository;
        this.gamingRepository = gamingRepository;
        this.gamingMapper = gamingMapper;
        this.tokenService = tokenService;
        this.invitationRepository = invitationRepository;
        this.jmsTemplate = jmsTemplate;
        this.sessionParticipantRepository = sessionParticipantRepository;
        this.messageHelper = messageHelper;
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

        if (!role.equals("USER")) {
            throw new RuntimeException("Only USER can create sessions");
        }

        if (sessionDto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Session start time must be in the future");
        }

        Double attpct = claims.get("attpct", Double.class);

        if (attpct < 90.0) {
            throw new RuntimeException("Session attpct must be greater than 90");
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
        Double attpct = claims.get("attpct", Double.class);
        Boolean blocked = claims.get("blocked", Boolean.class);

        if (!role.equals("USER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (blocked != null && blocked) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Blocked users cannot send invitations"
            );
        }

        if (attpct < 90.0) {
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

        InvitationNotificationEvent event = new InvitationNotificationEvent(
                invitationRequestDto.getInvitedUserId(),
                "SESSION_INVITE",
                "Join session: http://localhost:8082/games/invitations/accept?token=" + invitation.getToken()
        );

        String json = messageHelper.createTextMessage(event);

        jmsTemplate.send(destinationNotification, s ->
            s.createTextMessage(json)
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





}
