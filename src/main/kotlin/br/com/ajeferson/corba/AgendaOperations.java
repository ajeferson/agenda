package br.com.ajeferson.corba;


/**
* br/com/ajeferson/corba/AgendaOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Agenda.idl
* Quinta-feira, 26 de Abril de 2018 16h24min07s BRT
*/

public interface AgendaOperations 
{
  boolean isAlive ();
  boolean insert (String name, String phoneNumber);
  boolean remove (String name);
} // interface AgendaOperations
