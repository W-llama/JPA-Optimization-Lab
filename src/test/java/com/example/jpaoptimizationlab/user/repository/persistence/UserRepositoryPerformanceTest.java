package com.example.jpaoptimizationlab.user.repository.persistence;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import com.example.jpaoptimizationlab.image.entity.Image;
import com.example.jpaoptimizationlab.user.domain.entity.User;
import com.example.jpaoptimizationlab.user.domain.entity.UserPhoto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("UserRepository Performance Test")
class UserRepositoryPerformanceTest {

	@Autowired
	private UserRepository userRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@BeforeEach
	void setup() {
		// 기존 데이터 정리
		entityManager.createQuery("DELETE FROM UserPhoto").executeUpdate();
		entityManager.createQuery("DELETE FROM User").executeUpdate();
		entityManager.createQuery("DELETE FROM Image").executeUpdate();

		// 테스트용 더미 데이터 100개 삽입
		for (int i = 0; i < 100; i++) {
			User user = new User("test_user_" + i, "http://image.url/user/" + i + ".jpg");
			entityManager.persist(user);

			Image image =new Image("http://image.url/original/" + i + ".jpg");
			entityManager.persist(image);

			// 각 User마다 UserPhoto 3개 생성
			for (int j = 0; j < 3; j++) {
				UserPhoto userPhoto = new UserPhoto(user, image, j);
				entityManager.persist(userPhoto);
			}
		}
		entityManager.flush();
		entityManager.clear();
	}

	private Statistics getStatistics() {
		// 1. EntityManager에서 SessionFactory 추출 (Hibernate 네이티브 API)
		SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);

		// 2. SessionFactory에서 Statistics 객체 가져오기
		Statistics statistics = sessionFactory.getStatistics();

		// 3. 통계 수집 기능 활성화
		statistics.setStatisticsEnabled(true);

		// 4. Statistics 객체 반환
		return statistics;
	}

	@Test
	@DisplayName("단일 User 조회 시 이미 로드된 User + 지연 로딩으로 UserPhoto 조회")
	void testSingleUserLazyLoading() {
		// given - User를 미리 1차 캐시에 로드
		userRepository.findById(1L); // User를 1차 캐시에 저장

		Statistics statistics = getStatistics();
		statistics.clear(); // User 로드 후 Statistics 초기화

		// when - 1차 캐시에서 User 조회, UserPhoto만 DB에서 조회
		Optional<User> user = userRepository.findById(1L);
		if (user.isPresent()) {
			user.get().getUserPhotos().size(); // 지연 로딩 트리거 (UserPhoto만 쿼리)
		}

		// then
		System.out.printf("단일 User 지연 로딩 쿼리 수: %d%n", statistics.getPrepareStatementCount());

		// User는 1차 캐시에서, UserPhoto만 DB 조회 = 1개 쿼리
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("새로운 세션에서 단일 User 조회 시 2개 쿼리 발생")
	void testSingleUserLazyLoadingInNewSession() {
		// given - 현재 영속성 컨텍스트 완전히 클리어
		entityManager.flush();
		entityManager.clear();

		Statistics statistics = getStatistics();
		statistics.clear();

		// when - 직접 EntityManager로 새로 조회
		User user = entityManager.find(User.class, 1L);
		if (user != null) {
			user.getUserPhotos().size(); // 지연 로딩 트리거
		}

		// then
		System.out.printf("새 세션에서 User + UserPhoto 쿼리 수: %d%n", statistics.getPrepareStatementCount());
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
	}


	@Test
	@DisplayName("존재하지 않는 ID로 조회하여 명확한 쿼리 카운트 확인")
	void testSingleUserLazyLoadingWithNonExistentId() {
		// given
		Statistics statistics = getStatistics();
		statistics.clear();

		// when - 존재하지 않는 ID로 조회 (캐시에 없음을 보장)
		Optional<User> user = userRepository.findById(999L);

		// then
		System.out.printf("존재하지 않는 User 조회 쿼리 수: %d%n", statistics.getPrepareStatementCount());
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1); // User 조회 쿼리만
		assertThat(user).isEmpty();
	}

	@Test
	@DisplayName("단일 User Fetch Join 조회 시 단일 쿼리로 최적화")
	void testSingleUserFetchJoin() {
		// given
		Statistics statistics = getStatistics();
		statistics.clear();

		// when
		User user = userRepository.findByIdWithUserPhotosFetchJoin(1L);
		if (user != null) {
			user.getUserPhotos().size(); // 이미 로드되어 있음
		}

		// then
		System.out.printf("단일 User Fetch Join 쿼리 수: %d%n", statistics.getPrepareStatementCount());
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("단일 User QueryDSL Fetch Join 조회 시 단일 쿼리로 최적화")
	void testSingleUserQueryDslFetchJoin() {
		// given
		Statistics statistics = getStatistics();
		statistics.clear();

		// when
		User user = userRepository.findByIdWithUserPhotosFetchJoinQueryDsl(1L);
		if (user != null) {
			user.getUserPhotos().size();
		}

		// then
		System.out.printf("단일 User QueryDSL Fetch Join 쿼리 수: %d%n", statistics.getPrepareStatementCount());
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("지연 로딩 시 BatchSize로 인해 N+1 문제가 완화된다")
	void testLazyLoadingWithBatchSize() {
		// given
		Statistics statistics = getStatistics();
		statistics.clear();

		long startTime = System.nanoTime();

		// when
		List<User> users = userRepository.findAll();
		for (User user : users) {
			user.getUserPhotos().size(); // 지연 로딩 트리거
		}

		long endTime = System.nanoTime();

		// then
		long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
		System.out.printf("BatchSize 적용된 쿼리 실행 시간: %dms%n", durationMillis);
		System.out.printf("BatchSize 적용된 쿼리 실행 수: %d%n", statistics.getPrepareStatementCount());

		// BatchSize(10)로 인해 1(User) + 10(UserPhoto 배치) = 11개 쿼리
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(11);
	}

	@Test
	@DisplayName("Fetch Join 사용 시 단일 쿼리로 최적화된다")
	void testFetchJoinOptimization() {
		// given
		Statistics statistics = getStatistics();
		statistics.clear();

		long startTime = System.nanoTime();

		// when
		List<User> users = userRepository.findAllWithUserPhotosFetchJoin();
		for (User user : users) {
			user.getUserPhotos().size();
		}

		long endTime = System.nanoTime();

		// then
		long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
		System.out.printf("Fetch Join 쿼리 실행 시간: %dms%n", durationMillis);
		System.out.printf("Fetch Join 쿼리 실행 수: %d%n", statistics.getPrepareStatementCount());

		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("QueryDSL Fetch Join도 단일 쿼리로 최적화된다")
	void testQueryDslFetchJoinOptimization() {
		// given
		Statistics statistics = getStatistics();
		statistics.clear();

		long startTime = System.nanoTime();

		// when
		List<User> users = userRepository.findAllWithUserPhotosFetchJoinQueryDsl();
		for (User user : users) {
			user.getUserPhotos().size();
		}

		long endTime = System.nanoTime();

		// then
		long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
		System.out.printf("QueryDSL Fetch Join 실행 시간: %dms%n", durationMillis);
		System.out.printf("QueryDSL Fetch Join 쿼리 실행 수: %d%n", statistics.getPrepareStatementCount());

		assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
	}
}
