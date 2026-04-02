package com.example.tracklybe.domain.habit.controller;

import com.example.tracklybe.domain.habit.service.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HabitController.class)
@AutoConfigureMockMvc(addFilters = false)
class HabitControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitService habitService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createHabit_returnsBadRequest_whenTitleTooLong() throws Exception {
        String requestBody = """
                {
                  \"title\": \"%s\",
                  \"description\": \"desc\",
                  \"habitFrequency\": \"DAILY\",
                  \"tags\": [\"health\"]
                }
                """.formatted("a".repeat(51));

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.detail", containsString("title")));

        verify(habitService, never()).createHabit(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createHabit_returnsBadRequest_whenTagIsBlank() throws Exception {
        String requestBody = """
                {
                  \"title\": \"Morning Run\",
                  \"description\": \"desc\",
                  \"habitFrequency\": \"DAILY\",
                  \"tags\": [\"  \"]
                }
                """;

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.detail", containsString("태그는 공백일 수 없습니다.")));

        verify(habitService, never()).createHabit(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createHabit_returnsBadRequest_whenHabitFrequencyInvalid() throws Exception {
        String requestBody = """
                {
                  \"title\": \"Morning Run\",
                  \"description\": \"desc\",
                  \"habitFrequency\": \"YEARLY\",
                  \"tags\": [\"health\"]
                }
                """;

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.detail").value("요청 본문 형식이 올바르지 않습니다."));

        verify(habitService, never()).createHabit(org.mockito.ArgumentMatchers.any());
    }
}
