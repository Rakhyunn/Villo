package com.villo.domain.village;

import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.entity.type.Provider;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.entity.VillagePeople;
import com.villo.domain.village.entity.type.VillagerGrade;
import com.villo.domain.village.repository.UserVillageRepository;
import com.villo.domain.village.repository.VillagePeopleRepository;
import com.villo.domain.village.service.VillagePeopleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("주민 영입 동시성 테스트")
public class VillagerBuyConcurrencyTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserVillageRepository userVillageRepository;
    @Autowired
    private VillagePeopleRepository villagePeopleRepository;
    @Autowired
    private VillagePeopleService villagePeopleService;

    private Long userId;
    private Long villagerId;
    private static final int VILLAGER_PRICE = 100;
    private static final int STARTING_GOLD = 100; // 정확히 1번만 구매 가능한 골드
    private static final int THREAD_COUNT = 10;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .email("buytest" + System.nanoTime() + "@test.com")
                .provider(Provider.KAKAO)
                .providerId("buytest-" + System.nanoTime())
                .totalGold(STARTING_GOLD)
                .dailyGold(0)
                .lastGoldResetDate(java.time.LocalDate.now())
                .build());
        userId = user.getId();

        userVillageRepository.save(UserVillage.builder()
                .user(user)
                .villageName("테스트마을")
                .villageLevel(1)
                .build());

        VillagePeople villager = villagePeopleRepository.save(VillagePeople.builder()
                .name("테스트주민")
                .grade(VillagerGrade.COMMON)
                .price(VILLAGER_PRICE)
                .imageUrl("🐻")
                .description("테스트용 주민")
                .isActive(true)
                .build());
        villagerId = villager.getId();
    }

    @Test
    void buyConcurrencyTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        IntStream.range(0, THREAD_COUNT).forEach(i -> {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    villagePeopleService.buyVillager(userId, villagerId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("구매 실패: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        });

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        User after = userRepository.findById(userId).orElseThrow();

        System.out.println("=== 시작 골드: " + STARTING_GOLD + ", 주민 가격: " + VILLAGER_PRICE);
        System.out.println("=== 구매 성공 수: " + successCount.get() + ", 실패 수: " + failCount.get());
        System.out.println("=== 최종 골드: " + after.getTotalGold());

        // 검증 1: 정확히 1번만 구매 성공해야 함 (골드가 1번 구매분밖에 없으므로)
        assertThat(successCount.get()).isEqualTo(1);

        // 검증 2: 골드는 정확히 0이어야 함 (음수가 되면 안 됨, 중복 차감도 안 됨)
        assertThat(after.getTotalGold()).isEqualTo(0);

        // 검증 3: 음수 골드는 절대 발생하면 안 됨 (가장 심각한 데이터 정합성 문제)
        assertThat(after.getTotalGold()).isGreaterThanOrEqualTo(0);
    }
}
