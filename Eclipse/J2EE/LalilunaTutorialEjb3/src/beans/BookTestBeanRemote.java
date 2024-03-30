package beans;

import javax.ejb.Remote;

@Remote
public interface BookTestBeanRemote {
	public void test();
}
