package com.nicolas.schedule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nicolas.schedule.dto.AddScheduleDTO;
import com.nicolas.schedule.dto.RoomDTO;
import com.nicolas.schedule.dto.ScheduleDTO;
import com.nicolas.schedule.dto.UserDTO;
import com.nicolas.schedule.profile.ScheduleProfile;
import com.nicolas.schedule.repository.ScheduleRepository;
import com.nicolas.schedule.utils.GenericResponse;
import com.nicolas.schedule.utils.exception.BadRequestException;
import com.nicolas.schedule.utils.exception.RecordNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ScheduleService {

    protected final String USER_URI = "http://USER/user/";
    protected final String ROOM_URI = "http://ROOM/room/";

    private final ScheduleRepository scheduleRepository;
    private final ScheduleProfile scheduleProfile;
    private final RestTemplate restTemplate;

    public ResponseEntity<GenericResponse<List<ScheduleDTO>>> findAll() {
        var schedules = scheduleRepository.findAll().stream().map(schedule -> {
            var _schedule = scheduleProfile.toScheduleDTO().map(schedule);

            _schedule.setResponsible(getUserDTO(_schedule.getResponsibleId()).getName());
            _schedule.setRoom(getRoomDTO(_schedule.getRoomId()).getName());

            return _schedule;
        }).collect(Collectors.toList());

        if (schedules.isEmpty())
            throw new RecordNotFoundException("No schedule found");

        var response = new GenericResponse<>(true, 200, schedules);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<GenericResponse<ScheduleDTO>> findScheduleById(Long id) {
        var _schedule = scheduleRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("Schedule not found"));

        var schedule = scheduleProfile.toScheduleDTO().map(_schedule);

        schedule.setResponsible(getUserDTO(_schedule.getResponsibleId()).getName());
        schedule.setRoom(getRoomDTO(_schedule.getRoomId()).getName());

        var response = new GenericResponse<>(true, 200, schedule);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<GenericResponse<List<ScheduleDTO>>> findSchedulesByRoom(Long roomId) {
        var scheduleList = scheduleRepository.findByRoomIdOrderByBookingStart(roomId).orElseThrow(() -> new RecordNotFoundException("No schedule found"))
                .stream().map(schedule -> {
                    var _schedule = scheduleProfile.toScheduleDTO().map(schedule);

                    _schedule.setResponsible(getUserDTO(_schedule.getResponsibleId()).getName());
                    _schedule.setRoom(getRoomDTO(_schedule.getRoomId()).getName());

                    return _schedule;
                }).collect(Collectors.toList());;

        var response = new GenericResponse<>(true, 200, scheduleList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<GenericResponse<List<ScheduleDTO>>> findSchedulesByResponsible(Long responsibleId) {
        var scheduleList = scheduleRepository.findByResponsibleIdOrderByBookingStart(responsibleId).orElseThrow(() -> new RecordNotFoundException("No schedule found"))
                .stream().map(schedule -> {
                    var _schedule = scheduleProfile.toScheduleDTO().map(schedule);

                    _schedule.setResponsible(getUserDTO(_schedule.getResponsibleId()).getName());
                    _schedule.setRoom(getRoomDTO(_schedule.getRoomId()).getName());

                    return _schedule;
                }).collect(Collectors.toList());

        var response = new GenericResponse<>(true, 200, scheduleList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<GenericResponse<ScheduleDTO>> addSchedule(AddScheduleDTO model) {
        // TODO: verify that the room is available at that time and place
        // TODO: add how many people will be, then validate using the room size

        try {
            if (model == null)
                throw new BadRequestException("Invalid body");

            // TODO (DONE): For the creation method only, add new method to find by id only active users and rooms, so you cant create an appointment with a deleted user:
            validateUser(model.getResponsibleId());
            validateRoom(model.getRoomId());

            // TODO: Validate if the start and end is 1 hour long and has no minutes:
            validateAppointmentHours(model.getBookingStart(), model.getBookingEnd());

            var _schedule = scheduleProfile.toSchedule().map(model);

            _schedule.setCreatedAt(LocalDateTime.now());
            _schedule.setLastUpdateAt(LocalDateTime.now());

            scheduleRepository.save(_schedule);

            var schedule = scheduleProfile.toScheduleDTO().map(_schedule);

            schedule.setRoom(getRoomDTO(schedule.getRoomId()).getName());
            schedule.setResponsible(getUserDTO(schedule.getResponsibleId()).getName());

            var response = new GenericResponse<>(true, 201, schedule);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(new GenericResponse<>(500, ex.getMessage()), HttpStatus.OK);
        }
    }

    private void validateAppointmentHours(LocalDateTime start, LocalDateTime end) {
        if (start.getMinute() != 0 || end.getMinute() != 0)
            throw new BadRequestException("Appointments must be made on full hours"); // TODO: Check translation

        // Verificando se tem uma hora de duração pela diferença das datas:
        var appointmentDuration = Duration.between(start, end).toSeconds();

        if (appointmentDuration != 3600)
            throw new BadRequestException("Appointments must be one hour long only");

        var startTime = start.toLocalTime();

        // Verificando se a hora está entre o horário comercial: // 2022-11-13T19:00
        if (!startTime.isAfter(LocalTime.of(8, 0)) || !startTime.isBefore(LocalTime.of(18, 0)))
            throw new BadRequestException("Appointments must be done during business hours");
    }

    private void validateUser(Long id) {
        restTemplate.getForObject(USER_URI + "{responsibleId}", GenericResponse.class, id);
    }

    private void validateRoom(Long id) {
        // Em caso de erro (ex 404),
        restTemplate.getForObject(ROOM_URI + "{roomId}", GenericResponse.class, id);
    }

    private UserDTO getUserDTO(Long id) {
        try {
            // Usando o Jackson para conversão:
            var mapper = new ObjectMapper();

            // Chamando a aplicação USER para pegar o objeto pelo Id:
            var userObj = restTemplate.getForObject(USER_URI + "{responsibleId}", GenericResponse.class, id).getData();

            // Convertendo para JSON String:
            String jsonStr = mapper.writeValueAsString(userObj);

            // Convertendo para o DTO:
            return mapper.readValue(jsonStr, UserDTO.class);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    private RoomDTO getRoomDTO(Long id) {
        try {
            var mapper = new ObjectMapper();

            var userObj = restTemplate.getForObject(ROOM_URI + "{roomId}", GenericResponse.class, id).getData();

            String jsonStr = mapper.writeValueAsString(userObj);

            return mapper.readValue(jsonStr, RoomDTO.class);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }
}
