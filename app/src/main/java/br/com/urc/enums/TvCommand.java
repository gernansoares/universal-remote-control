package br.com.urc.enums;

import lombok.Getter;

@Getter
public enum TvCommand {

    VOLUME_UP("KEY_VOLUP"),
    VOLUME_DOWN("KEY_VOLDOWN"),
    VOLUME_MUTE("KEY_MUTE"),
    VOLUME_UNMUTE("KEY_UNMUTE"),
    POWER("KEY_POWER"),
    HOME("KEY_HOME"),
    BACK("KEY_BACK"),
    ENTER("KEY_ENTER"),
    ARROW_UP("KEY_UP"),
    ARROW_DOWN("KEY_DOWN"),
    ARROW_LEFT("KEY_LEFT"),
    ARROW_RIGHT("KEY_RIGHT"),
    NEXT_CHANNEL("KEY_CHUP"),
    PREVIOUS_CHANNEL("KEY_CHDOWN"),
    EXIT("KEY_EXIT"),
    INFO("KEY_INFO"),
    SOURCE("KEY_SOURCE");

    private String samsung;

    private TvCommand(String samsung) {
        this.samsung = samsung;
    }

}
