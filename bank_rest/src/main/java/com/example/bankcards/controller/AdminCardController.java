package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockCardRequest;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.CardServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.PagedModel;


import java.net.URI;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {

    private final CardService cardService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest cardRequest){

        CardResponse response = cardService.createCard(cardRequest);

        return ResponseEntity
                .created(URI
                        .create("/api/cards/admin" + response.cardId()))
                .body(response);
    }

    @PutMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> blockCard(@PathVariable @NotNull Long cardId, @RequestBody BlockCardRequest request){

        return ResponseEntity
                .ok()
                .body(cardService.blockCard(cardId, request.blockReason()));
    }

    @PutMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> activateCard(@PathVariable @NotNull Long cardId){

        return ResponseEntity
                .ok()
                .body(cardService.activateCard(cardId));
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable @NotNull Long cardId){
        cardService.deleteCard(cardId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<CardResponse>> getAllCards(@RequestParam(required = false) String search, Pageable pageable){

        Page<CardResponse> page = cardService.getAllCards(search, pageable);
        return ResponseEntity.ok().body(new PagedModel<>(page));
    }
}
