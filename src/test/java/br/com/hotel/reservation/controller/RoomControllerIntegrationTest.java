package br.com.hotel.reservation.controller;

import br.com.hotel.reservation.BaseIntegrationTest;
import br.com.hotel.reservation.dto.HotelRequest;
import br.com.hotel.reservation.dto.HotelResponse;
import br.com.hotel.reservation.dto.RoomRequest;
import br.com.hotel.reservation.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoomControllerIntegrationTest extends BaseIntegrationTest {

    private Long hotelId;

    @BeforeEach
    void setUpHotel() throws Exception {
        final var hotelRequest = new HotelRequest("Hotel Rooms Test", "Florianópolis", "SC", "Descrição", null);
        final var result = mockMvc.perform(post("/api/hotels")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(hotelRequest)))
                .andReturn();
        hotelId = parseResponse(result, HotelResponse.class).id();
    }

    @Test
    void shouldCreateRoomSuccessfully() throws Exception {
        final var request = new RoomRequest(hotelId, "101", RoomType.STANDARD, 2, new BigDecimal("300.00"));

        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("101"))
                .andExpect(jsonPath("$.type").value("STANDARD"));
    }

    @Test
    void shouldReturnRoomById() throws Exception {
        final var request = new RoomRequest(hotelId, "102", RoomType.DELUXE, 3, new BigDecimal("450.00"));
        final var created = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andReturn();
        final var roomId = MAPPER.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("102"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentRoom() throws Exception {
        mockMvc.perform(get("/api/rooms/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteRoomSuccessfully() throws Exception {
        final var request = new RoomRequest(hotelId, "103", RoomType.SUITE, 4, new BigDecimal("800.00"));
        final var created = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andReturn();
        final var roomId = MAPPER.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/rooms/" + roomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentRoom() throws Exception {
        mockMvc.perform(delete("/api/rooms/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAvailableRooms() throws Exception {
        final var roomRequest = new RoomRequest(hotelId, "104", RoomType.STANDARD, 2, new BigDecimal("250.00"));
        mockMvc.perform(post("/api/rooms")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(roomRequest)));

        final var availabilityBody = String.format(
                "{\"hotelId\":%d,\"checkIn\":\"%s\",\"checkOut\":\"%s\"}",
                hotelId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );

        mockMvc.perform(post("/api/rooms/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(availabilityBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnBadRequestWhenAvailabilityDatesInvalid() throws Exception {
        final var availabilityBody = String.format(
                "{\"hotelId\":%d,\"checkIn\":\"%s\",\"checkOut\":\"%s\"}",
                hotelId,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(1)
        );

        mockMvc.perform(post("/api/rooms/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(availabilityBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnForbiddenWithoutToken() throws Exception {
        mockMvc.perform(get("/api/rooms/1"))
                .andExpect(status().isForbidden());
    }
}
