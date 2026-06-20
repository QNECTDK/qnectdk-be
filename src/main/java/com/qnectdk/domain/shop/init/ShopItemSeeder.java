package com.qnectdk.domain.shop.init;

import com.qnectdk.domain.shop.entity.ItemType;
import com.qnectdk.domain.shop.entity.ShopItem;
import com.qnectdk.domain.shop.repository.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 상점 아이템 시드. 기동 시 상점이 비어있으면 캐릭터 17종(200P)을 적재(멱등).
 * 캐릭터 이미지는 프론트가 characterId로 매핑한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShopItemSeeder implements ApplicationRunner {

    private static final int CHARACTER_PRICE = 200;

    // {표시이름, characterId}
    private static final List<String[]> CHARACTERS = List.of(
            new String[]{"쥐", "character01"},
            new String[]{"소", "character02"},
            new String[]{"호랑이", "character03"},
            new String[]{"토끼", "character04"},
            new String[]{"용", "character05"},
            new String[]{"뱀", "character06"},
            new String[]{"말", "character07"},
            new String[]{"양", "character08"},
            new String[]{"원숭이", "character09"},
            new String[]{"닭", "character10"},
            new String[]{"개", "character11"},
            new String[]{"돼지", "character12"},
            new String[]{"코알라", "character13"},
            new String[]{"사자", "character14"},
            new String[]{"라쿤", "character15"},
            new String[]{"레서판다", "character16"},
            new String[]{"돌고래", "character17"},
            new String[]{"상어", "character18"},
            new String[]{"우파루파", "character19"}
    );

    private final ShopItemRepository shopItemRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 이미 아이템이 있으면 건너뜀 (멱등)
        if (shopItemRepository.count() > 0) {
            return;
        }

        // 캐릭터 19종 (각 200P)
        for (String[] c : CHARACTERS) {
            String name = c[0];
            String characterId = c[1];
            shopItemRepository.save(ShopItem.create(name, ItemType.CHARACTER, characterId, CHARACTER_PRICE));
        }

        log.info("상점 시드 적재 완료: 캐릭터 {}종", CHARACTERS.size());
    }
}