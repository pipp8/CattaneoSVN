/*
 * Generated by XDoclet - Do not edit!
 */
package test.tusc;

/**
 * Home interface for MySession.
 * @generated 
 * @wtp generated
 */
public interface MySessionHome
   extends javax.ejb.EJBHome
{
   public static final String COMP_NAME="java:comp/env/ejb/MySession";
   public static final String JNDI_NAME="MySession";

   public test.tusc.MySession create()
      throws javax.ejb.CreateException,java.rmi.RemoteException;

}
