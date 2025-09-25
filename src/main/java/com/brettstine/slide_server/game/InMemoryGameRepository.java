package com.brettstine.slide_server.game;

import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

@Repository
public class InMemoryGameRepository implements GameRepository {

    private final Map<UUID, Game> storage = new ConcurrentHashMap<>();

    @Override
    public Game save(Game game) {
        if (game.getId() == null) {
            game.setId(UUID.randomUUID());
        }
        storage.put(game.getId(), game);
        return game;
    }

    @Override
    public Optional<Game> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Game> findByHostUsername(String username) {
        return storage.values().stream()
                .filter(g -> g.getHost().getUsername().equals(username))
                .findFirst();
    }

    @Override
    public void delete(Game game) {
        storage.remove(game.getId());
    }

    @Override
    public Set<Game> getAllGames() {
        return new HashSet<>(storage.values());
    }
}
