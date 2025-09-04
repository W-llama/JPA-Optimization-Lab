package com.example.jpaoptimizationlab.user.domain.entity;

import com.example.jpaoptimizationlab.image.entity.Image;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "user_photo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPhoto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "image_id")
	private Image image;

	private int orderIndex; // 사진 순서 (1, 2, 3)
}
