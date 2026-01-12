package com.raf.gaminglobbyuserservice.repository;

import com.raf.gaminglobbyuserservice.dto.UserDto;
import com.raf.gaminglobbyuserservice.model.User;
import com.raf.gaminglobbyuserservice.model.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsernameAndPassword(String username, String password);
    Optional<User> findUserByUsername(String username);

}
