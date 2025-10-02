package com.brettstine.slide_server.game;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.brettstine.slide_server.user.UserService;
import com.brettstine.slide_server.websocket.GameBroadcaster;
import com.brettstine.slide_server.websocket.dto.GameWebSocketMessageTypes;
import com.brettstine.slide_server.websocket.dto.PlayerNameOnlyPayload;
import com.brettstine.slide_server.websocket.dto.PlayerReadyPayload;
import com.brettstine.slide_server.websocket.dto.WebSocketMessage;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import com.brettstine.slide_server.user.User;
import com.brettstine.slide_server.user.UserProfileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {
    
    private final GameRepository repository;
    private final UserService userService;
    private final UserProfileService userProfileService;
    private final GameBroadcaster gameBroadcaster;

    /*
     * CREATE
     * creates a game, saves a reference for the game to the host's User object
     * and saves the game to repository
     */
    @Transactional
    public GameDto create(GameOptions options, String authenticatedUsername) {
        Game game = new Game();
        User host = userService.findByUsername(authenticatedUsername)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (repository.findByHostUsername(authenticatedUsername).isPresent()) {
           throw new IllegalArgumentException(authenticatedUsername + " already has a game");
        }

        game.setHost(host);
        game.setMazes(options.getMazes());

        //automatically add host as a player -- required
        game.getPlayers().put(authenticatedUsername, createPlayerFromUsername(authenticatedUsername));

        Game savedGame = repository.save(game);

        host.setCurrentGameId(savedGame.getId());
        userService.save(host);

        return GameDto.convertToDto(game);
        
    }

    public GameDto addPlayerToGame(UUID gameId, String authenticatedUsername) {
        Game game = repository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found"));

        if (game.getPlayers().get(authenticatedUsername) != null) {
            throw new IllegalStateException("Player already in game: " + authenticatedUsername);
        }
        Player player = createPlayerFromUsername(authenticatedUsername);
        

        // link joining user to game
        User playerUser = userService.findByUsername(authenticatedUsername)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        playerUser.setCurrentGameId(game.getId());
        userService.save(playerUser);

        // add player to game and save
        game.getPlayers().put(authenticatedUsername, player);
        repository.save(game);

        // broadcast to any active players in that game
        // create message
        WebSocketMessage<PlayerDto> message = new WebSocketMessage<>(
            GameWebSocketMessageTypes.PLAYER_JOIN, 
            PlayerDto.convertToDto(player), 
            System.currentTimeMillis()
        );
        gameBroadcaster.broadcast(gameId, message);

        return GameDto.convertToDto(game);
    }

    private Player createPlayerFromUsername(String username) {
        // get player's custom details from db (color, etc)
        String color = userProfileService.getUserProfile(username).getColor();

        return Player.builder()
                .username(username)
                .color(color)
                .build();
    }

    public GameDto getById(UUID gameId) {
        Game game = repository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found"));
        return GameDto.convertToDto(game);
    }

    public GameDto deleteByHostUsername(String username) {
        Game game = repository.findByHostUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("No game found for host: " + username));

        repository.delete(game);

        for (String playerUsername : game.getPlayers().keySet()) {
            userService.clearCurrentGameIdForUsername(playerUsername);
        }

        // broadcast the game deletion to all users in the game
        gameBroadcaster.closeAllSessionsForGame(game.getId(), 1000, "The game session was deleted.");

        return GameDto.convertToDto(game);
    }

    public Set<GameDto> getAllGames() {
        return repository.getAllGames()
                        .stream()
                        .map(g -> GameDto.convertToDto(g))
                        .collect(Collectors.toSet());
    }

    public GameDto getCurrentGameForUser(String authenticatedUsername) {
        
        User thisUser = userService.findByUsername(authenticatedUsername)
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UUID currentGameId = thisUser.getCurrentGameId();
        
        if (currentGameId == null) return null;

        Game game = repository.findById(currentGameId).orElse(null);

        if (game == null) {
            thisUser.setCurrentGameId(null);
            userService.save(thisUser);
        }

        return GameDto.convertToDto(game);

    }

    public GameDto leaveGame(String authenticatedUsername) {
        Game game = getGameForUsername(authenticatedUsername, false);
        return removePlayerFromGame(game.getId(), authenticatedUsername);
    }

    public GameDto removePlayerFromGame(UUID gameId, String authenticatedUsername) {
        Game game = repository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found"));

        // Clear the user's current game reference
        userService.findByUsername(authenticatedUsername)
                .ifPresent(user -> {
                    user.setCurrentGameId(null);
                    userService.save(user);
        });

        // Atomically remove the player from the map
        Player removedPlayer = game.getPlayers().remove(authenticatedUsername);
        if (removedPlayer == null) {
            throw new EntityNotFoundException("Player not found in the game");
        }

        // If this is the last player in the game, delete the game and return early
        if (game.getPlayers().isEmpty()) {
            repository.delete(game);
            GameDto.convertToDto(game);
        } else {
            repository.save(game);
        }

        // broadcast the change to all users in the game
        gameBroadcaster.broadcast(
                gameId, 
                new WebSocketMessage<PlayerNameOnlyPayload>(
                    GameWebSocketMessageTypes.PLAYER_LEAVE,
                    new PlayerNameOnlyPayload(authenticatedUsername), 
                    System.currentTimeMillis()
                )
        );

        return GameDto.convertToDto(game);
    }

    public GameDto setPlayerReadyForUser(String authenticatedUsername) {
        Game game = getGameForUsername(authenticatedUsername, false);

        // Atomically update the ready status of the player
        game.getPlayers().computeIfPresent(authenticatedUsername, (key, player) -> {
            player.setReady(true);
            return player;
        });

        // broadcast the change to all users in the game
        gameBroadcaster.broadcast(
                game.getId(), 
                new WebSocketMessage<PlayerReadyPayload>(
                    GameWebSocketMessageTypes.PLAYER_READY,
                    new PlayerReadyPayload(
                        authenticatedUsername,
                        true
                    ), 
                    System.currentTimeMillis()
                )
        );

        return GameDto.convertToDto(game);
    }

    private Game getGameForUsername(String username, boolean clearIfMissing) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UUID currentGameId = user.getCurrentGameId();

        if (currentGameId == null) {
            throw new EntityNotFoundException("User has no current game");
        }

        Game game = repository.findById(currentGameId).orElse(null);
        if (game == null && clearIfMissing) {
            user.setCurrentGameId(null);
            userService.save(user);
        } else if (game == null) {
            throw new EntityNotFoundException("User has no current game");
        }

        return game;
    }

    public Boolean confirmPlayerExistsInGame(UUID gameId, String username) {
        Game game = repository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found"));
        Boolean doesPlayerExistInGame = game.getPlayers().containsKey(username);
        if (!doesPlayerExistInGame) {
            throw new EntityNotFoundException("Player not found in game");
        }
        return true;
    }



}
