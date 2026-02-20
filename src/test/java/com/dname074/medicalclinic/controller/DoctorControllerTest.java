package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.UserDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.exception.doctor.DoctorAlreadyExistsException;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        int page = 0;
        int size = 1;
        DoctorDto doctorDto = createDoctor();
        List<DoctorDto> doctors = List.of(doctorDto);
        Pageable pageable = PageRequest.of(page, size);
        Page<DoctorDto> doctorsPage = new PageImpl<>(doctors, pageable, 1);
        PageDto<DoctorDto> doctorsPageDto = pageMapper.toDoctorDto(doctorsPage);
        when(service.findAllDoctors(any())).thenReturn(doctorsPageDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/doctors")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                )
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
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions").isEmpty());
        verify(service,times(1)).getDoctorDtoById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void findDoctorById_DoctorNotFoundExceptionThrown_404Returned() throws Exception {
        // given
        Long doctorId = 1L;
        when(service.getDoctorDtoById(doctorId)).thenThrow(new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/doctors/{doctorId}",doctorId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nie znaleziono doktora o podanym id"));
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
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions").isEmpty());
        verify(service, times(1)).addDoctor(createDoctorCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void addDoctor_DoctorAlreadyExistsExceptionThrown_409Returned() throws Exception {
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        when(service.addDoctor(createDoctorCommand)).thenThrow(new DoctorAlreadyExistsException("Doktor z podanym emailem znajduje się już w bazie"));

        mockMvc.perform(MockMvcRequestBuilders.post("/doctors")
                .content(objectMapper.writeValueAsString(createDoctorCommand))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Doktor z podanym emailem znajduje się już w bazie"));
        verify(service, times(1)).addDoctor(createDoctorCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void addDoctor_UserAlreadyExistsExceptionThrown_409Returned() throws Exception {
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        when(service.addDoctor(createDoctorCommand)).thenThrow(new UserAlreadyExistsException("Ta osoba została już dodana do systemu"));

        mockMvc.perform(MockMvcRequestBuilders.post("/doctors")
                        .content(objectMapper.writeValueAsString(createDoctorCommand))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ta osoba została już dodana do systemu"));
        verify(service, times(1)).addDoctor(createDoctorCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void addDoctor_ArgumentNotValid_400Returned() throws Exception {
        CreateDoctorCommand createDoctorCommand = new CreateDoctorCommand("email", "Jan", "Kowalski", "123", Specialization.DERMATOLOGIST);

        mockMvc.perform(MockMvcRequestBuilders.post("/doctors")
                        .content(objectMapper.writeValueAsString(createDoctorCommand))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
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
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions").isEmpty());
        verify(service, times(1)).updateDoctorById(1L, createDoctorCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updateDoctorById_DoctorNotFoundExceptionThrown_404Returned() throws Exception {
        Long doctorId = 1L;
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        when(service.updateDoctorById(doctorId, createDoctorCommand)).thenThrow(new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
        mockMvc.perform(MockMvcRequestBuilders.put("/doctors/{doctorId}",doctorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDoctorCommand)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nie znaleziono doktora o podanym id"));
        verify(service,times(1)).updateDoctorById(1L, createDoctorCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updateDoctorById_ArgumentsNotValid_400Returned() throws Exception {
        Long doctorId = 1L;
        CreateDoctorCommand createDoctorCommand = new CreateDoctorCommand("email", "Jan", "Kowalski", "123", Specialization.DERMATOLOGIST);

        mockMvc.perform(MockMvcRequestBuilders.put("/doctors/{doctorId}",doctorId)
                        .content(objectMapper.writeValueAsString(createDoctorCommand))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
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
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions").isEmpty());
        verify(service, times(1)).deleteDoctorById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deleteDoctorById_DoctorNotFoundExceptionThrown_404Returned() throws Exception {
        Long doctorId = 1L;
        when(service.deleteDoctorById(doctorId)).thenThrow(new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
        mockMvc.perform(MockMvcRequestBuilders.delete("/doctors/{doctorId}",doctorId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nie znaleziono doktora o podanym id"));
        verify(service,times(1)).deleteDoctorById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deleteDoctorById_ArgumentsNotValid_400Returned() throws Exception {
        String doctorId = "g";
        mockMvc.perform(MockMvcRequestBuilders.delete("/doctors/{doctorId}",doctorId))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private CreateDoctorCommand makeCreateDoctorCommand() {
        return new CreateDoctorCommand("email@onet.pl", "Jan", "Kowalski", "password123", Specialization.DERMATOLOGIST);
    }

    private DoctorDto createDoctor() {
        return new DoctorDto(1L, "email@onet.pl", Specialization.DERMATOLOGIST,
                new UserDto(1L, "Jan", "Kowalski"), List.of());
    }

    // todo: testy walidacji
}
