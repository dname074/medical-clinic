package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.dto.UserDto;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    PatientService service;
    @Autowired
    ObjectMapper objectMapper;

//    @Test
//    void findAll_PatientFound_PageReturned() throws Exception {
//        Pageable pageable = PageRequest.of(0, 1);
//        List<PatientDto> patients = List.of(
//                new PatientDto(1L, "email", "22", "123456789",
//                        LocalDate.of(2000, 1, 2),
//                        new UserDto(1L, "Jan", "Kowalski"),
//                        List.of())
//        );
//        Page<PatientDto> page = new PageImpl(patients, pageable, 1);
//        when(service.findAll(any())).thenReturn(page);
//        mockMvc.perform(MockMvcRequestBuilders.get("/patients"))
//                .andExpect(jsonPath("$.content[0]"));
//        // todo
//    }

    @Test
    void addPatient_DataCorrect_PatientDtoReturned() throws Exception{
        CreatePatientCommand createPatientCommand = new CreatePatientCommand(
                "email", "123", "23",
                "Jan", "Kowalski", "123456789",
                LocalDate.of(2000, 1, 2)
        );
        PatientDto patientDto = new PatientDto(1L, "email", "23", "123456789",
                LocalDate.of(2000, 1, 2),
                new UserDto(1L, "Jan", "Kowalski"),
                List.of());
        when(service.getPatientDtoById(patientDto.id())).thenReturn(patientDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/patients")
                .content(objectMapper.writeValueAsString(createPatientCommand))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email"))
                .andExpect(jsonPath("$.idCardNo").value(23));
        // todo
    }
}
