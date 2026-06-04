package br.com.hotel.reservation.controller;

import br.com.hotel.reservation.BaseIntegrationTest;
import br.com.hotel.reservation.dto.HotelRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HotelControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateHotelSuccessfully() throws Exception {
        final var request = new HotelRequest("Hotel Teste", "São Paulo", "SP", "Descrição", null);

        mockMvc.perform(post("/api/hotels")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hotel Teste"))
                .andExpect(jsonPath("$.city").value("São Paulo"));
    }

    @Test
    void shouldReturnAllHotels() throws Exception {
        mockMvc.perform(get("/api/hotels")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldReturnNotFoundForNonExistentHotel() throws Exception {
        mockMvc.perform(get("/api/hotels/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnForbiddenWithoutToken() throws Exception {
        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailValidationWithBlankName() throws Exception {
        final var invalidRequest = new HotelRequest("", "São Paulo", "SP", null, null);

        mockMvc.perform(post("/api/hotels")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
