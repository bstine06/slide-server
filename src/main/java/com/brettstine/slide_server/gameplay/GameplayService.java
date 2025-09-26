package com.brettstine.slide_server.gameplay;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.brettstine.slide_server.game.Game;
import com.brettstine.slide_server.game.GameDto;
import com.brettstine.slide_server.game.GameRepository;
import com.brettstine.slide_server.game.Player;
import com.brettstine.slide_server.websocket.GameBroadcaster;
import com.brettstine.slide_server.websocket.dto.GameWebSocketMessageTypes;
import com.brettstine.slide_server.websocket.dto.PlayerNameOnlyPayload;
import com.brettstine.slide_server.websocket.dto.PlayerReadyPayload;
import com.brettstine.slide_server.websocket.dto.PlayerUpdatePayload;
import com.brettstine.slide_server.websocket.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameplayService {

    private final GameRepository repository;
    private final GameBroadcaster broadcaster;

    public void sendFullGameStateToPlayer(UUID gameId, String username) {
        Game game = getGame(gameId);
        getPlayer(game, username); // just to see if it throws (player isnt in game)

        broadcaster.sendTo(gameId, username, 
            new WebSocketMessage<GameDto>(GameWebSocketMessageTypes.GAME_STATE, GameDto.convertToDto(game), System.currentTimeMillis())
        );
    }

    public void readyUpPlayer(UUID gameId, String username, PlayerReadyPayload payload) {
        validateUsername(username, payload.getUsername());

        Game game = getGame(gameId);
        Player player = getPlayer(game, username);
        player.setReady(payload.isReady());
        repository.save(game);

        broadcaster.broadcast(gameId,
            new WebSocketMessage<>(GameWebSocketMessageTypes.PLAYER_READY, payload, System.currentTimeMillis())
        );

        // check if everyone is ready and then start the game if so
        attemptStartGame(gameId);
    }

    public void removePlayer(UUID gameId, String username, PlayerNameOnlyPayload payload) {
        validateUsername(username, payload.getUsername());

        Game game = getGame(gameId);
        if (game.getPlayers().remove(username) == null) {
            throw new IllegalArgumentException("Player not found in game");
        }
        repository.save(game);

        broadcaster.broadcast(gameId,
            new WebSocketMessage<>(GameWebSocketMessageTypes.PLAYER_LEAVE, payload, System.currentTimeMillis())
        );
    }

    public void updatePlayer(UUID gameId, String username, PlayerUpdatePayload payload) { 
        validateUsername(username, payload.getUsername());

        Game game = getGame(gameId);
        Player player = getPlayer(game, username);
        player.setLevel(payload.getLevel());
        player.setX(payload.getX());
        player.setY(payload.getY());
        player.setVx(payload.getVx());
        player.setVy(payload.getVy());
        player.setNextMove(payload.getNextMove());
        player.setStopX(payload.getStopX());
        player.setStopY(payload.getStopY());

        repository.save(game);

        broadcaster.broadcastToGameExcept(gameId, username,
            new WebSocketMessage<>(GameWebSocketMessageTypes.PLAYER_UPDATE, payload, System.currentTimeMillis())
        );
    }

    private void attemptStartGame(UUID gameId) {
        Game game = getGame(gameId);
        for (Player player : game.getPlayers().values()) {
            if (!player.isReady()) {
                return; // exits attemptStartGame
            }
        }
        startGame(gameId);
    }

    private void startGame(UUID gameId) {
        Game game = getGame(gameId);
        game.setInProgress(true);
        repository.save(game);
        broadcaster.broadcast(gameId, 
            new WebSocketMessage<>(
                GameWebSocketMessageTypes.GAME_START,
                GameDto.convertToDto(game),
                System.currentTimeMillis()
            ));
    }

    // helpers
    private Game getGame(UUID gameId) { 
        return repository.findById(gameId).orElseThrow(); 
    }

    private Player getPlayer(Game game, String username) {
        return Optional.ofNullable(game.getPlayers().get(username))
                       .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    private void validateUsername(String actual, String fromPayload) {
        if (!actual.equals(fromPayload)) throw new IllegalArgumentException("Username mismatch");
    }

}
