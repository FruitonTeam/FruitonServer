package cz.cuni.mff.fruiton.dao.domain;

import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.util.KernelUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FruitonTeam {

    private String name;

    private List<FruitonTeamMember> fruitons = new LinkedList<>();

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final List<FruitonTeamMember> getFruitons() {
        return fruitons;
    }

    public final void setFruitons(final List<FruitonTeamMember> fruitons) {
        this.fruitons = fruitons;
    }

    public final GameProtos.FruitonTeam toProtobuf() {
        return GameProtos.FruitonTeam.newBuilder()
                .setName(name)
                .addAllFruitonIDs(fruitons.stream().map(FruitonTeamMember::getFruitonId).collect(Collectors.toList()))
                .addAllPositions(fruitons.stream().map(f -> KernelUtils.positionOf(f.getX(), f.getY()))
                        .collect(Collectors.toList()))
                .build();
    }

    public static FruitonTeam fromProtobuf(final GameProtos.FruitonTeam protobufTeam) {
        FruitonTeam team = new FruitonTeam();
        team.name = protobufTeam.getName();
        for (int i = 0; i < protobufTeam.getFruitonIDsCount(); i++) {
            FruitonTeamMember member = new FruitonTeamMember();
            member.setFruitonId(protobufTeam.getFruitonIDs(i));
            member.setX(protobufTeam.getPositions(i).getX());
            member.setY(protobufTeam.getPositions(i).getY());
            team.fruitons.add(member);
        }
        return team;
    }

}
