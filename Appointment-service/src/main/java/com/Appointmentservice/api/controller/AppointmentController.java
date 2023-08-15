package com.Appointmentservice.api.controller;

import com.Appointmentservice.api.entity.Appointment;
import com.Appointmentservice.api.paload.Doctor;
import com.Appointmentservice.api.paload.Patient;
import com.Appointmentservice.api.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final RestTemplate restTemplate;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, RestTemplate restTemplate) {
        this.appointmentService = appointmentService;
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        System.out.println("GetAllAppointements");
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return appointment != null
                ? ResponseEntity.ok(appointment)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        Appointment savedAppointment = null;
        try {
            ResponseEntity<Patient> patientResponse = restTemplate.getForEntity(

                    "http://patient-service/patients" + appointment.getPatientId(),
                    Patient.class);
            System.out.println("appointment" + appointment);
            if (patientResponse.getStatusCode() != HttpStatus.OK || patientResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Patient patient = patientResponse.getBody();

            ResponseEntity<Doctor> doctorResponse = restTemplate.getForEntity(
                    "http://doctor-service/doctors/" + appointment.getDoctorId(),
                    Doctor.class);

            if (doctorResponse.getStatusCode() != HttpStatus.OK || doctorResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Doctor doctor = doctorResponse.getBody();

            savedAppointment = appointmentService.saveAppointment(appointment);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    // Additional methods if needed...
}
