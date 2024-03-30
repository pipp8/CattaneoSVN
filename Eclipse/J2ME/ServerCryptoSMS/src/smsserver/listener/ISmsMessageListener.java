package smsserver.listener;

import java.util.EventListener;
import smsserver.MyMessage;

// Interfaccia listener per le classi che devono/vogliono gestire
// gli sms in ingresso
public interface ISmsMessageListener extends EventListener
{
     public void notifyIncomingMessage(MyMessage m, boolean isEmulator);
}

