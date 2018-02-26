package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.FruitonService;
import cz.cuni.mff.fruiton.service.game.FruitonService.FruitonInfo;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public final class CollectionController {

    private final FruitonService fruitonService;

    private final UserService userService;

    private final AuthenticationService authService;

    @Autowired
    public CollectionController(
            final FruitonService fruitonService,
            final UserService userService,
            final AuthenticationService authService
    ) {
        this.fruitonService = fruitonService;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/collection")
    public String collection(final Model model) {
        Map<Integer, Long> fruitonCountMap = userService.getAvailableFruitons(authService.getLoggedInUser()).stream()
                .collect(Collectors.groupingBy(i -> i, Collectors.counting()));

        List<FruitonInfo> fruitonInfos = fruitonService.getFruitonInfos(KernelUtils.getAllFruitonIds());

        model.addAttribute("fruitons", fruitonInfos.stream()
                .map(info -> new CollectionFruitonInfo(info, fruitonCountMap.computeIfAbsent(info.getId(), k -> 0L).intValue()))
                .collect(Collectors.toList()));

        return "collection";
    }

    @GetMapping("/collection/fruiton")
    public String fruitonDetail(final Model model, @RequestParam("id") final int fruitonId) {
        FruitonInfo info = fruitonService.getFruitonInfo(fruitonId);

        model.addAttribute("fruiton", info);

        return "fruitonDetail";
    }

    private static class CollectionFruitonInfo {

        private FruitonInfo info;
        private int count;

        CollectionFruitonInfo(final FruitonInfo info, final int count) {
            this.info = info;
            this.count = count;
        }

        public FruitonInfo getInfo() {
            return info;
        }

        public int getCount() {
            return count;
        }
    }

}
