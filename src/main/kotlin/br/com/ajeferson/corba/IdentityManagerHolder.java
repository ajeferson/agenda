package br.com.ajeferson.corba;

/**
* br/com/ajeferson/corba/IdentityManagerHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Agenda.idl
* Quinta-feira, 26 de Abril de 2018 17h26min30s BRT
*/

public final class IdentityManagerHolder implements org.omg.CORBA.portable.Streamable
{
  public br.com.ajeferson.corba.IdentityManager value = null;

  public IdentityManagerHolder ()
  {
  }

  public IdentityManagerHolder (br.com.ajeferson.corba.IdentityManager initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = br.com.ajeferson.corba.IdentityManagerHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    br.com.ajeferson.corba.IdentityManagerHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return br.com.ajeferson.corba.IdentityManagerHelper.type ();
  }

}
