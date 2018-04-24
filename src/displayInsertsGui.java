import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTable;


public class displayInsertsGui extends JDialog {

	private final JPanel contentPanel = new JPanel();
	ArrayList<Account> creatableAccountList;

	/**
	 * Launch the application.
	 */

	/**
	 * Create the dialog.
	 * @throws SQLException 
	 */
	public displayInsertsGui(GuiAccounts gaccts) throws SQLException {
		setBounds(100, 100, 1283, 453);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane);
		
		final JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Copy to clipBoard");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						StringSelection stringSelection = new StringSelection (textArea.getText());
						Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
						clpbrd.setContents (stringSelection, null);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Ok");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		
		//Generating inserts here for the accounts one by one and then fireing up the stored PL procedure
		//Assembling uneque account list 
		creatableAccountList = new ArrayList();
		
		
		Object[][] obj = getTableData(gaccts.table);

				//have to implement logic of when item selected then adding to the list
				for (int i = 0; i < obj.length;i++){
					
			
			if( Boolean.parseBoolean(obj[i][0].toString())){

				
				//lookup for the record in the account master file
				
				String account = obj[i][2].toString();
				String currency = obj[i][3].toString();
				
				//If record already in the list we skip the record, otherwise we add it
				
				for (int m = 0; m < gaccts.AccountMasterList.size(); m++){
					
					String account_toAdd = gaccts.AccountMasterList.get(m).type;
					String currency_toAdd = gaccts.AccountMasterList.get(m).currency;
					
					
					if(account.equals(account_toAdd)&&currency.equals(currency_toAdd)){
						
						boolean foundEqual = true;

						for(Account acct : creatableAccountList){

							if(account.equals(acct.type)&&currency.equals(acct.currency)){
								foundEqual = false;
							}
						}
						
						if(foundEqual){
							//adding account if not added already
							creatableAccountList.add(gaccts.AccountMasterList.get(m));
							
						}

					}
					
					
					
				}
				
				
			}

				
				
			
			}
		
		//old instance of the Connection
		//gaccts.g.c
				//Here looping trough actual account list and sending necesarry data to the package
				
				String insertPlaceHolder = "";
				
				for(int i = 0; i < creatableAccountList.size();i++){
					
					String tmp = gaccts.g.c.getInsertsFromAccounts(creatableAccountList.get(i), gaccts.inst_number, gaccts.test_system);
					
					String response  = creatableAccountList.get(i).type_index + " - " + creatableAccountList.get(i).currency_index+" - " + gaccts.inst_number+" - " + gaccts.test_system;
					
					
					if(tmp == null){
						
						insertPlaceHolder += "THERE WAS ERROR: Probably entrie already exists in DB: " + response;
						insertPlaceHolder += "\n";
						
					}
					else{
						
						insertPlaceHolder += "-- inserts for:" + response;
						insertPlaceHolder += "\n";
						insertPlaceHolder += tmp;
						insertPlaceHolder += "\n";
						insertPlaceHolder += "\n";
					}

					
					
					System.out.println(creatableAccountList.get(i).type_index+" - " + creatableAccountList.get(i).currency_index+" - " + gaccts.inst_number+" - " + gaccts.test_system);
				
				}
				
				
				
		textArea.setText(insertPlaceHolder);
		
	}
	
	
	
	public Object[][] getTableData (JTable table) {
		
	    DefaultTableModel dtm = (DefaultTableModel) table.getModel();
	    int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
	    Object[][] tableData = new Object[nRow][nCol];
	    for (int i = 0 ; i < nRow ; i++)
	        for (int j = 0 ; j < nCol ; j++)
	        	if(dtm.getValueAt(i,j)==null||dtm.getValueAt(i,j)==""){
	        		tableData[i][j] = "-";
	        	}
	        	else
	            tableData[i][j] = dtm.getValueAt(i,j);
	    return tableData;
	    
	}
	
	
	
	
}
