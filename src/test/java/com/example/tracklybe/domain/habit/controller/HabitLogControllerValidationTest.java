package com.example.tracklybe.domain.habit.controller;

import com.example.tracklybe.domain.habit.service.HabitLogService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HabitLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class HabitLogControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitLogService habitLogService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void toggleToday_returnsBadRequest_whenNoteTooLong() throws Exception {
        String requestBody = """
                {
                  \"completed\": true,
                  \"note\": \"%s\"
                }
                """.formatted("n".repeat(501));

        mockMvc.perform(patch("/api/habits/1/logs/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.detail", containsString("노트는 500자 이하여야 합니다.")));

        verify(habitLogService, never()).toggleToday(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }
}
