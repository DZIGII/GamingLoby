package com.raf.gaminglobbyuserservice.repository;

import com.raf.gaminglobbyuserservice.model.Title;
import com.raf.gaminglobbyuserservice.model.TitleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TitleRepository extends JpaRepository<Title,Long> {
    Optional<Title> findTitleByName(TitleName titleName);
}
