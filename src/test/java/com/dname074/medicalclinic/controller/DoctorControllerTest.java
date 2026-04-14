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
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DoctorControllerTest {

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
        int page = 0;
        int size = 1;

        DoctorDto doctorDto = createDoctor();
        Pageable pageable = PageRequest.of(page, size);
        Page<DoctorDto> doctorsPage = new PageImpl<>(List.of(doctorDto), pageable, 1);
        PageDto<DoctorDto> doctorsPageDto = pageMapper.toDoctorDto(doctorsPage);

        when(service.findAllDoctors(any(Pageable.class), isNull()))
                .thenReturn(doctorsPageDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/doctors")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(1));

        verify(service).findAllDoctors(eq(pageable), isNull());
        verifyNoMoreInteractions(service);
    }

    @Test
    void findAllDoctors_WithSpecialization_PageReturned() throws Exception {
        Specialization specialization = Specialization.DERMATOLOGIST;
        int page = 0;
        int size = 1;

        DoctorDto doctorDto = createDoctor();
        Pageable pageable = PageRequest.of(page, size);
        Page<DoctorDto> doctorsPage = new PageImpl<>(List.of(doctorDto), pageable, 1);
        PageDto<DoctorDto> doctorsPageDto = pageMapper.toDoctorDto(doctorsPage);

        when(service.findAllDoctors(any(Pageable.class), eq(specialization)))
                .thenReturn(doctorsPageDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/doctors")
                        .param("specialization", specialization.name())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(service).findAllDoctors(eq(pageable), eq(specialization));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void findDoctorById_DoctorFound_DoctorReturned() throws Exception {
        Long doctorId = 1L;
        DoctorDto doctorDto = createDoctor();

        when(service.getDoctorDtoById(eq(doctorId), any(Authentication.class)))
                .thenReturn(doctorDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/doctors/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1));

        verify(service).getDoctorDtoById(eq(doctorId), any(Authentication.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void findDoctorById_DoctorNotFound_404Returned() throws Exception {
        Long doctorId = 1L;

        when(service.getDoctorDtoById(eq(doctorId), any(Authentication.class)))
                .thenThrow(new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/doctors/{doctorId}", doctorId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nie znaleziono doktora o podanym id"));

        verify(service).getDoctorDtoById(eq(doctorId), any(Authentication.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addDoctor_DoctorAdded_Returned() throws Exception {
        CreateDoctorCommand cmd = makeCreateDoctorCommand();
        DoctorDto doctorDto = createDoctor();

        when(service.addDoctor(cmd)).thenReturn(doctorDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("email@onet.pl"));

        verify(service).addDoctor(cmd);
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addDoctor_DoctorAlreadyExists_409Returned() throws Exception {
        CreateDoctorCommand cmd = makeCreateDoctorCommand();

        when(service.addDoctor(cmd))
                .thenThrow(new DoctorAlreadyExistsException("Doktor z podanym emailem znajduje się już w bazie"));

        mockMvc.perform(MockMvcRequestBuilders.post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(service).addDoctor(cmd);
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addDoctor_UserAlreadyExists_409Returned() throws Exception {
        CreateDoctorCommand cmd = makeCreateDoctorCommand();

        when(service.addDoctor(cmd))
                .thenThrow(new UserAlreadyExistsException("Ta osoba została już dodana do systemu"));

        mockMvc.perform(MockMvcRequestBuilders.post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(service).addDoctor(cmd);
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addDoctor_InvalidBody_400Returned() throws Exception {
        CreateDoctorCommand invalid =
                new CreateDoctorCommand("email", "Jan", "Kowalski", "123",
                        Specialization.DERMATOLOGIST, "000000000000000000000000000000000000");

        mockMvc.perform(MockMvcRequestBuilders.post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void updateDoctor_DoctorUpdated_Returned() throws Exception {
        Long doctorId = 1L;
        CreateDoctorCommand cmd = makeCreateDoctorCommand();
        DoctorDto dto = createDoctor();

        when(service.updateDoctorById(eq(doctorId), eq(cmd), any(Authentication.class)))
                .thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.put("/doctors/{doctorId}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).updateDoctorById(eq(doctorId), eq(cmd), any(Authentication.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void updateDoctor_NotFound_404Returned() throws Exception {
        Long doctorId = 1L;
        CreateDoctorCommand cmd = makeCreateDoctorCommand();

        when(service.updateDoctorById(eq(doctorId), eq(cmd), any(Authentication.class)))
                .thenThrow(new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));

        mockMvc.perform(MockMvcRequestBuilders.put("/doctors/{doctorId}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());

        verify(service).updateDoctorById(eq(doctorId), eq(cmd), any(Authentication.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDoctor_DoctorDeleted_Returned() throws Exception {
        Long doctorId = 1L;
        DoctorDto dto = createDoctor();

        when(service.deleteDoctorById(doctorId)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.delete("/doctors/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).deleteDoctorById(doctorId);
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDoctor_NotFound_404Returned() throws Exception {
        Long doctorId = 1L;

        when(service.deleteDoctorById(doctorId))
                .thenThrow(new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/doctors/{doctorId}", doctorId))
                .andExpect(status().isNotFound());

        verify(service).deleteDoctorById(doctorId);
        verifyNoMoreInteractions(service);
    }

    private CreateDoctorCommand makeCreateDoctorCommand() {
        return new CreateDoctorCommand(
                "email@onet.pl",
                "Jan",
                "Kowalski",
                "password123",
                Specialization.DERMATOLOGIST,
                "000000000000000000000000000000000000"
        );
    }

    private DoctorDto createDoctor() {
        return new DoctorDto(
                1L,
                "email@onet.pl",
                Specialization.DERMATOLOGIST,
                new UserDto(1L, "Jan", "Kowalski"),
                List.of()
        );
    }
}