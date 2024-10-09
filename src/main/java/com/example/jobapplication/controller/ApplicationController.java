package com.example.jobapplication.controller;



import com.example.jobapplication.model.Application;
import com.example.jobapplication.model.Interview;
import com.example.jobapplication.model.JobOffer;
import com.example.jobapplication.repository.ApplicationRepository;
import com.example.jobapplication.repository.InterviewRepository;
import com.example.jobapplication.repository.JobOfferRepository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private InterviewRepository interviewRepository;
    @Autowired
    private JobOfferRepository jobOfferRepository;

    @PostMapping("/submit")
    public ResponseEntity<Object> submitApplication(@RequestBody Application application) {
        applicationRepository.save(application);
        return ResponseEntity.ok(createResponse(application.getApplicationId(), application.getApplicantName(), application.getResume(), 1));
    }

    @PostMapping("/{id}/schedule-interview")
    public ResponseEntity<Object> scheduleInterview(@PathVariable Long id, @RequestBody Interview interview) {
        Application application = applicationRepository.findById(id).orElse(null);
        if (application == null) {
            return ResponseEntity.badRequest().body("Invalid application.");
        }
        if (application.getStage() != Application.ApplicationStage.STAGE_1) {
            return ResponseEntity.badRequest().body("Cannot schedule interview unless Stage 1 is complete.");
        }

        interview.setApplicationId(id);
        interviewRepository.save(interview);
        application.setStage(Application.ApplicationStage.STAGE_2);  // Move to Stage 2
        applicationRepository.save(application);

        return ResponseEntity.ok(createResponse(id, application.getApplicantName(), application.getResume(), 2, interview.getInterviewDate()));
    }

    @PostMapping("/{id}/offer-job")
    public ResponseEntity<Object> offerJob(@PathVariable Long id, @RequestBody JobOffer jobOffer) {
        Application application = applicationRepository.findById(id).orElse(null);
        if (application == null) {
            return ResponseEntity.badRequest().body("Invalid application.");
        }
        if (application.getStage() != Application.ApplicationStage.STAGE_2) {
            return ResponseEntity.badRequest().body("Cannot offer job unless Stage 2 is complete.");
        }

        jobOffer.setApplicationId(id);
        jobOfferRepository.save(jobOffer);
        application.setStage(Application.ApplicationStage.STAGE_3);  // Move to Stage 3
        applicationRepository.save(application);

        return ResponseEntity.ok(createResponse(id, application.getApplicantName(), application.getResume(), 3, jobOffer.getOfferDetails()));
    }

    @PostMapping("/{id}/accept-offer")
    public ResponseEntity<Object> acceptOffer(@PathVariable Long id) {
        Application application = applicationRepository.findById(id).orElse(null);
        if (application == null) {
            return ResponseEntity.badRequest().body("Invalid application.");
        }
        if (application.getStage() != Application.ApplicationStage.STAGE_3) {
            return ResponseEntity.badRequest().body("Cannot accept offer unless Stage 3 is complete.");
        }

        application.setOfferAccepted(true);  // Update acceptance status
        application.setStage(Application.ApplicationStage.STAGE_4);  // Move to Stage 4
        applicationRepository.save(application);

        return ResponseEntity.ok(createResponse(id, application.getApplicantName(), application.getResume(), 4));
    }

    // Helper method to create structured response
    private Object createResponse(Long applicationId, String applicantName, String resume, int stage, String... additionalFields) {
        // Create a response structure
        return Map.of(
                "applicationId", applicationId,
                "applicantName", applicantName,
                "resume", resume,
                "stage", stage,
                "additionalFields", additionalFields
        );
    }
}

