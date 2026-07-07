package com.villo.domain.village;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.entity.type.Provider;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.village.dto.PlacementRequest;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.entity.UserVillagePeople;
import com.villo.domain.village.entity.VillagePeople;
import com.villo.domain.village.entity.type.VillagerGrade;
import com.villo.domain.village.repository.UserVillagePeopleRepository;
import com.villo.domain.village.repository.UserVillageRepository;
import com.villo.domain.village.repository.VillagePeopleRepository;
import com.villo.domain.village.repository.VillagePlacementRepository;
import com.villo.domain.village.service.VillagePlacementService;
import com.villo.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("주민 배치 동시성 테스트")
public class VillagePlacementConcurrencyTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserVillageRepository userVillageRepository;
    @Autowired
    private VillagePeopleRepository villagePeopleRepository;
    @Autowired
    private UserVillagePeopleRepository userVillagePeopleRepository;
    @Autowired
    private VillagePlacementService villagePlacementService;
    @Autowired
    private VillagePlacementRepository villagePlacementRepository;

    private Long userId;
    private List<Long> userVillagePeopleIds;
    private static final int THREAD_COUNT = 10;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .email("placement" + System.nanoTime() + "@test.com")
                .provider(Provider.KAKAO)
                .providerId("placement-" + System.nanoTime())
                .totalGold(0)
                .dailyGold(0)
                .lastGoldResetDate(LocalDate.now())
                .build());
        userId = user.getId();

        userVillageRepository.save(UserVillage.builder()
                .user(user)
                .villageName("테스트마을")
                .villageLevel(1)
                .build());

        // 서로 다른 주민 10마리를 보유 상태로 준비 (모두 "같은 좌표"에 배치 시도할 대상)
        userVillagePeopleIds = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            VillagePeople villager = villagePeopleRepository.save(VillagePeople.builder()
                    .name("주민" + i)
                    .grade(VillagerGrade.COMMON)
                    .price(100)
                    .imageUrl("🐻")
                    .description("테스트용")
                    .isActive(true)
                    .build());

            UserVillagePeople uvp = userVillagePeopleRepository.save(UserVillagePeople.builder()
                    .user(user)
                    .villagePeople(villager)
                    .build());
            userVillagePeopleIds.add(uvp.getId());
        }
    }

    @Test
    void placementConcurrencyTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger domainErrorCount = new AtomicInteger(0);
        AtomicInteger unexpectedErrorCount = new AtomicInteger(0);

        // 10명의 서로 다른 주민을 전부 "같은 좌표(0,0)"에 배치 시도 → 동시 경합 유도
        IntStream.range(0, THREAD_COUNT).forEach(i -> {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    villagePlacementService.createPlacement(
                            userId,
                            new PlacementRequest(userVillagePeopleIds.get(i), 0, 0)
                    );
                    successCount.incrementAndGet();
                } catch (CustomException e) {
                    domainErrorCount.incrementAndGet();
                    System.out.println("도메인 에러: " + e.getMessage());
                } catch (Exception e) {
                    unexpectedErrorCount.incrementAndGet();
                    System.out.println("예상 못한 에러: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        });

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        long placementCount = villagePlacementRepository.count();

        System.out.println("=== 배치 성공: " + successCount.get());
        System.out.println("=== 도메인 에러(정상): " + domainErrorCount.get());
        System.out.println("=== 예상 못한 에러(500 등): " + unexpectedErrorCount.get());
        System.out.println("=== 실제 DB에 저장된 배치 수: " + placementCount);

        // 검증 1: 같은 좌표에는 정확히 1명만 배치되어야 함
        assertThat(placementCount).isEqualTo(1);

        // 검증 2: 나머지 9건은 "예상 못한 에러(500)"가 아니라 "도메인 에러"로 처리되어야 함
        assertThat(unexpectedErrorCount.get()).isEqualTo(0);
        assertThat(domainErrorCount.get()).isEqualTo(THREAD_COUNT - 1);
    }
}
