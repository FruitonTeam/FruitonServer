package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.service.game.FruitonService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import fruiton.kernel.Fruiton;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public final class FruitonServiceImpl implements FruitonService {

    private Map<Integer, FruitonInfo> fruitonInfoMap = new HashMap<>();

    @PostConstruct
    private void init() {
        for (int fruitonId : KernelUtils.getAllFruitonIds()) {
            Fruiton f = KernelUtils.getFruiton(fruitonId);
            fruitonInfoMap.put(fruitonId, FruitonInfo.fromFruiton(f));
        }
    }

    @Override
    public List<FruitonInfo> getFruitonInfos(final List<Integer> fruitonIds) {
        if (fruitonIds == null) {
            return Collections.emptyList();
        }

        List<FruitonInfo> fruitonInfos = new ArrayList<>(fruitonIds.size());
        for (int fruitonId : fruitonIds) {
            fruitonInfos.add(fruitonInfoMap.get(fruitonId));
        }
        return fruitonInfos;
    }

    @Override
    public FruitonInfo getFruitonInfo(final int fruitonId) {
        return fruitonInfoMap.get(fruitonId);
    }

}
