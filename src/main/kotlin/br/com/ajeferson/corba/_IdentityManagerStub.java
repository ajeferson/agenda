package br.com.ajeferson.corba;


/**
* br/com/ajeferson/corba/_IdentityManagerStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Agenda.idl
* Quinta-feira, 26 de Abril de 2018 16h24min07s BRT
*/

public class _IdentityManagerStub extends org.omg.CORBA.portable.ObjectImpl implements br.com.ajeferson.corba.IdentityManager
{

  public void identify (String identity)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("identify", true);
                $out.write_string (identity);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                identify (identity        );
            } finally {
                _releaseReply ($in);
            }
  } // identify

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:br/com/ajeferson/corba/IdentityManager:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _IdentityManagerStub
