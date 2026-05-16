package com.stock.analysis.controller;

import com.stock.analysis.model.entity.WatchlistItem;
import com.stock.analysis.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自选股Controller
 */
@RestController
@RequestMapping("/api/watchlist")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WatchlistController {
    
    private final WatchlistRepository repository;
    
    @GetMapping
    public ResponseEntity<List<WatchlistItem>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }
    
    @PostMapping
    public ResponseEntity<WatchlistItem> add(@RequestBody WatchlistItem item) {
        return ResponseEntity.ok(repository.save(item));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
