package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.service.game.FruitonService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import cz.cuni.mff.fruiton.util.RangedProbabilityRandom;
import fruiton.kernel.Fruiton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:game.properties")
public final class FruitonServiceImpl implements FruitonService {

    @Value("#{'${default.unlocked.fruitons}'.split(',')}")
    private List<Integer> defaultUnlockedFruitons;

    private final Map<Integer, FruitonInfo> fruitonInfoMap = new HashMap<>();

    private final RangedProbabilityRandom<Integer> numberOfFruitonsUnlockedGenerator =
            new RangedProbabilityRandom<>(List.of(0, 1, 2), List.of(50, 25, 25));

    private RangedProbabilityRandom<Integer> fruitonsGenerator;

    private final Random random = new Random();

    private List<Integer> nonDefaultFruitons;

    @PostConstruct
    private void init() {
        List<Integer> nonDefaultFruitons = new ArrayList<>();

        for (int fruitonId : KernelUtils.getAllFruitonIds()) {
            Fruiton f = KernelUtils.getFruiton(fruitonId);
            fruitonInfoMap.put(fruitonId, FruitonInfo.fromFruiton(f));

            if (!defaultUnlockedFruitons.contains(fruitonId)) {
                nonDefaultFruitons.add(fruitonId);
            }
        }
        this.nonDefaultFruitons = Collections.unmodifiableList(nonDefaultFruitons);

        // every fruiton with the same probability for now
        fruitonsGenerator = new RangedProbabilityRandom<>(nonDefaultFruitons, Collections.nCopies(nonDefaultFruitons.size(), 1));
    }

    @Override
    public List<FruitonInfo> getFruitonInfos(final List<Integer> fruitonIds) {
        if (fruitonIds == null) {
            return Collections.emptyList();
        }

        List<FruitonInfo> fruitonInfos = new ArrayList<>(fruitonIds.size());
        for (int fruitonId : fruitonIds) {
            if (!fruitonInfoMap.containsKey(fruitonId)) {
                continue;
            }
            fruitonInfos.add(fruitonInfoMap.get(fruitonId));
        }
        return fruitonInfos;
    }

    @Override
    public FruitonInfo getFruitonInfo(final int fruitonId) {
        return fruitonInfoMap.get(fruitonId);
    }

    @Override
    public List<Integer> getRandomFruitons() {
        int numberOfUnlockedFruitons = numberOfFruitonsUnlockedGenerator.next();

        List<Integer> result = new ArrayList<>(numberOfUnlockedFruitons);
        for (int i = 0; i < numberOfUnlockedFruitons; i++) {
            result.add(fruitonsGenerator.next());
        }

        return result;
    }

    @Override
    public List<Integer> getRandomFruitons(final int count, final List<Integer> excludedFruitons) {
        List<Integer> availableFruitons = new ArrayList<>(nonDefaultFruitons);
        availableFruitons.removeAll(excludedFruitons);

        List<Integer> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int randomIdx = random.nextInt(availableFruitons.size());
            result.add(availableFruitons.get(randomIdx));
            availableFruitons.remove(randomIdx);
        }
        return result;
    }

    @Override
    public List<Integer> filter(final List<Integer> fruitonIds, final FruitonType type) {
        return fruitonIds.stream().filter(id -> fruitonInfoMap.get(id).getType() == type).collect(Collectors.toList());
    }

}
