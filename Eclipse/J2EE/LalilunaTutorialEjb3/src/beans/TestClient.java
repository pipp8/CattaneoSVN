package beans;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TestClient {

	/**	 * @param args	 */
		public static void main(String[] args) {
			/* get a initial context. By default the settings in the file
			 * jndi.properties are used.
			 * You can explicitly set up properties instead of using the file.
			 * Properties properties = new Properties();
			 * properties.put("java.naming.factory.initial","org.jnp.interfaces.NamingContextFactory");
			 * properties.put("java.naming.factory.url.pkgs","=org.jboss.naming:org.jnp.interfaces");
			 * properties.put("java.naming.provider.url","localhost:1099");
			 */
			Context context;
			try		{
				context = new InitialContext();
				BookTestBeanRemote beanRemote = (BookTestBeanRemote) context.lookup(BookTestBean.RemoteJNDIName);
				beanRemote.test();
			}
			catch (NamingException e)		{
				e.printStackTrace();
				/* I rethrow it as runtimeexception as there is really no need to continue
				 * if an exception happens and I do not want to catch it everywhere.
				 */
				throw new RuntimeException(e);
			}
		}
}
