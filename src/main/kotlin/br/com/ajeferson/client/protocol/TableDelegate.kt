package br.com.ajeferson.client.protocol

interface TableDelegate {

    fun didChangeName(index: Int, name: String)
    fun didChangePhoneNumber(index: Int, phoneNumber: String)

}