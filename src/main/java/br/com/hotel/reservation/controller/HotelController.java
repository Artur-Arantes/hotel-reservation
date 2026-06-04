package br.com.hotel.reservation.controller;

import br.com.hotel.reservation.dto.HotelRequest;
import br.com.hotel.reservation.dto.HotelResponse;
import br.com.hotel.reservation.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels", description = "Hotel management")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    @Operation(summary = "List all hotels")
    @ApiResponse(responseCode = "200", description = "Hotels retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public ResponseEntity<Page<HotelResponse>> findAll(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        final var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(hotelService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotel by ID")
    @ApiResponse(responseCode = "200", description = "Hotel found")
    @ApiResponse(responseCode = "404", description = "Hotel not found")
    public ResponseEntity<HotelResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(hotelService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new hotel")
    @ApiResponse(responseCode = "201", description = "Hotel created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<HotelResponse> create(@RequestBody @Valid final HotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update hotel")
    @ApiResponse(responseCode = "200", description = "Hotel updated")
    @ApiResponse(responseCode = "404", description = "Hotel not found")
    public ResponseEntity<HotelResponse> update(@PathVariable final Long id,
                                                @RequestBody @Valid final HotelRequest request) {
        return ResponseEntity.ok(hotelService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete hotel")
    @ApiResponse(responseCode = "204", description = "Hotel deleted")
    @ApiResponse(responseCode = "404", description = "Hotel not found")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
