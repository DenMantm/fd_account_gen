import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class App {
	
	//Version History
	//V0.01 - Initial start of the development
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		
		//connecting to the database
		User u = new User("sysimp_util","sysimp");
		Connection c = new Connection(u);
		
		
		ArrayList<String> list = new ArrayList();
		
			c.getAllEnvoirments(list);

		
		//test system list
		System.out.println("Avalable Test Systems:");
		System.out.println(list.toString());
		

			ArrayList<PostingTariff> instr = c.getPostingMethodList("00000019","SYSIMP");
			
			System.out.println(instr.get(1).postingInstruction);



			ArrayList<Account> accts = c.getAccountsFromPostingInst("154", "00000019", "SYSIMP");
			
			System.out.println(accts.get(1).type);
			
			
			ArrayList<Institution> inst = c.getInstitutionList("SYSIMP");
			
			
			System.out.println(inst.get(0).number);

			
			Gui g = new Gui();
			
			g.setVisible(true);

		
		//Getting list of posting methods for the institution
		
		
		
	}

}
