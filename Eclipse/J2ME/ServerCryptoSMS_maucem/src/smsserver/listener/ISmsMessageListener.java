package smsserver.listener;

import java.util.EventListener;


// Interfaccia listener per le classi che devono/vogliono gestire
// gli sms in ingresso
public interface ISmsMessageListener extends EventListener
{
     public void notifyIncomingMessage(util.wma.BinaryMessage m, boolean isEmulator);
}

