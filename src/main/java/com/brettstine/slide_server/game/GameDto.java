package com.brettstine.slide_server.game;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameDto {

    private UUID gameId;
    private String hostUsername;
    private GamePhase phase;
    private Map<String, Player> players;
    private List<Maze> mazes;

    public static GameDto convertToDto(Game game) { 
        GameDto dto = GameDto.builder()
                            .gameId(game.getId())
                            .hostUsername(game.getHost().getUsername())
                            .phase(game.getPhase())
                            .players(game.getPlayers())
                            .mazes(game.getMazes())
                            .build();
        return dto;
    }

}
