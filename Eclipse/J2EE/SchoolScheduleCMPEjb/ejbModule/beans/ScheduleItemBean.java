/**
 * 
 */
package beans;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;




 
/**
 * <!-- begin-xdoclet-definition -->
 * @ejb.bean name="ScheduleItem" 
 *	jndi-name="ScheduleItem"
 *	type="CMP" 
 *  primkey-field="id" 
 *  schema="ScheduleItemSCHEMA" 
 *  cmp-version="2.x"
 *
 *  @ejb.persistence 
 *   table-name="PUBLIC.SCHEDITEM" 
 * 
 * @ejb.finder 
 *    query="SELECT OBJECT(a) FROM ScheduleItemSCHEMA as a"  
 *    signature="java.util.Collection findAll()"  
 *
 * @ejb.finder 
 *    query="SELECT OBJECT(a) FROM ScheduleItemSCHEMA as a WHERE a.scheduleID = (?1)"  
 *    signature="java.util.Collection findByScheduleID(java.lang.String scheduleID)"  
 * 
 * @ejb.finder 
 *    query="SELECT OBJECT(a) FROM ScheduleItemSCHEMA as a WHERE a.name = (?1)"  
 *    signature="java.util.Collection findByName(java.lang.String name)"  

 * @ejb.pk class="java.lang.Integer"
 *
 *
 * @jboss.persistence datasource="java:/DefaultDS" datasource-mapping="Hypersonic SQL" table-name="PUBLIC.SCHEDITEM" create-table="false" remove-table="false" alter-table="false"
 * <!-- end-xdoclet-definition -->
 * @generated
 **/

public abstract class ScheduleItemBean implements javax.ejb.EntityBean {

	private static int PRIMKEY = (int) System.currentTimeMillis();
	
	/**
	 *
	 * <!-- begin-user-doc -->
	 * The  ejbCreate method.
	 * <!-- end-user-doc -->
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.create-method view-type="local"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 */
	public java.lang.Integer ejbCreate(java.lang.String name) throws javax.ejb.CreateException {
		// EJB 2.0 spec says return null for CMP ejbCreate methods.
		// begin-user-code
		setId(new Integer(PRIMKEY++));
		setName(name);
		return null;
		// end-user-code
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * The container invokes this method immediately after it calls ejbCreate.
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void ejbPostCreate() throws javax.ejb.CreateException {
		// begin-user-code
		// end-user-code
	}

	/**
	 *
	 *
	 * <!-- begin-user-doc -->
	 * CMP Field id
	 *
	 * Returns the id
	 * @return the id
	 * 
	 * <!-- end-user-doc -->
	 *
	 * <!-- begin-xdoclet-definition --> 
	 *
	 * @ejb.persistent-field 
	 * @ejb.persistence
	 *    column-name="ITEMID"
	 *     jdbc-type="INTEGER"
	 *     sql-type="INTEGER"
	 *     read-only="false"
	 * @ejb.pk-field 
	 *
	 * @ejb.interface-method view-type="local"
	 * 
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 */
	public abstract java.lang.Integer getId();

	/**
	 * <!-- begin-user-doc -->
	 * Sets the id
	 * 
	 * @param java.lang.Integer the new id value
	 * <!-- end-user-doc -->
	 * 
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="local"
	 * <!-- end-xdoclet-definition -->
	 * @generated 
	 */
	public abstract void setId(java.lang.Integer id);

	/**
	 *
	 *
	 * <!-- begin-user-doc -->
	 * CMP Field name
	 *
	 * Returns the name
	 * @return the name
	 * 
	 * <!-- end-user-doc -->
	 *
	 * <!-- begin-xdoclet-definition --> 
	 *
	 * @ejb.persistent-field 
	 * @ejb.persistence
	 *    column-name="NAME"
	 *     jdbc-type="VARCHAR"
	 *     sql-type="VARCHAR(30)"
	 *     read-only="false"
	 *  
	 *
	 * @ejb.interface-method view-type="local"
	 * 
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 */
	public abstract java.lang.String getName();

	/**
	 * <!-- begin-user-doc -->
	 * Sets the name
	 * 
	 * @param java.lang.String the new name value
	 * <!-- end-user-doc -->
	 * 
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="local"
	 * <!-- end-xdoclet-definition -->
	 * @generated 
	 */
	public abstract void setName(java.lang.String name);

	/**
	 *
	 *
	 * <!-- begin-user-doc -->
	 * CMP Field startTime
	 *
	 * Returns the startTime
	 * @return the startTime
	 * 
	 * <!-- end-user-doc -->
	 *
	 * <!-- begin-xdoclet-definition --> 
	 *
	 * @ejb.persistent-field 
	 * @ejb.persistence
	 *    column-name="STARTTIME"
	 *     jdbc-type="INTEGER"
	 *     sql-type="INTEGER"
	 *     read-only="false"
	 *  
	 *
	 * @ejb.interface-method view-type="local"
	 * 
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 */
	public abstract java.lang.Integer getStartTime();

	/**
	 * <!-- begin-user-doc -->
	 * Sets the startTime
	 * 
	 * @param java.lang.Integer the new startTime value
	 * <!-- end-user-doc -->
	 * 
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="local"
	 * <!-- end-xdoclet-definition -->
	 * @generated 
	 */
	public abstract void setStartTime(java.lang.Integer startTime);

	/**
	 *
	 *
	 * <!-- begin-user-doc -->
	 * CMP Field endTime
	 *
	 * Returns the endTime
	 * @return the endTime
	 * 
	 * <!-- end-user-doc -->
	 *
	 * <!-- begin-xdoclet-definition --> 
	 *
	 * @ejb.persistent-field 
	 * @ejb.persistence
	 *    column-name="ENDTIME"
	 *     jdbc-type="INTEGER"
	 *     sql-type="INTEGER"
	 *     read-only="false"
	 *  
	 *
	 * @ejb.interface-method view-type="local"
	 * 
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 */
	public abstract java.lang.Integer getEndTime();

	/**
	 * <!-- begin-user-doc -->
	 * Sets the endTime
	 * 
	 * @param java.lang.Integer the new endTime value
	 * <!-- end-user-doc -->
	 * 
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="local"
	 * <!-- end-xdoclet-definition -->
	 * @generated 
	 */
	public abstract void setEndTime(java.lang.Integer endTime);

	/**
	 *
	 *
	 * <!-- begin-user-doc -->
	 * CMP Field day
	 *
	 * Returns the day
	 * @return the day
	 * 
	 * <!-- end-user-doc -->
	 *
	 * <!-- begin-xdoclet-definition --> 
	 *
	 * @ejb.persistent-field 
	 * @ejb.persistence
	 *    column-name="DAY"
	 *     jdbc-type="INTEGER"
	 *     sql-type="INTEGER"
	 *     read-only="false"
	 *  
	 *
	 * @ejb.interface-method view-type="local"
	 * 
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 */
	public abstract java.lang.Integer getDay();

	/**
	 * <!-- begin-user-doc -->
	 * Sets the day
	 * 
	 * @param java.lang.Integer the new day value
	 * <!-- end-user-doc -->
	 * 
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="local"
	 * <!-- end-xdoclet-definition -->
	 * @generated 
	 */
	public abstract void setDay(java.lang.Integer day);

	/**
	 *
	 *
	 * <!-- begin-user-doc -->
	 * CMP Field scheduleID
	 *
	 * Returns the scheduleID
	 * @return the scheduleID
	 * 
	 * <!-- end-user-doc -->
	 *
	 * <!-- begin-xdoclet-definition --> 
	 *
	 * @ejb.persistent-field 
	 * @ejb.persistence
	 *    column-name="SCHEDULEID"
	 *     jdbc-type="VARCHAR"
	 *     sql-type="VARCHAR(10)"
	 *     read-only="false"
	 *  
	 *
	 * @ejb.interface-method view-type="local"
	 * 
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 */
	public abstract java.lang.String getScheduleID();

	/**
	 * <!-- begin-user-doc -->
	 * Sets the scheduleID
	 * 
	 * @param java.lang.String the new scheduleID value
	 * <!-- end-user-doc -->
	 * 
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="local"
	 * <!-- end-xdoclet-definition -->
	 * @generated 
	 */
	public abstract void setScheduleID(java.lang.String scheduleID);

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbLoad()
	 */
	public void ejbLoad() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbRemove()
	 */
	public void ejbRemove() throws RemoveException, EJBException,
			RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#ejbStore()
	 */
	public void ejbStore() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
	 */
	public void setEntityContext(EntityContext arg0) throws EJBException,
			RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.EntityBean#unsetEntityContext()
	 */
	public void unsetEntityContext() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 */
	public ScheduleItemBean() {
		// TODO Auto-generated constructor stub
	}

}
