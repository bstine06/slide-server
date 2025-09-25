package com.brettstine.slide_server.game;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.brettstine.slide_server.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    private UUID id;
    private User host;
    private boolean inProgress;
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private List<Maze> mazes;

}
