package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.dto.UserDto;
import com.dname074.medicalclinic.dto.command.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.exception.patient.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.PageMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    PatientService service;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PageMapper pageMapper;

    @Test
    void findAll_PatientFound_PageReturned() throws Exception {
        int page = 0;
        int size = 1;
        Pageable pageable = PageRequest.of(page, size);
        List<PatientDto> patients = List.of(
                new PatientDto(1L, "email@onet.pl", "22", "123456789",
                        LocalDate.of(2000, 1, 2),
                        new UserDto(1L, "Jan", "Kowalski"),
                        List.of())
        );
        Page<PatientDto> patientsPage = new PageImpl<>(patients, pageable, 1);
        when(service.findAll(any())).thenReturn(pageMapper.toPatientDto(patientsPage));
        mockMvc.perform(MockMvcRequestBuilders.get("/patients")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                )
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(1));
        verify(service, times(1)).findAll(pageable);
        verifyNoMoreInteractions(service);
    }

    @Test
    void findPatientById_PatientFound_PatientReturned() throws Exception {
        // given
        Long patientId = 1L;
        PatientDto patientDto = createPatientDto();
        when(service.getPatientDtoById(patientId)).thenReturn(patientDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/patients/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.idCardNo").value("23"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.birthday").value("2000-01-02"));
        verify(service, times(1)).getPatientDtoById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void findPatientById_PatientNotFound_404Returned() throws Exception {
        // given
        Long patientId = 1L;
        when(service.getPatientDtoById(patientId)).thenThrow(new PatientNotFoundException("Nie udało się znaleźć pacjenta o podanym id"));
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/patients/{patientId}", patientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nie udało się znaleźć pacjenta o podanym id"));
        verify(service, times(1)).getPatientDtoById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void findPatientById_ArgumentsNotValid_400Returned() throws Exception {
        String patientId = "g";
        mockMvc.perform(MockMvcRequestBuilders.get("/patients/{patientId}", patientId))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void addPatient_DataCorrect_PatientDtoReturned() throws Exception {
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        PatientDto patientDto = createPatientDto();
        when(service.addPatient(createPatientCommand)).thenReturn(patientDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/patients")
                        .content(objectMapper.writeValueAsString(createPatientCommand))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.idCardNo").value("23"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.birthday").value("2000-01-02"));
        verify(service, times(1)).addPatient(createPatientCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void addPatient_NotValidArguments_400Returned() throws Exception {
        CreatePatientCommand createPatientCommand = new CreatePatientCommand(
                "email", "123", "23",
                "Jan", "Kowalski", "123456789",
                LocalDate.of(2000, 1, 2)
        );
        mockMvc.perform(MockMvcRequestBuilders.post("/patients")
                        .content(objectMapper.writeValueAsString(createPatientCommand))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void addPatient_PatientAlreadyExistsExceptionThrown_409Returned() throws Exception {
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        when(service.addPatient(createPatientCommand)).thenThrow(new PatientAlreadyExistsException("Pacjent o podanym adresie email już istnieje w bazie danych"));
        mockMvc.perform(MockMvcRequestBuilders.post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPatientCommand)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Pacjent o podanym adresie email już istnieje w bazie danych"));
        verify(service, times(1)).addPatient(createPatientCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void addPatient_UserAlreadyExistsExceptionThrown_409Returned() throws Exception {
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        when(service.addPatient(createPatientCommand)).thenThrow(new UserAlreadyExistsException("Ta osoba została już dodana do systemu"));
        mockMvc.perform(MockMvcRequestBuilders.post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPatientCommand)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ta osoba została już dodana do systemu"));
        verify(service, times(1)).addPatient(createPatientCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updatePatientById_PatientFound_PatientUpdatedAndReturned() throws Exception {
        // given
        Long patientId = 1L;
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        PatientDto patientDto = createPatientDto();
        when(service.updatePatientById(patientId, createPatientCommand)).thenReturn(patientDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.put("/patients/1")
                        .content(objectMapper.writeValueAsString(createPatientCommand))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.idCardNo").value("23"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.birthday").value("2000-01-02"));

        verify(service, times(1)).updatePatientById(1L, createPatientCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updatePatientById_PatientNotFoundExceptionThrown_404Returned() throws Exception {
        Long patientId = 1L;
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        when(service.updatePatientById(patientId, createPatientCommand)).thenThrow(new PatientNotFoundException("Nie udało się znaleźć pacjenta o podanym id"));
        mockMvc.perform(MockMvcRequestBuilders.put("/patients/{patientId}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPatientCommand)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nie udało się znaleźć pacjenta o podanym id"));
        verify(service, times(1)).updatePatientById(1L, createPatientCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updatePatientById_NotValidArgumentsPassed_400Returned() throws Exception {
        Long patientId = 1L;
        CreatePatientCommand createPatientCommand = new CreatePatientCommand(
                "email", "123", "23",
                "Jan", "Kowalski", "123456789",
                LocalDate.of(2000, 1, 2)
        );
        mockMvc.perform(MockMvcRequestBuilders.put("/patients/{patientId}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPatientCommand)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void deletePatientById_PatientFound_PatientDeletedAndReturned() throws Exception {
        // given
        Long patientId = 1L;
        PatientDto patientDto = createPatientDto();
        when(service.deletePatientById(patientId)).thenReturn(patientDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/patients/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.idCardNo").value("23"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.birthday").value("2000-01-02"));
        verify(service, times(1)).deletePatientById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deletePatientById_PatientNotFoundExceptionThrown_404Returned() throws Exception {
        Long patientId = 1L;
        when(service.deletePatientById(patientId)).thenThrow(new PatientNotFoundException("Nie udało się znaleźć pacjenta o podanym id"));
        mockMvc.perform(MockMvcRequestBuilders.delete("/patients/{patientId}", patientId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nie udało się znaleźć pacjenta o podanym id"));
        verify(service, times(1)).deletePatientById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deletePatientById_NotValidArgumentPassed_400Returned() throws Exception {
        String patientId = "g";
        mockMvc.perform(MockMvcRequestBuilders.delete("/patients/{patientId}",patientId))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void modifyPasswordById_PatientFound_PatientModifiedAndReturned() throws Exception {
        // given
        Long patientId = 1L;
        PatientDto patientDto = createPatientDto();
        ChangePasswordCommand changePasswordCommand = new ChangePasswordCommand("password");
        when(service.modifyPatientPasswordById(patientId, changePasswordCommand)).thenReturn(patientDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.patch("/patients/1")
                        .content(objectMapper.writeValueAsString(changePasswordCommand))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email@onet.pl"))
                .andExpect(jsonPath("$.idCardNo").value("23"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.birthday").value("2000-01-02"));
        verify(service, times(1)).modifyPatientPasswordById(1L, changePasswordCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void modifyPasswordById_PatientNotFoundExceptionThrown_404Returned() throws Exception {
        Long patientId = 1L;
        ChangePasswordCommand changePasswordCommand = new ChangePasswordCommand("12345678");
        when(service.modifyPatientPasswordById(patientId, changePasswordCommand)).thenThrow(new PatientNotFoundException("Nie udało się znaleźć pacjenta o podanym id"));
        mockMvc.perform(MockMvcRequestBuilders.patch("/patients/{patientId}", patientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordCommand)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nie udało się znaleźć pacjenta o podanym id"));
        verify(service, times(1)).modifyPatientPasswordById(1L, changePasswordCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void modifyPasswordById_NotValidArguments_400Returned() throws Exception {
        Long patientId = 1L;
        ChangePasswordCommand changePasswordCommand = new ChangePasswordCommand("1234");
        mockMvc.perform(MockMvcRequestBuilders.put("/patients/{patientId}", patientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordCommand)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private CreatePatientCommand makeCreatePatientCommand() {
        return new CreatePatientCommand(
                "email@onet.pl", "password123", "23",
                "Jan", "Kowalski", "123456789",
                LocalDate.of(2000, 1, 2)
        );
    }

    private PatientDto createPatientDto() {
        return new PatientDto(1L, "email@onet.pl", "23", "123456789",
                LocalDate.of(2000, 1, 2),
                new UserDto(1L, "Jan", "Kowalski"),
                List.of());
    }
}
