package com.qnectdk.domain.point.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "attendance_checks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_user_date", columnNames = {"user_id", "check_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceCheck extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    @Builder
    private AttendanceCheck(Long userId, LocalDate checkDate) {
        this.userId = userId;
        this.checkDate = checkDate;
    }

    public static AttendanceCheck of(Long userId, LocalDate checkDate) {
        return AttendanceCheck.builder()
                .userId(userId)
                .checkDate(checkDate)
                .build();
    }
}