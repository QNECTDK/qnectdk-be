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
 * 캐릭터 이미지는 프론트가 imageUrl 경로(/characters/{name}.png)로 매핑한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShopItemSeeder implements ApplicationRunner {

    private static final int CHARACTER_PRICE = 200;

    // {표시이름, 이미지키}
    private static final List<String[]> CHARACTERS = List.of(
            new String[]{"쥐", "rat"},
            new String[]{"소", "ox"},
            new String[]{"호랑이", "tiger"},
            new String[]{"토끼", "rabbit"},
            new String[]{"용", "dragon"},
            new String[]{"뱀", "snake"},
            new String[]{"말", "horse"},
            new String[]{"양", "sheep"},
            new String[]{"원숭이", "monkey"},
            new String[]{"닭", "rooster"},
            new String[]{"개", "dog"},
            new String[]{"돼지", "pig"},
            new String[]{"라쿤", "raccoon"},
            new String[]{"레서판다", "redpanda"},
            new String[]{"돌고래", "dolphin"},
            new String[]{"상어", "shark"},
            new String[]{"우파루파", "axolotl"}
    );

    private final ShopItemRepository shopItemRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 이미 아이템이 있으면 건너뜀 (멱등)
        if (shopItemRepository.count() > 0) {
            return;
        }

        // 캐릭터 17종 (각 200P)
        for (String[] c : CHARACTERS) {
            String name = c[0];
            String imageUrl = "/characters/" + c[1] + ".png";
            shopItemRepository.save(ShopItem.create(name, ItemType.CHARACTER, imageUrl, CHARACTER_PRICE));
        }

        log.info("상점 시드 적재 완료: 캐릭터 {}종", CHARACTERS.size());
    }
}