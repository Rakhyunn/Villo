package com.villo.global.init;

import com.villo.domain.village.entity.VillagePeople;
import com.villo.domain.village.entity.type.VillagerGrade;
import com.villo.domain.village.repository.VillagePeopleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class InitData {
    @Autowired
    @Lazy
    private InitData self;

    private final VillagePeopleRepository villagePeopleRepository;

    @Bean
    public ApplicationRunner initDataApplicationRunner() {
        return args -> {
            self.work1();   // 주민 데이터 생성
        };
    }

    @Transactional
    public void work1() {
        // 이미 데이터가 있으면 스킵 (중복 삽입 방지)
        if (villagePeopleRepository.count() > 0) {
            return;
        }

        // COMMON
        villagePeopleRepository.save(createVillager("곰돌이", VillagerGrade.COMMON, 100,
                "🐻", "순박하고 든든한 마을의 큰형님"));
        villagePeopleRepository.save(createVillager("토실이", VillagerGrade.COMMON, 100,
                "🐿️", "먹는 것 좋아하는 통통한 다람쥐"));
        villagePeopleRepository.save(createVillager("몽이", VillagerGrade.COMMON, 120,
                "🐶", "낮잠 자주 자는 게으른 강아지"));
        villagePeopleRepository.save(createVillager("나비", VillagerGrade.COMMON, 150,
                "🐱", "도도하고 새침한 고양이"));

        // RARE
        villagePeopleRepository.save(createVillager("여우비", VillagerGrade.RARE, 250,
                "🦊", "영리하고 재빠른 여우, 비 오는 날을 좋아함"));
        villagePeopleRepository.save(createVillager("부엉이", VillagerGrade.RARE, 300,
                "🦉", "밤에만 활동하는 지혜로운 학자"));
        villagePeopleRepository.save(createVillager("콩콩이", VillagerGrade.RARE, 350,
                "🐰", "방방 뛰어다니는 활발한 토끼"));

        // EPIC
        villagePeopleRepository.save(createVillager("사자킹", VillagerGrade.EPIC, 600,
                "🦁", "마을을 지키는 용맹한 리더"));
        villagePeopleRepository.save(createVillager("별빛이", VillagerGrade.EPIC, 800,
                "🌟", "밤하늘 별을 좋아하는 신비로운 여우"));

        // LEGENDARY
        villagePeopleRepository.save(createVillager("유니콘", VillagerGrade.LEGENDARY, 2000,
                "🦄", "무지개를 몰고 다니는 전설의 유니콘"));
    }

    private VillagePeople createVillager(String name, VillagerGrade grade, int price,
                                         String imageUrl, String description) {
        return VillagePeople.builder()
                .name(name)
                .grade(grade)
                .price(price)
                .imageUrl(imageUrl)
                .description(description)
                .isActive(true)
                .build();
    }
}
