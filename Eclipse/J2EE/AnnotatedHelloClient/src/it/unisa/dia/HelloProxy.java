package it.unisa.dia;

public class HelloProxy implements it.unisa.dia.Hello {
  private String _endpoint = null;
  private it.unisa.dia.Hello hello = null;
  
  public HelloProxy() {
    _initHelloProxy();
  }
  
  public HelloProxy(String endpoint) {
    _endpoint = endpoint;
    _initHelloProxy();
  }
  
  private void _initHelloProxy() {
    try {
      hello = (new it.unisa.dia.HelloServiceLocator()).getHelloPort();
      if (hello != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)hello)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)hello)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (hello != null)
      ((javax.xml.rpc.Stub)hello)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public it.unisa.dia.Hello getHello() {
    if (hello == null)
      _initHelloProxy();
    return hello;
  }
  
  public java.lang.String greet(java.lang.String name) throws java.rmi.RemoteException{
    if (hello == null)
      _initHelloProxy();
    return hello.greet(name);
  }
  
  
}