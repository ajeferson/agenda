package br.com.ajeferson.main

import br.com.ajeferson.server.Server
import javax.swing.JOptionPane

fun main(args: Array<String>) {

    var ip = JOptionPane.showInputDialog("Type the name server's IP (leave blank for localhost):") ?: return
    if(ip.isEmpty()) {
        ip = "localhost"
    }

    val id = JOptionPane.showInputDialog("Type the server's ID:") ?: return
    if(id.isEmpty()) {
        JOptionPane.showMessageDialog(null, "You must provide the server's ID", "Error", JOptionPane.INFORMATION_MESSAGE)
        return
    }

    val instances = JOptionPane.showInputDialog("Type the amount of servers:") ?: return
    if(instances.isEmpty()) {
        JOptionPane.showMessageDialog(null, "You must provide the amount of servers", "Error", JOptionPane.INFORMATION_MESSAGE)
        return
    }

    Server(id, ip, instances.toInt())

}