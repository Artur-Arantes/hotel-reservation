package br.com.hotel.reservation.controller;

import br.com.hotel.reservation.dto.AvailabilityRequest;
import br.com.hotel.reservation.dto.RoomRequest;
import br.com.hotel.reservation.dto.RoomResponse;
import br.com.hotel.reservation.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Room management")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID")
    @ApiResponse(responseCode = "200", description = "Room found")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<RoomResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(roomService.findById(id));
    }

    @PostMapping("/availability")
    @Operation(summary = "Check room availability")
    @ApiResponse(responseCode = "200", description = "Available rooms retrieved")
    public ResponseEntity<List<RoomResponse>> findAvailableRooms(@RequestBody @Valid final AvailabilityRequest request) {
        return ResponseEntity.ok(roomService.findAvailableRooms(request));
    }

    @PostMapping
    @Operation(summary = "Create a room")
    @ApiResponse(responseCode = "201", description = "Room created")
    @ApiResponse(responseCode = "404", description = "Hotel not found")
    public ResponseEntity<RoomResponse> create(@RequestBody @Valid final RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete room")
    @ApiResponse(responseCode = "204", description = "Room deleted")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
