package com.ombremoon.sentinellib.common.event;

import com.ombremoon.sentinellib.api.box.SentinelBox;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

public class RegisterPlayerSentinelBoxEvent extends PlayerEvent {
    private final List<SentinelBox> boxList;

    public RegisterPlayerSentinelBoxEvent(Player player, List<SentinelBox> boxList) {
        super(player);
        this.boxList = boxList;
    }

    /**
     * @return The list of registered sentinel boxes on the player
     */
    public void addEntry(SentinelBox sentinelBox) {
        this.boxList.add(sentinelBox);
    }

    public static void registerSentinelBox(Player player, List<SentinelBox> sentinelBoxes) {
        RegisterPlayerSentinelBoxEvent event = new RegisterPlayerSentinelBoxEvent(player, sentinelBoxes);
        NeoForge.EVENT_BUS.post(event);
    }
}
