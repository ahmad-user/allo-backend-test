package com.allobank.splitbill;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Full workflow: Create group -> Add expense -> Add 2nd expense -> Get settlement summary")
    void testFullWorkflow() throws Exception {
        // 1. Create Group
        Map<String, Object> createGroupReq = Map.of(
                "name", "Trip Bandung",
                "participants", List.of(
                        Map.of("name", "Alice"),
                        Map.of("name", "Bob"),
                        Map.of("name", "Charlie")
                )
        );

        MvcResult groupResult = mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Trip Bandung")))
                .andExpect(jsonPath("$.participants", hasSize(3)))
                .andReturn();

        String groupResponseBody = groupResult.getResponse().getContentAsString();
        Long groupId = objectMapper.readTree(groupResponseBody).get("id").asLong();
        Long aliceId = objectMapper.readTree(groupResponseBody).get("participants").get(0).get("id").asLong();
        Long bobId = objectMapper.readTree(groupResponseBody).get("participants").get(1).get("id").asLong();
        Long charlieId = objectMapper.readTree(groupResponseBody).get("participants").get(2).get("id").asLong();

        // 2. Add Expense 1: Alice pays 300000 for Alice, Bob, Charlie
        Map<String, Object> addExpenseReq1 = Map.of(
                "description", "Dinner",
                "amount", 300000.00,
                "paidByParticipantId", aliceId,
                "participantIds", List.of(aliceId, bobId, charlieId)
        );

        mockMvc.perform(post("/api/groups/" + groupId + "/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addExpenseReq1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.amount", is(300000.00)));

        // 3. Add Expense 2: Bob pays 90000 for Bob & Charlie
        Map<String, Object> addExpenseReq2 = Map.of(
                "description", "Taxi",
                "amount", 90000.00,
                "paidByParticipantId", bobId,
                "participantIds", List.of(bobId, charlieId)
        );

        mockMvc.perform(post("/api/groups/" + groupId + "/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addExpenseReq2)))
                .andExpect(status().isCreated());

        // 4. Get Expenses list
        mockMvc.perform(get("/api/groups/" + groupId + "/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is("Dinner")))
                .andExpect(jsonPath("$[0].amount", is(300000.00)))
                .andExpect(jsonPath("$[1].description", is("Taxi")))
                .andExpect(jsonPath("$[1].amount", is(90000.00)));

        // 5. Get Settlement Summary
        // Total expenses = 390000.00
        // Service charge for 'johndoe47' = 0% -> 0.00
        // Alice paid 300000, share 100000 -> net +200000
        // Bob paid 90000, share 145000 -> net -55000
        // Charlie paid 0, share 145000 -> net -145000
        // Settlements: Bob -> Alice 55000, Charlie -> Alice 145000
        mockMvc.perform(get("/api/groups/" + groupId + "/settlement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.group_id", is(groupId.intValue())))
                .andExpect(jsonPath("$.group_name", is("Trip Bandung")))
                .andExpect(jsonPath("$.total_expenses", is(390000.00)))
                .andExpect(jsonPath("$.service_charge_pct", is(0)))
                .andExpect(jsonPath("$.service_charge_amount", is(0.00)))
                .andExpect(jsonPath("$.settlements", hasSize(2)))
                .andExpect(jsonPath("$.settlements[0].from_participant_id", is(charlieId.intValue())))
                .andExpect(jsonPath("$.settlements[0].to_participant_id", is(aliceId.intValue())))
                .andExpect(jsonPath("$.settlements[0].amount", is(145000.00)))
                .andExpect(jsonPath("$.settlements[1].from_participant_id", is(bobId.intValue())))
                .andExpect(jsonPath("$.settlements[1].to_participant_id", is(aliceId.intValue())))
                .andExpect(jsonPath("$.settlements[1].amount", is(55000.00)));
    }

    @Test
    @DisplayName("Validation: Reject duplicate participant names in group creation")
    void testDuplicateParticipantNames() throws Exception {
        Map<String, Object> req = Map.of(
                "name", "Invalid Group",
                "participants", List.of(
                        Map.of("name", "Alice"),
                        Map.of("name", "alice")
                )
        );

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("Validation: Add expense with non-existent group returns 404")
    void testExpenseGroupNotFound() throws Exception {
        Map<String, Object> req = Map.of(
                "description", "Lunch",
                "amount", 50000.00,
                "paidByParticipantId", 999,
                "participantIds", List.of(999L)
        );

        mockMvc.perform(post("/api/groups/999/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }
}
