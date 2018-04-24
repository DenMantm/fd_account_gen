import java.sql.*;
import java.util.ArrayList;


import oracle.jdbc.OracleTypes;

public class Connection {
	private final String DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
	private final String DB_CONNECTION = "jdbc:oracle:thin:@172.21.64.72:1521:SYSIMP";
	public final String DB_USER;// = "simp_dstrods";
	private final String DB_PASSWORD;// = "diamond1987";
	User u = null;
	
	
	//This is workaround to get woring index incrementation, not a perfect solution...
	public String account_number = "0";
	public String group_number = "0";
	
		
		//getting user details here
	    public Connection(User u){

	    	//decripting stored encripted password
	    	// StrongAES enc = new StrongAES();
		    // u.username = enc.decript(u.username);
		    // u.password = enc.decript(u.password);
		     
	    	
	    	DB_USER = u.username;
	    	DB_PASSWORD = u.password;
	    	
	    }
	    
	    public void testConnection(){
	    	
	    	//just a test connection
	    	java.sql.Connection dbConnection = null;
			String sql = "Select table_name from all_tables"+
			" where table_name like 'CHT_%' and table_name"+
			" not in('CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN') order by 1";

			try {
				dbConnection = getDBConnection();
				
				  Statement stmt = dbConnection.createStatement();
				  ResultSet rows = stmt.executeQuery(sql);
				  
//				while (rows.next()) {
//
//				}
			
			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (dbConnection != null) {
					try {
						dbConnection.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
			}
	    	
	    }
		

	    
	    	//Getting next avalible values here:::
	    
	    public String[] getNextAvalibleIndexes(String inst_number, String db_name) throws SQLException{
			// except 'CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN'
			
			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;
			
			String[] indexes = new String[2];
			
			
			String getDBUSERCursorSql = "{call sysimp_util.SIMP_ACCOUNT_GENERATION.GET_NEXT_ACCOUNT_INDEX(?,?,?,?)}";
			
			try {
			dbConnection = getDBConnection();
			callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);
			
			//DEBUG
			System.out.println(inst_number + " - " + db_name);
			
			//defining return type of the out
			callableStatement.registerOutParameter(1, OracleTypes.VARCHAR);
			callableStatement.registerOutParameter(2, OracleTypes.VARCHAR);
			callableStatement.setString(3, inst_number);
			callableStatement.setString(4, db_name);
			
			
			//this one is for condition when
			
			
			
			// execute getDBUSERCursor store procedure
			callableStatement.executeUpdate();
			
			// get cursor and cast it to ResultSet
			
			System.out.println(callableStatement.getString(1));
			
			//Getting start indexes
			indexes[0] = callableStatement.getString(1);
			indexes[1] = callableStatement.getString(2);


} catch (SQLException e) {

System.out.println(e.getMessage());

} finally {

if (rs != null) {
rs.close();
}

if (callableStatement != null) {
callableStatement.close();
}

if (dbConnection != null) {
dbConnection.close();
}

}

return indexes;
}
		
		
	    
	  
	    public ArrayList<Account> getAccountsFromPostingInst(String posting_method,
	    										String inst_number,
	    										String db_name) throws SQLException{
			// except 'CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN'
			
			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;
			ArrayList<Account> list = new ArrayList();
			
			
			String getDBUSERCursorSql = "{call sysimp_util.SIMP_ACCOUNT_GENERATION.GET_ACCOUNT_LIST_DB_LINK(?,?,?,?)}";

			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);

				
				//defining return type of the out
				callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
				callableStatement.setString(2, posting_method);
				callableStatement.setString(3, inst_number);
				callableStatement.setString(4, db_name);

				// execute getDBUSERCursor store procedure
				callableStatement.executeUpdate();

				// get cursor and cast it to ResultSet
				rs = (ResultSet) callableStatement.getObject(1);
				
				

				while (rs.next()) {
					
					String type = rs.getString("type");
					String currency = rs.getString("currency");
					String type_index = rs.getString("type_index");
					String currency_index = rs.getString("currency_index");
					String exists = rs.getString("exists");
					
					
					String isOnDemand = rs.getString("isOnDemand");
					boolean isCreateOnDemand = false;
					
					//checking if account is mandatory or not:: (Some very unapropriate logic here...)
					if(isOnDemand.equals("000")){
						
						String[] tmp = new String[2];
						isCreateOnDemand = true;
						String l_account_number,r_account_number;
						
						if(account_number.equals("0") && group_number.equals("0")){
							
							
							tmp = getNextAvalibleIndexes(inst_number, db_name);
							
							
							account_number = tmp[0];
							group_number = tmp[1];
							
							l_account_number = account_number.substring(0, 8);
							r_account_number = account_number.substring(8, 11);
							
							r_account_number = String.format("%3d", Integer.parseInt(r_account_number)+1).replace(' ', '0');
							
							account_number = l_account_number + r_account_number;
							
							//For group::
							group_number = String.format("%8d", Integer.parseInt(group_number)+1).replace(' ', '0');
							
							System.out.println(account_number + " - " + group_number);
							
							
							
						}
						else{
							//logic to increment
							//No Action is required...
							
							l_account_number = account_number.substring(0, 8);
							r_account_number = account_number.substring(8, 11);
							
							r_account_number = String.format("%3d", Integer.parseInt(r_account_number)+1).replace(' ', '0');
							
							account_number = l_account_number + r_account_number;
							
							//For group::
							group_number = String.format("%8d", Integer.parseInt(group_number)+1).replace(' ', '0');
							
							System.out.println(account_number + " - " + group_number);

						}
						
						
						
						
					}
					
					
					Account tmp = new Account(type_index,currency_index,type,currency,exists,account_number,group_number,isCreateOnDemand);
					
					
					list.add(tmp);
				}
				
				//Resetting account number values
				account_number = "0";
				group_number = "0";
				
			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}
			
			return list;
	    }
	    
	    
	    public String getInsertsFromAccounts(Account a, String inst_number, String db_name) throws SQLException{
									// except 'CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN'
	    							
	    	
									java.sql.Connection dbConnection = null;
									CallableStatement callableStatement = null;
									ResultSet rs = null;
									
									String insert = null;
									
									
									String getDBUSERCursorSql = "{call sysimp_util.SIMP_ACCOUNT_GENERATION.GET_ACCOUNT_INSERTS(?,?,?,?,?,?,?)}";
									
									try {
									dbConnection = getDBConnection();
									callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);
									
									
									//defining return type of the out
									callableStatement.registerOutParameter(1, OracleTypes.VARCHAR);
									callableStatement.setString(2, a.type_index);
									callableStatement.setString(3, a.currency_index);
									callableStatement.setString(4, a.account_number);
									callableStatement.setString(5, a.group_number);
									callableStatement.setString(6, inst_number);
									callableStatement.setString(7, db_name);
									
									
									//this one is for condition when
									
									
									
									// execute getDBUSERCursor store procedure
									callableStatement.executeUpdate();
									
									// get cursor and cast it to ResultSet
									
									System.out.println(insert);
									
									
									insert = callableStatement.getString(1);
						
						
						} catch (SQLException e) {
						
						System.out.println(e.getMessage());
						
						} finally {
						
						if (rs != null) {
						rs.close();
						}
						
						if (callableStatement != null) {
						callableStatement.close();
						}
						
						if (dbConnection != null) {
						dbConnection.close();
						}
						
						}
						
						return insert;
						}
	    
	    
	    
	    
	    
	    
		public ArrayList<Institution> getInstitutionList(String db_name) throws SQLException{
						// except 'CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN'
						
						java.sql.Connection dbConnection = null;
						CallableStatement callableStatement = null;
						ResultSet rs = null;
						ArrayList<Institution> list = new ArrayList();
						
						
						String getDBUSERCursorSql = "{call sysimp_util.SIMP_ACCOUNT_GENERATION.GET_INSTITUTION_LIST(?,?)}";
						
						try {
								dbConnection = getDBConnection();
								callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);
								
								
								//defining return type of the out
								callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
								callableStatement.setString(2, db_name);
								
								// execute getDBUSERCursor store procedure
								callableStatement.executeUpdate();
								
								// get cursor and cast it to ResultSet
								rs = (ResultSet) callableStatement.getObject(1);
						
						
						while (rs.next()) {
						
									String name = rs.getString("institution_name");
									String number = rs.getString("institution_number");
									
									
									Institution tmp = new Institution(name, number);
									
									
									list.add(tmp);
						}
						
						
						} catch (SQLException e) {
						
						System.out.println(e.getMessage());
						
						} finally {
						
						if (rs != null) {
						rs.close();
						}
						
						if (callableStatement != null) {
						callableStatement.close();
						}
						
						if (dbConnection != null) {
						dbConnection.close();
						}
						
						}
						
						return list;
						}
							    		
	    
	    
	    
	    
		public void getAllEnvoirments(ArrayList<String> list) throws SQLException{
			// except 'CHT_CHARGEBACK_REASON','CHT_FRAUD_RESPONSE_CODE','CHT_CURRENCY','CHT_VISA_SOURCE_BIN'
			
			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;
			
			
			String getDBUSERCursorSql = "{call sysimp_util.SIMP_INDEXING_DATA.GET_INDEX_ENVIRONMENTS(?)}";

			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);

				
				//defining return type of the out
				callableStatement.registerOutParameter(1, OracleTypes.CURSOR);

				// execute getDBUSERCursor store procedure
				callableStatement.executeUpdate();

				// get cursor and cast it to ResultSet
				rs = (ResultSet) callableStatement.getObject(1);
				
				
				while (rs.next()) {

					list.add(rs.getString("name"));
				}
				
			

			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}
			
			
			
			

//			String sql = "select * from SYSIMP_UTIL.CHT_INDEXING_ENVIRONMENTS order by 2";
//
//			try {
//				dbConnection = getDBConnection();
//				
//				  Statement stmt = dbConnection.createStatement();
//				  ResultSet rows = stmt.executeQuery(sql);
//				  
//				  
//				while (rows.next()) {
//					
//					list.add(rows.getString("name"));
//					System.out.println(rows.getString("name"));
//
//				}
//			
//			} catch (SQLException e) {
//
//				System.out.println(e.getMessage());
//
//			} finally {
//
//				if (dbConnection != null) {
//					try {
//						dbConnection.close();
//					} catch (SQLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			
//			}
//			
//			
		}
		
		
		
		public ArrayList<PostingTariff> getPostingMethodList(String inst_number,String db_instance)
				throws SQLException {

			java.sql.Connection dbConnection = null;
			CallableStatement callableStatement = null;
			ResultSet rs = null;
			String getDBUSERCursorSql = "{call sysimp_util.SIMP_ACCOUNT_GENERATION.GET_POSTING_METHOD_DB_LINK(?,?,?)}";
			String[] postingMethods = null;
			
			//Creqating list container to hold al lthe results
			ArrayList<PostingTariff> list = new ArrayList();
			
			
			try {
				dbConnection = getDBConnection();
				callableStatement = dbConnection.prepareCall(getDBUSERCursorSql);

				
				//defining return type of the out
				callableStatement.registerOutParameter(1, OracleTypes.CURSOR );
				callableStatement.setString(2, inst_number);
				callableStatement.setString(3, db_instance);

				// execute getDBUSERCursor store procedure
				callableStatement.executeQuery();
				
				rs = (ResultSet) callableStatement.getObject(1);
				

				
				while (rs.next()) {
					
					 String index = rs.getString("index");
					 String postingTariff = rs.getString("method");
					 
					
					PostingTariff tmp = new PostingTariff(index, postingTariff);
					
					list.add(tmp);
					
				}
				
				
				



			} catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (rs != null) {
					rs.close();
				}

				if (callableStatement != null) {
					callableStatement.close();
				}

				if (dbConnection != null) {
					dbConnection.close();
				}

			}
			
			return list;

		}
		
		
		public java.sql.Connection getDBConnection() {

			java.sql.Connection dbConnection = null;

			try {

				Class.forName(DB_DRIVER);

			} catch (ClassNotFoundException e) {

				System.out.println(e.getMessage());

			}

			try {

				dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,DB_PASSWORD);
				
				return dbConnection;

			} catch (SQLException e) {

				System.out.println(e.getMessage());

			}

			return dbConnection;

}
		
		
}
