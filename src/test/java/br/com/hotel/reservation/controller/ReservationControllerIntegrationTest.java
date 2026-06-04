package br.com.hotel.reservation.controller;

import br.com.hotel.reservation.BaseIntegrationTest;
import br.com.hotel.reservation.dto.HotelRequest;
import br.com.hotel.reservation.dto.HotelResponse;
import br.com.hotel.reservation.dto.ReservationRequest;
import br.com.hotel.reservation.dto.RoomRequest;
import br.com.hotel.reservation.dto.RoomResponse;
import br.com.hotel.reservation.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationControllerIntegrationTest extends BaseIntegrationTest {

    private Long hotelId;
    private Long roomId;

    @BeforeEach
    void setUpData() throws Exception {
        final var hotelRequest = new HotelRequest("Hotel Integração", "Gramado", "RS", "Descrição", null);
        final var hotelResult = mockMvc.perform(post("/api/hotels")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(hotelRequest)))
                .andReturn();
        hotelId = parseResponse(hotelResult, HotelResponse.class).id();

        final var roomRequest = new RoomRequest(hotelId, "201", RoomType.DELUXE, 2, new BigDecimal("500.00"));
        final var roomResult = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(roomRequest)))
                .andReturn();
        roomId = parseResponse(roomResult, RoomResponse.class).id();
    }

    @Test
    void shouldCreateReservationSuccessfully() throws Exception {
        final var request = new ReservationRequest(
                roomId, "Artur", "artur@email.com",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15)
        );

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestName").value("Artur"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(2500.0));
    }

    @Test
    void shouldReturnConflictWhenRoomNotAvailable() throws Exception {
        final var request = new ReservationRequest(
                roomId, "Artur", "artur@email.com",
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(25)
        );
        mockMvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(request)));

        final var conflictRequest = new ReservationRequest(
                roomId, "João", "joao@email.com",
                LocalDate.now().plusDays(22),
                LocalDate.now().plusDays(24)
        );

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(conflictRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldCancelReservation() throws Exception {
        final var request = new ReservationRequest(
                roomId, "Artur", "artur@email.com",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(35)
        );
        final var created = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andReturn();
        final var reservationId = MAPPER.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/reservations/" + reservationId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturnBadRequestWhenCheckOutBeforeCheckIn() throws Exception {
        final var invalidRequest = new ReservationRequest(
                roomId, "Artur", "artur@email.com",
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(10)
        );

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
