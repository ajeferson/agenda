package br.com.ajeferson.main

import br.com.ajeferson.client.Client
import javax.swing.JOptionPane

fun main(args: Array<String>) {

    var ip = JOptionPane.showInputDialog("Type the name server's IP (leave blank for localhost):") ?: return
    if(ip.isEmpty()) {
        ip = "localhost"
    }

    val instances = JOptionPane.showInputDialog("Type the amount of servers:") ?: return
    if(instances.isEmpty()) {
        JOptionPane.showMessageDialog(null, "You must provide the amount of servers", "Error", JOptionPane.INFORMATION_MESSAGE)
        return
    }

    Client(ip, instances.toInt())

}