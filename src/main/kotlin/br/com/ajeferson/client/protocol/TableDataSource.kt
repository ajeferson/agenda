package br.com.ajeferson.client.protocol

interface TableDataSource {

    fun numberOfRows(): Int
    fun numberOfColumns(): Int
    fun columnNameAt(index: Int): String
    fun valueAt(row: Int, column: Int): Any

}