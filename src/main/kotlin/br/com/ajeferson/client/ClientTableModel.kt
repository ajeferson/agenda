package br.com.ajeferson.client

import br.com.ajeferson.client.protocol.TableDataSource
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel

class ClientTableModel(dataSource: TableDataSource): AbstractTableModel(), TableDataSource by dataSource {

    override fun getRowCount() = numberOfRows()

    override fun getColumnCount() = numberOfColumns()

    override fun getValueAt(row: Int, column: Int) = valueAt(row, column)

    override fun getColumnName(index: Int) = columnNameAt(index)

    override fun isCellEditable(rowIndex: Int, columnIndex: Int) = false

    fun reloadData() {
        SwingUtilities.invokeLater { fireTableDataChanged() }
    }

}