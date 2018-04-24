import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

public class GuiAccounts extends JFrame {

	private JPanel contentPane;
	protected JTable table;
	
	
	
	Gui g;
	GuiAccounts gaccts;
	
	ArrayList<Account> accts;
	
	//lazy way to do it
	ArrayList<Account> AccountMasterList;
	
	String inst_number ;
	String test_system ;

	/**
	 * Launch the application.
	 */

	/**
	 * Create the frame.
	 * @throws SQLException 
	 */
	public GuiAccounts(final Gui g) throws SQLException {
		
		//Making avalable everything from Previouse Gui
		this.g = g;
		this.gaccts = this;
		
		AccountMasterList = new ArrayList();
		
		inst_number = g.cb_institution.getSelectedItem().toString().substring(0, 8);
		test_system =  g.cb_testSystem.getSelectedItem().toString();
		
		setTitle("Accounts Specofoed in Posting Tariffs");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 790, 408);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		TableModel tm = GenerateTableData(g,false);
		
    	//table.setAutoCreateRowSorter(true);
    	
    	table.setModel(tm);
    	
    	
    	TableColumn col4 = table.getColumnModel().getColumn(0);
    	col4.setCellEditor(table.getDefaultEditor(Boolean.class));
    	col4.setCellRenderer(table.getDefaultRenderer(Boolean.class));
    	
    	
    	
    	// Sticking in table colour scheme genearation
    	
    	table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
    	    @Override
    	    public Component getTableCellRendererComponent(JTable table,
    	            Object value, boolean isSelected, boolean hasFocus, int row, int col) {

    	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

    	        String status = (String)table.getModel().getValueAt(row, 4);
    	        if ("Yes".equals(status)) {
    	            setBackground(Color.GREEN);
    	            setForeground(Color.BLACK);
    	        } else {
    	            setBackground(Color.RED);
    	            setForeground(Color.WHITE);
    	        }       
    	        return this;
    	    }   
    	});
    	
    	
   /*
    	
    	table.getRowSorter ().addRowSorterListener (new RowSorterListener() {
            @Override
            public void sorterChanged (RowSorterEvent e) {
                if (e.getType () == RowSorterEvent.Type.SORTED) {
                    ///System.out.println ("selected row: "+table.getSelectedRow());
                    ///if (table.getSelectedRow () != -1) {
                     ///   table.scrollRectToVisible (table.getCellRect (table.getSelectedRow (), 0, false));
                  ///  }
                	
                	System.out.println("xxxx");
                	
                	table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
                	    @Override
                	    public Component getTableCellRendererComponent(JTable table,
                	            Object value, boolean isSelected, boolean hasFocus, int row, int col) {

                	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                	        String status = (String)table.getModel().getValueAt(row, 4);
                	        if ("1".equals(status)) {
                	            setBackground(Color.GREEN);
                	            setForeground(Color.BLACK);
                	        } else {
                	            setBackground(Color.RED);
                	            setForeground(Color.WHITE);
                	        }       
                	        return this;
                	    }   
                	});
                	
                	
                	table.repaint();
                	table.validate();
                	
                }

                
            }
        });
    	
    	*/
    	
    	
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		
		final JButton btnNewButton = new JButton("Select All");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				if (btnNewButton.getText().equals("Select All")){
					
					TableModel tm = null;
					try {
						tm = GenerateTableData(g,true);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
			    	//table.setAutoCreateRowSorter(true);
			    	
			    	table.setModel(tm);
			    	
			    	
			    	TableColumn col4 = table.getColumnModel().getColumn(0);
			    	col4.setCellEditor(table.getDefaultEditor(Boolean.class));
			    	col4.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			    	
			    	
			    	
			    	// Sticking in table colour scheme genearation
			    	
			    	table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			    	    @Override
			    	    public Component getTableCellRendererComponent(JTable table,
			    	            Object value, boolean isSelected, boolean hasFocus, int row, int col) {

			    	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			    	        String status = (String)table.getModel().getValueAt(row, 4);
			    	        if ("Yes".equals(status)) {
			    	            setBackground(Color.GREEN);
			    	            setForeground(Color.BLACK);
			    	        } else {
			    	            setBackground(Color.RED);
			    	            setForeground(Color.WHITE);
			    	        }       
			    	        return this;
			    	    }   
			    	});
					

					btnNewButton.setText("Unselect All");

					}
					else{
						
						TableModel tm = null;
						try {
							tm = GenerateTableData(g,false);
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
				    	//table.setAutoCreateRowSorter(true);
				    	
				    	table.setModel(tm);
				    	
				    	
				    	TableColumn col4 = table.getColumnModel().getColumn(0);
				    	col4.setCellEditor(table.getDefaultEditor(Boolean.class));
				    	col4.setCellRenderer(table.getDefaultRenderer(Boolean.class));
				    	
				    	
				    	
				    	// Sticking in table colour scheme genearation
				    	
				    	table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
				    	    @Override
				    	    public Component getTableCellRendererComponent(JTable table,
				    	            Object value, boolean isSelected, boolean hasFocus, int row, int col) {

				    	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

				    	        String status = (String)table.getModel().getValueAt(row, 4);
				    	        if ("Yes".equals(status)) {
				    	            setBackground(Color.GREEN);
				    	            setForeground(Color.BLACK);
				    	        } else {
				    	            setBackground(Color.RED);
				    	            setForeground(Color.WHITE);
				    	        }       
				    	        return this;
				    	    }   
				    	});
						

						
						
						
						btnNewButton.setText("Select All");
					}
					

				
				
				
				
			}
		});
		
		
		JLabel lblTestSystemSysymp = new JLabel("Test System: "+ test_system +" / Institution Number : "+ inst_number);
		
		
		panel.add(lblTestSystemSysymp);
		panel.add(btnNewButton);
		
		JButton btnGenerateInsertsFor = new JButton("Generate inserts for selected");
		btnGenerateInsertsFor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//passing instance of this class
				displayInsertsGui insGui = null;
				try {
					insGui = new displayInsertsGui(gaccts);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Set it visible
				insGui.setVisible(true);
				
			}
		});
		panel.add(btnGenerateInsertsFor);
		

		//owerriding behavour of the closing JFRAME
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
	}

	public TableModel GenerateTableData(Gui g,boolean condition) throws SQLException{
		
		String col[] = { "Select","Posting Tariff", "Account Name", "Currency", "Exists"};

		DefaultTableModel tableModel = new DefaultTableModel(col, 0);
		                                            // The 0 argument is number rows.
		//JTable table = new JTable(tableModel);

		String POSTING_TARIFF;
		String ACCOUNT;
		String CURRENCY;
		String Status;
		String isCreated;
		

		
		
		
		for (int i = 0; i < g.checkboxList.size();i++){
			
			if(g.checkboxList.get(i).isSelected()){
				
				
				System.out.println("Selected: " + g.checkboxList.get(i).getText());
				
				
				//posting tariff list avalable
				for (int n = 0; n<g.pTariffs.size();n++){
					
					String currentPmethod = g.pTariffs.get(n).postingInstruction;
					
					String selectedPmethod = g.checkboxList.get(i).getText();
					
					if ( currentPmethod.equals(selectedPmethod) ){
						
						String posting_method_index = g.pTariffs.get(n).index;
						
						
						accts = g.c.getAccountsFromPostingInst(posting_method_index, inst_number, test_system);
						
						
						
						
						
						//adding row by row to the table
						for (int x = 0; x < accts.size(); x++){
							
							
							//copying element to the list of accounts master
							Account a = accts.get(x);
							AccountMasterList.add(a);
							
							
							POSTING_TARIFF = selectedPmethod;
							ACCOUNT = accts.get(x).type;
							CURRENCY = accts.get(x).currency;
							isCreated = accts.get(x).exists;
							   Object[] data = { condition, POSTING_TARIFF, ACCOUNT, CURRENCY, isCreated};
							   
							tableModel.addRow(data);
							   
							}

					}
					

				}
				
				//Printing for debug
				
				

			}

		}

		return tableModel;
		
		
	}
	

}
