package com.qnectdk.domain.interest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_interests_category_name", columnNames = {"category", "name"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false, length = 30)
    private String name;

    @Builder
    private Interest(String category, String name) {
        this.category = category;
        this.name = name;
    }

    public static Interest create(String category, String name) {
        return Interest.builder().category(category).name(name).build();
    }
}
