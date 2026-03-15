package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.dto.TransactionResponse;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user/cards")
@RequiredArgsConstructor
public class UserController {
    private final CardService cardService;

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllUsersCards(user.getId(), search, pageable));
    }

    @PostMapping("/{cardId}/block")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<CardResponse> blockUserCard(@AuthenticationPrincipal UserPrincipal user,
                                               @PathVariable Long cardId, CardRequest request){
        return ResponseEntity.ok().body(cardService.blockUserCard(user.getId(), cardId, request.blockedReason()));
    }

    @GetMapping("/balance")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<BigDecimal> getTotalBalance(@AuthenticationPrincipal UserPrincipal user){

        return ResponseEntity.ok().body(cardService.getTotalBalance(user.getId()));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<TransactionResponse> transferBetweenCards(@AuthenticationPrincipal UserPrincipal user,
                                                             @Valid @RequestBody TransactionRequest request){

        return ResponseEntity.ok().body(cardService.transferBetweenCards(user.getId(), request));
    }
}
