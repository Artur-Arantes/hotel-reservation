package br.com.hotel.reservation;

import br.com.hotel.reservation.dto.LoginRequest;
import br.com.hotel.reservation.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
public abstract class BaseIntegrationTest {

    static HotelReservationDatabaseContainer database = HotelReservationDatabaseContainer.getInstance();

    protected static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;
    protected String token;

    @DynamicPropertySource
    static void dynamicProperties(final DynamicPropertyRegistry registry) {
        database.start();
        registry.add("spring.datasource.url", () -> System.getProperty("DB_URL"));
        registry.add("spring.datasource.username", () -> System.getProperty("DB_USER"));
        registry.add("spring.datasource.password", () -> System.getProperty("DB_PASSWORD"));
        registry.add("secret.key", () -> System.getProperty("SECRET_KEY"));
    }

    @BeforeEach
    void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        this.token = getToken();
    }

    protected String json(final Object obj) throws Exception {
        return MAPPER.writeValueAsString(obj);
    }

    protected <T> T parseResponse(final MvcResult result, final Class<T> type) throws Exception {
        return MAPPER.readValue(result.getResponse().getContentAsString(), type);
    }

    protected String getToken() throws Exception {
        final var loginRequest = new LoginRequest("test@hotel.com", "test123");

        final var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(loginRequest)))
                .andReturn();

        if (loginResult.getResponse().getStatus() == 200) {
            final var body = MAPPER.readTree(loginResult.getResponse().getContentAsString());
            return body.get("accessToken").asText();
        }

        final var registerRequest = new RegisterRequest("Test User", "test@hotel.com", "test123");
        final var registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerRequest)))
                .andReturn();

        final var body = MAPPER.readTree(registerResult.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }
}
