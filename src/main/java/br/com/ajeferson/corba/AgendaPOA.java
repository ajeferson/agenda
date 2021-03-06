package br.com.ajeferson.corba;


/**
* br/com/ajeferson/corba/AgendaPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Agenda.idl
* Segunda-feira, 30 de Abril de 2018 09h56min00s BRT
*/

public abstract class AgendaPOA extends org.omg.PortableServer.Servant
 implements br.com.ajeferson.corba.AgendaOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("isAlive", new java.lang.Integer (0));
    _methods.put ("insert", new java.lang.Integer (1));
    _methods.put ("remove", new java.lang.Integer (2));
    _methods.put ("update", new java.lang.Integer (3));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // br/com/ajeferson/corba/Agenda/isAlive
       {
         boolean $result = false;
         $result = this.isAlive ();
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 1:  // br/com/ajeferson/corba/Agenda/insert
       {
         String name = in.read_string ();
         String phoneNumber = in.read_string ();
         boolean $result = false;
         $result = this.insert (name, phoneNumber);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 2:  // br/com/ajeferson/corba/Agenda/remove
       {
         String name = in.read_string ();
         boolean $result = false;
         $result = this.remove (name);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 3:  // br/com/ajeferson/corba/Agenda/update
       {
         String oldName = in.read_string ();
         String newName = in.read_string ();
         String newPhoneNumber = in.read_string ();
         boolean $result = false;
         $result = this.update (oldName, newName, newPhoneNumber);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:br/com/ajeferson/corba/Agenda:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public Agenda _this() 
  {
    return AgendaHelper.narrow(
    super._this_object());
  }

  public Agenda _this(org.omg.CORBA.ORB orb) 
  {
    return AgendaHelper.narrow(
    super._this_object(orb));
  }


} // class AgendaPOA
