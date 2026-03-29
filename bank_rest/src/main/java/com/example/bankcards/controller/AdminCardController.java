package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockCardRequest;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.CardServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.PagedModel;


import javax.naming.Binding;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
public class AdminCardController {

    private final CardService cardService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest cardRequest){

        CardResponse response = cardService.createCard(cardRequest);

        URI location = URI.create("/api/admin/cards/" + response.cardId());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PutMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> blockCard(@PathVariable @NotNull Long cardId, @RequestBody @Valid BlockCardRequest request){

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
