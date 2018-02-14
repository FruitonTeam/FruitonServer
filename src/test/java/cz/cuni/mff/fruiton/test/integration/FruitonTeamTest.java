package cz.cuni.mff.fruiton.test.integration;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dao.domain.FruitonTeamMember;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FruitonTeamTest {

    private static final String TEAM_NAME = "team_name";

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void fruitonTeamsTest() {
        User user = TestUtils.defaultRegister(registrationService, userRepository);

        FruitonTeam team = new FruitonTeam();
        team.setName(TEAM_NAME);
        FruitonTeamMember member = new FruitonTeamMember();
        member.setX(1);
        member.setY(1);
        member.setFruitonId(1);
        team.setFruitons(List.of(member));

        userService.addFruitonTeam(UserIdHolder.of(user), team);

        user = userRepository.findOne(user.getId());

        assertEquals(1, user.getTeams().size());

        userService.addFruitonTeam(UserIdHolder.of(user), team);

        user = userRepository.findOne(user.getId());

        assertEquals(1, user.getTeams().size());

        userService.removeTeam(UserIdHolder.of(user), TEAM_NAME);

        user = userRepository.findOne(user.getId());

        assertTrue(user.getTeams().isEmpty());
    }

}
