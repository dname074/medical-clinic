package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.UserDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.service.DoctorService;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class DoctorControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    DoctorService service;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PageMapper pageMapper;

    @Test
    void findAllDoctors_DoctorFound_PageReturned() throws Exception {
        // given
        DoctorDto doctorDto = createDoctor();
        List<DoctorDto> doctors = List.of(doctorDto);
        Pageable pageable = PageRequest.of(0, 1);
        Page<DoctorDto> page = new PageImpl<>(doctors, pageable, 1);
        PageDto<DoctorDto> pageDto = pageMapper.toDoctorDto(page);
        when(service.findAllDoctors(any())).thenReturn(pageDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/doctors?page=0&size=1"))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(1));
        verify(service,times(1)).findAllDoctors(pageable);
        verifyNoMoreInteractions(service);
    }

    @Test
    void findDoctorById_DoctorFound_DoctorReturned() throws Exception {
        // given
        Long doctorId = 1L;
        DoctorDto doctorDto = createDoctor();
        when(service.getDoctorDtoById(doctorId)).thenReturn(doctorDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/doctors/{doctorId}", doctorId))
                .andExpect(jsonPath("$.email").value("email"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions").isEmpty());
        verify(service,times(1)).getDoctorDtoById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void addDoctor_DoctorNotFound_DoctorAddedAndReturned() throws Exception {
        // given
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        DoctorDto doctorDto = createDoctor();
        when(service.addDoctor(createDoctorCommand)).thenReturn(doctorDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/doctors")
                .content(objectMapper.writeValueAsString(createDoctorCommand))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("email"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions").isEmpty());
        verify(service, times(1)).addDoctor(createDoctorCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updateDoctorById_DoctorFound_DoctorUpdatedAndReturned() throws Exception {
        // given
        Long doctorId = 1L;
        DoctorDto doctorDto = createDoctor();
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        when(service.updateDoctorById(doctorId,createDoctorCommand)).thenReturn(doctorDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.put("/doctors/{doctorId}", doctorId)
                        .content(objectMapper.writeValueAsString(createDoctorCommand))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions").isEmpty());
        verify(service, times(1)).updateDoctorById(1L, createDoctorCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deleteDoctorById_DoctorFound_DoctorDeletedAndReturned() throws Exception {
        // given
        Long doctorId = 1L;
        DoctorDto doctorDto = createDoctor();
        when(service.deleteDoctorById(doctorId)).thenReturn(doctorDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/doctors/{doctorId}", doctorId))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions").isEmpty());
        verify(service, times(1)).deleteDoctorById(1L);
        verifyNoMoreInteractions(service);
    }

    private CreateDoctorCommand makeCreateDoctorCommand() {
        return new CreateDoctorCommand("email", "Jan", "Kowalski", "123", Specialization.DERMATOLOGIST);
    }

    private DoctorDto createDoctor() {
        return new DoctorDto(1L, "email", Specialization.DERMATOLOGIST,
                new UserDto(1L, "Jan", "Kowalski"), List.of());
    }

    // todo: dokonczyc negatywne przypadki
}
