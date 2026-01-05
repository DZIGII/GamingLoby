package com.raf.gaminglobbygamingservice.messaging;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CancleSessionNotification {

    private Long userId;
    private String type;
    private String content;

}
