
public class Account {
	
	public String type,currency,exists,type_index,currency_index,account_number,group_number;
	public boolean isCreateOnDemand;
	
	public Account(String type_index, String currency_index,String type, String currency,String exists,String account_number,String group_number,boolean isCreateOnDemand){
		this.type = type;
		this.currency =currency;
		this.exists = exists;
		this.type_index = type_index;
		this.currency_index =currency_index;
		this.account_number = account_number;
		this.group_number = group_number;
		this.isCreateOnDemand = isCreateOnDemand;
	}
}
