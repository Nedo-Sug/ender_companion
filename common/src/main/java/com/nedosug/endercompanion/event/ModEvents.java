package com.nedosug.endercompanion.event;

import dev.architectury.event.events.common.PlayerEvent;

public final class ModEvents {
    private ModEvents() {
    }

    public static void register() {
        PlayerEvent.PLAYER_JOIN.register(PlayerJoinHandler::onPlayerJoin);
    }
}
