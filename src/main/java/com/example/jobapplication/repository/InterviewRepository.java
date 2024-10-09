package com.example.jobapplication.repository;



import com.example.jobapplication.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
}

