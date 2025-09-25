package com.brettstine.slide_server.game;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface GameRepository {
    Game save(Game game);

    Optional<Game> findById(UUID id);

    Optional<Game> findByHostUsername(String username);

    void delete(Game game);

    Set<Game> getAllGames();
}

