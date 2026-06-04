package br.com.hotel.reservation.controller;

import br.com.hotel.reservation.dto.ReservationRequest;
import br.com.hotel.reservation.dto.ReservationResponse;
import br.com.hotel.reservation.service.ReservationService;
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

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Reservation management")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "List all reservations")
    @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully")
    public ResponseEntity<Page<ReservationResponse>> findAll(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        final var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(reservationService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    @ApiResponse(responseCode = "200", description = "Reservation found")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @GetMapping("/guest")
    @Operation(summary = "Get reservations by guest email")
    @ApiResponse(responseCode = "200", description = "Reservations found")
    public ResponseEntity<List<ReservationResponse>> findByGuestEmail(@RequestParam final String email) {
        return ResponseEntity.ok(reservationService.findByGuestEmail(email));
    }

    @PostMapping
    @Operation(summary = "Create a reservation")
    @ApiResponse(responseCode = "201", description = "Reservation created")
    @ApiResponse(responseCode = "400", description = "Invalid dates")
    @ApiResponse(responseCode = "409", description = "Room not available for the selected period")
    public ResponseEntity<ReservationResponse> create(@RequestBody @Valid final ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(request));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation")
    @ApiResponse(responseCode = "200", description = "Reservation cancelled")
    @ApiResponse(responseCode = "400", description = "Reservation already cancelled")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable final Long id) {
        return ResponseEntity.ok(reservationService.cancel(id));
    }
}
