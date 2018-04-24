import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JComboBox;
import java.awt.Label;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.awt.FlowLayout;
import java.awt.Checkbox;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import java.awt.GridLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

public class Gui extends JFrame {

	private JPanel contentPane;
	 Connection c;
	 Gui g;
	private User u;
	
	
	//checkbox list with selected posting tariffs
	public ArrayList<JCheckBox> checkboxList;
	
	//posting tariff list avalable
	public ArrayList<PostingTariff> pTariffs;
	
	
	final JPanel panel_1;
	JComboBox cb_institution;
	JComboBox cb_testSystem;
	final JButton btnSelectAll;

	/**
	 * Create the frame.
	 * @throws SQLException 
	 */
	public Gui() throws SQLException {
		
		g = this;
		setTitle("Posting Method Utilitie V0.5B");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 781, 488);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		//Filling combo box with institution_list
		
		//initializing connection
		u = new User("sysimp_util","sysimp");
		c = new Connection(u);
		
		//list to display
		String[] instList = GenerateInstitutionList(c);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(7, 11, 750, 394);
		contentPane.add(panel);
		panel.setLayout(null);
		
		Label label = new Label("Institution:");
		label.setBounds(329, 32, 62, 22);
		panel.add(label);
		
		cb_institution = new JComboBox(instList);
		cb_institution.setBounds(400, 32, 227, 20);
		cb_institution.setSelectedIndex(0);
		cb_institution.setOpaque(true);
		cb_institution.addItemListener(new ItemChangeListener(this));
		panel.add(cb_institution);
		
		panel_1 = new JPanel();
		panel_1.setBounds(5, 100, 740, 286);
		panel.add(panel_1);
		panel_1.setLayout(new GridLayout(15, 8, 8, 8));
		
		JLabel lblPostingMethodList = new JLabel("Posting Method List:");
		lblPostingMethodList.setBounds(31, 75, 124, 14);
		panel.add(lblPostingMethodList);
		
		JLabel lblTestSystem = new JLabel("Test System:");
		lblTestSystem.setBounds(10, 40, 75, 14);
		panel.add(lblTestSystem);
		
		
		
		String[] systemList = GenerateTestSystemList(c);
		
		cb_testSystem = new JComboBox(systemList);
		cb_testSystem.setSelectedIndex(0);
		cb_testSystem.addItemListener(new ItemChangeListenerTs(this));
		cb_testSystem.setBounds(90, 34, 161, 20);
		panel.add(cb_testSystem);
		
		JLabel lblPostingMethodUtilitie = new JLabel("Posting Method Utilitie V0.5");
		lblPostingMethodUtilitie.setBounds(255, 11, 179, 14);
		panel.add(lblPostingMethodUtilitie);
		
		btnSelectAll = new JButton("Select All");
		btnSelectAll.addActionListener(new ActionListener() {
			
			//adding switch logic to the button
			public void actionPerformed(ActionEvent arg0) {
				
				if (btnSelectAll.getText().equals("Select All")){
			
				for(JCheckBox box : checkboxList){	
					box.setSelected (true);
				}
				
				btnSelectAll.setText("Unselect All");
				}
				else{
					
					for(JCheckBox box : checkboxList){	
						box.setSelected (false);
					}
					
				btnSelectAll.setText("Select All");
				}
				
				contentPane.repaint();
				contentPane.validate();
				
			}
		});
		btnSelectAll.setBounds(165, 71, 101, 23);
		panel.add(btnSelectAll);
		
		JButton btnCheckAccountStatus = new JButton("Check Account Status");
		btnCheckAccountStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				///Enabling GUI to view table with accounts
				GuiAccounts gac = null;
				try {
					gac = new GuiAccounts(g);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Setting it visible
				gac.setVisible(true);
				
				
			}
		});
		btnCheckAccountStatus.setBounds(299, 416, 166, 23);
		contentPane.add(btnCheckAccountStatus);
		
		
        String inst_number = g.cb_institution.getSelectedItem().toString().substring(0, 8);
        
        String test_system =  g.cb_testSystem.getSelectedItem().toString();
		
		showPostingMethods(g.panel_1, g.c, inst_number, test_system);
	}
	
	//Generating institution list with right format
	public String[] GenerateInstitutionList(Connection c) throws SQLException{
		
		ArrayList<Institution> inst = c.getInstitutionList("SYSIMP");
		
		String [] tmp = new String[inst.size()];
		
			for(int i = 0; i<inst.size();i++){
				
				tmp[i] = inst.get(i).number + " - " + inst.get(i).name;
				
			}
		
		return tmp;
		
	}
	
	
	public String[] GenerateTestSystemList(Connection c) throws SQLException{
		
		ArrayList<String> list = new ArrayList();
		
		c.getAllEnvoirments(list);
		
		
		String [] tmp = new String[list.size()];
		
		for(int i = 0; i<list.size();i++){
			
			tmp[i] = list.get(i);
			
		}
	
	return tmp;
		
	}
	
	public void showPostingMethods(JPanel pan, Connection c, String inst_number, String test_system) throws SQLException{
		
		
		//Clearing panell
		pan.removeAll();
		
		
		//creating new list of checkboxes
		//checkboxList.clear();
		checkboxList = new ArrayList();
		
		

		pTariffs = c.getPostingMethodList(inst_number,test_system);
		
		
		//position in the grid
		int x_position = 0;
		int y_position = 0;
		
		
		//lable for posting tariff::
//		JLabel lblPostingTariff = new JLabel("Posting Tariffs:");
//		GridBagConstraints gbc_lblPostingTariff = new GridBagConstraints();
//		gbc_lblPostingTariff.insets = new Insets(0, 0, 0, 5);
//		gbc_lblPostingTariff.gridx = x_position;
//		gbc_lblPostingTariff.gridy = y_position;
//		panel_1.add(lblPostingTariff, gbc_lblPostingTariff);
//		y_position++;
		
		//Looping trough the list
		
			for (int i = 0; i<pTariffs.size()-1;i++){
				
				//adding appropriate place in the grid for the element
				if (x_position>6){
					x_position = 0;
					y_position++;
					
				}
				
				checkboxList.add(new JCheckBox(pTariffs.get(i).postingInstruction));
				
				GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
				gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 0, 5);
				gbc_chckbxNewCheckBox.gridx = x_position;
				gbc_chckbxNewCheckBox.gridy = y_position;
				panel_1.add(checkboxList.get(i), gbc_chckbxNewCheckBox);
				
				x_position++;
				
			}
		
		
		
		//repainting and making new checkboxes visible
		
		pan.repaint();
		pan.validate();

	}
}
class ItemChangeListener implements ItemListener{
    
    Gui g;
    public ItemChangeListener(Gui g){
    	this.g = g;
    	
    }
    @Override
    // When changing choice of the institution
    public void itemStateChanged(ItemEvent event) {
       if (event.getStateChange() == ItemEvent.SELECTED) {
          Object item = event.getItem();
          
          System.out.println(item.toString().substring(0, 8));
          
          String inst_number = item.toString().substring(0, 8);
          
          String test_system =  g.cb_testSystem.getSelectedItem().toString();
          
          g.btnSelectAll.setText("Select All");
          
          //JOptionPane.showMessageDialog(null, item.toString());
          
          // passing panel to modifie and connection object as well
          try {
        	  
			g.showPostingMethods(g.panel_1, g.c,inst_number,test_system);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          
          
          
       }
    }

}

class ItemChangeListenerTs implements ItemListener{
    
    Gui g;
    public ItemChangeListenerTs(Gui g){
    	this.g = g;
    	
    }
    @Override
    public void itemStateChanged(ItemEvent event) {
       if (event.getStateChange() == ItemEvent.SELECTED) {
          Object item = event.getItem();
          
          System.out.println(item.toString());
          
          String inst_number = g.cb_institution.getSelectedItem().toString().substring(0, 8);
          
          String test_system =  item.toString();
          
          g.btnSelectAll.setText("Select All");
          
          //JOptionPane.showMessageDialog(null, item.toString());
          
          // passing panel to modifie and connection object as well
          try {
        	  
        	  
			g.showPostingMethods(g.panel_1, g.c, inst_number, test_system);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          
          
          
       }
    }

}


