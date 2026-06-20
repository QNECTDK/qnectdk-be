package com.qnectdk.domain.profile.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profiles_user_id", columnNames = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 30)
    private String school;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private Gender gender;

    @Column(nullable = false, columnDefinition = "char(4)")
    private String mbti;

    @Column(name = "drink_level", nullable = false, length = 20)
    private String drinkLevel;

    @Column(name = "favorite_food", length = 30)
    private String favoriteFood;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Builder
    private Profile(Long userId, String school, Gender gender, String mbti,
                    String drinkLevel, String favoriteFood) {
        this.userId = userId;
        this.school = school;
        this.gender = gender;
        this.mbti = mbti;
        this.drinkLevel = drinkLevel;
        this.favoriteFood = favoriteFood;
    }

    public static Profile create(Long userId, String school, Gender gender, String mbti,
                                 String drinkLevel, String favoriteFood) {
        return Profile.builder()
                .userId(userId)
                .school(school)
                .gender(gender)
                .mbti(mbti)
                .drinkLevel(drinkLevel)
                .favoriteFood(favoriteFood)
                .build();
    }

    public void updateBasicInfo(String school, Gender gender, String mbti,
                                String drinkLevel, String favoriteFood) {
        this.school = school;
        this.gender = gender;
        this.mbti = mbti;
        this.drinkLevel = drinkLevel;
        this.favoriteFood = favoriteFood;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
