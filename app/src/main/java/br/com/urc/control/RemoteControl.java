package br.com.urc.control;

import br.com.urc.common.enums.TvCommand;

public interface RemoteControl {

    void connect();

    void sendCommand(TvCommand command);

    void disconnect();

}
