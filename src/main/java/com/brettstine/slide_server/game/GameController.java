package com.brettstine.slide_server.game;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class GameController {
    
    private final GameService gameService;

    @PostMapping
    public ResponseEntity<ResponseDto<GameDto>> create(@RequestBody GameOptions options, Authentication authentication) {

        String authenticatedUsername = authentication.getName();

        GameDto gameDto = gameService.create(options, authenticatedUsername);

        String message = "Game created with id: " + gameDto.getGameId();
        ResponseDto<GameDto> response = new ResponseDto<GameDto>(message, gameDto);

        return ResponseEntity.ok(response);

    }    

    @DeleteMapping()
    public ResponseEntity<ResponseDto<GameDto>> delete(Authentication authentication) {

        String authenticatedUsername = authentication.getName();

        GameDto gameDto = gameService.deleteByHostUsername(authenticatedUsername);
        
        String message = "Game (id: "+ gameDto.getGameId() +") deleted";
        ResponseDto<GameDto> response = new ResponseDto<GameDto>(message, gameDto);

        return ResponseEntity.ok(response);

    }

    @PatchMapping("/{gameId}")
    public ResponseEntity<ResponseDto<GameDto>> joinGame(@PathVariable("gameId") UUID gameId, Authentication authentication) {

        String authenticatedUsername = authentication.getName();

        GameDto gameDto = gameService.addPlayerToGame(gameId, authenticatedUsername);

        String message = "User '"+authenticatedUsername+"' joined game: " + gameDto.getGameId();
        ResponseDto<GameDto> response = new ResponseDto<GameDto>(message, gameDto);

        return ResponseEntity.ok(response); 

    }

    @PatchMapping()
    public ResponseEntity<ResponseDto<GameDto>> leaveGame(Authentication authentication) {

        String authenticatedUsername = authentication.getName();

        GameDto gameDto = gameService.leaveGame(authenticatedUsername);

        String message = authenticatedUsername + " removed from game id: " + gameDto.getGameId();
        ResponseDto<GameDto> response = new ResponseDto<GameDto>(message, gameDto);

        return ResponseEntity.ok(response); 

    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDto<Set<GameDto>>> getAllGames() {
        return ResponseEntity.ok(new ResponseDto<Set<GameDto>>("All games", gameService.getAllGames()));
    }

    @GetMapping()
    public ResponseEntity<ResponseDto<GameDto>> getCurrentGame(Authentication authentication) {

        String authenticatedUsername = authentication.getName();

        GameDto gameDto = gameService.getCurrentGameForUser(authenticatedUsername);

        if (gameDto == null) {
            String message = "No game found for user: " + authenticatedUsername;
            ResponseDto<GameDto> response = new ResponseDto<GameDto>(message, gameDto);

            return ResponseEntity.ok(response); 
        }

        String message = "Linked game found with id: " + gameDto.getGameId();
        ResponseDto<GameDto> response = new ResponseDto<GameDto>(message, gameDto);

        return ResponseEntity.ok(response); 

    }

    @GetMapping("/{gameId}")
    public ResponseEntity<ResponseDto<GameDto>> getGameById(@PathVariable("gameId") UUID gameId) {

        GameDto gameDto = gameService.getById(gameId);

        String message = "Found game with id: " + gameDto.getGameId();
        ResponseDto<GameDto> response = new ResponseDto<GameDto>(message, gameDto);

        return ResponseEntity.ok(response); 

    }

}
