package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest cardRequest){

        CardResponse response = cardService.createCard(cardRequest);

        return ResponseEntity
                .created(URI
                .create("/api/cards/admin" + response.cardId()))
                .body(response);
    }

    @PutMapping("/admin/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> blockCard(@PathVariable @NotNull Long cardId, @RequestBody CardRequest request){

        return ResponseEntity
                .ok()
                .body(cardService.blockCard(cardId, request.blockedReason()));
    }

    @PutMapping("/admin/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> activateCard(@PathVariable @NotNull Long cardId){

        return ResponseEntity
                .ok()
                .body(cardService.activateCard(cardId));
    }

    @DeleteMapping("/admin/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable @NotNull Long cardId){
        cardService.deleteCard(cardId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(@RequestParam(required = false) String search, Pageable pageable){

        return ResponseEntity.ok().body(cardService.getAllCards(search, pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponse>> getMyCards(@AuthenticationPrincipal CustomUserDetails user,
                                         @RequestParam(required = false) String search,
                                         Pageable pageable){
        return ResponseEntity.ok().body(cardService.getAllUsersCards(user.getId(),search,pageable));
    }

    @PostMapping("/my/{cardId}/block")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<CardResponse> blockUserCard(@AuthenticationPrincipal CustomUserDetails user,
                               @PathVariable Long cardId, CardRequest request){
        return ResponseEntity.ok().body(cardService.blockUserCard(user.getId(), cardId, request.blockedReason()));
    }

    @GetMapping("/my/balance")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<BigDecimal> getTotalBalance(@AuthenticationPrincipal CustomUserDetails user){

        return ResponseEntity.ok().body(cardService.getTotalBalance(user.getId()));
    }

    @PostMapping("/my/transfer")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<TransactionResponse> transferBetweenCards(@AuthenticationPrincipal CustomUserDetails user,
                                                             @Valid @RequestBody TransactionRequest request){

        return ResponseEntity.ok().body(cardService.transferBetweenCards(user.getId(), request));
    }
}
