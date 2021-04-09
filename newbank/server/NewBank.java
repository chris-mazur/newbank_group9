package newbank.server;

import java.util.HashMap;
import java.util.regex.*;
import java.lang.Double;
import java.lang.Integer;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.Random;

public class NewBank {

	// bank instance
	private static final NewBank bank = new NewBank();

	// parameters set by the bank
	private static final int lenderLoanLimit = 3; // limits the number of loans a lender can create
	private static final double lenderLoanSizeLimit = 0.8; // limits the total size of loans relative to lender funds
	private static final int borrowerLoanLimit = 3; // limits the number of loans a borrower can accept
	private static final int borrowerLoanSizeLimit = 4; // limits the size of a loan relative to borrower funds
	private static final String sortCode = "07-16-18";
	private static int accountNumberCurrent;
	private static ArrayList<Integer> accountNumberList;

	// data structures for bank
	private HashMap<String,Customer> customers; // place to store all customer data
	private Calendar calendar = Calendar.getInstance(); // for time-dependent operations (e.g. interest)
	private HashMap<String, Loan> loanMarketPlace; // place to store loans before people take them

	private NewBank() {
		customers = new HashMap<>();
		loanMarketPlace = new HashMap<>();
		accountNumberList = new ArrayList<>();
		addTestData();
	}

	private void addTestData() {
		//initial starting account number
		accountNumberCurrent = 77771234;

		Customer bhagy = new Customer("bhagy1234");
		bhagy.addAccount(new SavingsAccount(sortCode, assignAccountNumber(), "Main", 1000.0));
		bhagy.setPassword("test1234");
		customers.put("Bhagy", bhagy);
		
		Customer christina = new Customer("christina5678");
		christina.addAccount(new SavingsAccount(sortCode, assignAccountNumber(), "Savings", 1500.0));
		christina.setPassword("test5678");
		customers.put("Christina", christina);
		
		Customer john = new Customer("john9999");
		john.addAccount(new SavingsAccount(sortCode, assignAccountNumber(), "Checking", 250.0));
		john.setPassword("test9999");
		customers.put("John", john);
	}
	
	public static NewBank getBank() {
		return bank;
	}

	public void setUserPassword(Customer c, String password) {
		c.setPassword(password);
	}

	public synchronized CustomerID checkLogInDetails(String userName, String password) {
		if(customers.containsKey(userName) && (customers.get(userName).getPassword().equals(password))) {
			return new CustomerID(userName);
		}
		return null;
	}

	public boolean usernameIsAvailable(String userName) {
		return !customers.containsKey(userName);
	}

	public CustomerID createNewCustomer(String userName, String password) {
		Customer c = new Customer(password);
		customers.put(userName, c);
		CustomerID id = new CustomerID(userName);
		// Create Main account upon new customer creation
		String[] defaultAccountRequest = {"NEWSAVINGSACCOUNT","Main"};
		bank.newSavingsAccount(id, defaultAccountRequest);
		return id;
	}

	// commands from the NewBank customer are processed in this method
	public synchronized String processRequest(CustomerID customer, String request) {

		String[] requestParams = request.split("\\s+");

		if(customers.containsKey(customer.getKey())) {
			switch(requestParams[0]) {
				case "HELP":
					return showHelp();
				case "SHOWMYACCOUNTS":
					return showMyAccounts(customer); // this should also show money lent and borrowed
				case "NEWSAVINGSACCOUNT":
					return newSavingsAccount(customer, requestParams);
				case "NEWCHECKINGACCOUNT":
					return newCheckingAccount(customer, requestParams);
				case "DEPOSIT":
					return depositFunds(customer, requestParams);
				case "MOVE":
					return transferFunds(customer, requestParams);
				case "PAY":
					return makePayment(customer, requestParams);
				case "LEND":
					return lendMoney(customer, requestParams);
				case "LOANS":
					return showLoans();
				case "BORROW":
					return borrowMoney(customer, requestParams); // loanID
				case "REPAY":
					return loanRepayment(customer, requestParams); // loanID, repayment amount
				case "TIMETRAVEL": // for testing purposes
					return timeTravel(requestParams);
				case "LOGOUT":
					return "LOGOUT";
				case "SHOWCONTACTDETAILS":
					return showContactDetails(customer, requestParams);
				case "CHANGEPOSTCODE":
					return changePostcode(customer, requestParams);
				case "CHANGEMYADDRESS":
					return changeAddress(customer, requestParams);
				case "CHANGEMYEMAIL":
					return changeEmail(customer, requestParams);
				case "CHANGEMYMOBILE":
					return changeMobilePhone(customer, requestParams);
				case "CHANGEMYLANDLINE":
					return changeLandlinePhone(customer, requestParams);
				default:
					return "Invalid input. Please try again or type 'HELP' for available options."; // TODO - should we rewrite this to 'Not a valid command, type in "HELP"...'?
			}
		}
		return "FAIL";
	}

	// displays information about all accounts and loans held by the customer
	private String showMyAccounts(CustomerID customerID) {
		Customer customer = customers.get(customerID.getKey());
		String accountData = "Accounts\n--------\n";
		accountData += customer.accountsToString();
		if (customer.numLoansOffered() > 0) {
			accountData += "\n-------------\nLoans Offered\n-------------" +
					customer.showLoansOffered(calendar.getTime());
		}
		if (customer.numLoansReceived() > 0) {
			accountData += "\n--------------\nLoans Received\n--------------" +
					customer.showLoansReceived(calendar.getTime());
		}
		return accountData;
	}
	
	private String showContactDetails(CustomerID customer, String[] requestParams) {
		if(requestParams.length > 1) return "Incorrect format.";
		String details = "";
		String address = customers.get(customer.getKey()).getAddress();
		String postcode = customers.get(customer.getKey()).getPostcode();
		String phone = customers.get(customer.getKey()).getPhoneNo();
		String landline = customers.get(customer.getKey()).getLandlinePhoneNo();
		String email = customers.get(customer.getKey()).getEmailAddress();
		
		if(address == null && postcode == null && phone == null && email == null && landline == null) {
			return "No contact details have been added yet.";
		}
		details += "Contact Details\n---------------";
		if(address!=null) {
			details += "\nAddress: " + address;
		}
		if(postcode!=null) {
			details += "\nPostcode: " + postcode;
		}
		if(phone!=null) {
			details += "\nMobile phone no: " + phone;
		}
		if(landline!=null) {
			details += "\nLandline phone no: " + landline;
		}
		if(email!=null) {
			details += "\nEmail address: " + email;
		}
		return details;
	}
	
	private String changeAddress(CustomerID customer, String[] requestParams) {	
		String address = "";
		if(requestParams.length > 1) {		
			for(int i=1;i<requestParams.length;i++) {
				address += requestParams[i];
				if(i!=requestParams.length-1) address += " ";
			}
			customers.get(customer.getKey()).setAddress(address);
			return "Address changed to " + address + ".";
		}
		return "Incorrect format.";
	}
	
	private String changePostcode(CustomerID customer, String[] requestParams) {
		// Regex based on assets.publishing.service.gov.uk
		String postcode;
		if(requestParams.length == 3) {
			postcode = requestParams[1] + " " + requestParams[2];	
			String regex = "^([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([AZa-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9]?[A-Za-z])))) [0-9][A-Za-z]{2})$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(postcode);
			if(matcher.matches()) {
				customers.get(customer.getKey()).setPostcode(postcode);
				return "Postcode changed to " + postcode + ".";
			}
		}
		return "Incorrect format.";
	}
	
	private String changeEmail(CustomerID customer, String[] requestParams) {
		if(requestParams.length == 2){
			// Regex based on https://www.regular-expressions.info/email.html
			String regex = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(requestParams[1]);
			if(matcher.matches()) {
				customers.get(customer.getKey()).setEmailAddress(requestParams[1]);
				return "Email address changed to " + requestParams[1] + ".";
			}
		}
		return "Incorrect format.";
	}
	
	private String changeMobilePhone(CustomerID customer, String[] requestParams) {	
		String phone = "";	
		if(requestParams.length > 1) {		
			for(int i=1;i<requestParams.length;i++) {
				phone += requestParams[i];
			}
		} else {
			return "Incorrect format.";
		}		
		// Regex based on https://www.regextester.com/104299
		String regex = "((\\+44(\\s\\(0\\)\\s|\\s0\\s|\\s)?)|0)7\\d{3}(\\s)?\\d{6}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(phone);
		if(matcher.matches()) {
			customers.get(customer.getKey()).setPhoneNo(phone);
			return "Phone number changed to " + phone + ".";
		}
		return "Incorrect format.";			
	}
	
	private String changeLandlinePhone(CustomerID customer, String[] requestParams) {	
		if(requestParams.length!=4) return "Incorrect format.";
		String phone = requestParams[1] + " " + requestParams[2] + " " + requestParams[3];	
		// Regex based on https://regexlib.com/
		String regex = "^((\\(?0\\d{4}\\)?\\s?\\d{3}\\s?\\d{3})|(\\(?0\\d{3}\\)?\\s?\\d{3}\\s?\\d{4})|(\\(?0\\d{2}\\)?\\s?\\d{4}\\s?\\d{4}))(\\s?\\#(\\d{4}|\\d{3}))?$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(phone);
		if(matcher.matches()) {
			customers.get(customer.getKey()).setLandlinePhoneNo(phone);
			return "Landline phone number changed to " + phone + ".";
		}
		return "Incorrect format.";			
	}

	private String newSavingsAccount(CustomerID customer, String[] requestParams) {
		if(requestParams.length == 2){
			if(isNumeric(requestParams[1])) {
				return "Account name cannot be a number. Try again";
			} else if (accountNameBlockList(requestParams[1])) {
				return "Account name is invalid. Try again";
			} else {
				String accountName = requestParams[1];
				customers.get(customer.getKey()).addAccount(new SavingsAccount(sortCode, assignAccountNumber(), accountName,0.00));
				return "Account created: " + accountName;
			}
		}
		return "Invalid entry. Try NEWSAVINGSACCOUNT <account name>";
	}

	private String newCheckingAccount(CustomerID customer, String[] requestParams) {
		if(requestParams.length == 2){
			if(isNumeric(requestParams[1])) {
				return "Account name cannot be a number. Try again";
			} else if (accountNameBlockList(requestParams[1])) {
				return "Account name is invalid. Try again";
			} else {
				String accountName = requestParams[1];
				customers.get(customer.getKey()).addAccount(new CheckingAccount(sortCode, assignAccountNumber(), accountName,0.00));
				return "Account created: " + accountName;
			}
		}
		return "Invalid entry. Try NEWCHECKINGACCOUNT <account name>";
	}

	//method to check if a String is Numeric. Useful when checking user input
	private boolean isNumeric(String string) {
		String regex = "-?\\d+(\\.\\d+)?";
		return Pattern.matches(regex, string);
	}

	private int assignAccountNumber() {
		Random r = new Random();
		//generate a random account number that is not in current list
		while (accountNumberList.contains(accountNumberCurrent)) {
			accountNumberCurrent = r.nextInt((79999999-70000000) + 1) + 70000000;
		}
		accountNumberList.add(accountNumberCurrent);
		System.out.println("Account number assigned: " + accountNumberCurrent);
		return accountNumberCurrent;
	}

	private boolean accountNameBlockList(String string) {
		String[] blockList = {"savings", "checking"};
		for (String s : blockList) {
			if (s.equals(string.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	// provides the user with an overview of all commands for interacting with the client
	private String showHelp() {
		// working draft to outline all possible commands (please update when necessary)
		return "Welcome to NewBank! Here is a list of commands you can use:\n" +
				"SHOWMYACCOUNTS - Displays a list of all bank accounts you currently have.\n" +
				"NEWSAVINGSACCOUNT - Creates a new Savings account; enter the command followed by the name " +
				"you would like to give to the account.\n" +
				"NEWCHECKINGACCOUNT - Creates a new Checking account; enter the command followed by the name " +
				"you would like to give to the account.\n" +
				"DEPOSIT - Adds funds to one of your accounts; enter the command followed by the balance to be " +
				"added, then the account name to deposit funds to.\n" +
				"MOVE - Moves funds between your accounts; enter the command followed by the balance to " +
				"be transferred, the account name to withdraw from, and the account name to deposit to.\n" +
				"PAY - Make a payment to another bank account; enter the command followed by the payment amount, " +
				"account to pay from, name of the payee, and the account name of the payee.\n" +
				"LEND - Lend money directly to other members of the bank; enter the command followed by the amount " +
				" to lend, the account to pay from, and the duration (in weeks) to make the money available for.\n" +
				"LOANS - Displays a list of all loans that are currently available.\n" +
				"BORROW - Apply for a loan; enter the command followed by the name of the loan and the name of the " +
				"account you would like the money to be paid into.\n" +
				"REPAY - Pay back money from a loan; enter the command followed by the amount to repay and the " +
				"name of the account you would like to make the payment from.\n" +
				"TIMETRAVEL - Skips ahead to a future date; enter the command followed by a number of days.\n" +
				"SHOWCONTACTDETAILS - shows all contact details.\n" +				
				"CHANGEMYADDRESS <NEW ADDRESS> - change your street address\n" +
				"CHANGEPOSTCODE <NEW POSTCODE> - change your postcode\n" +
				"CHANGEMYEMAIL <NEW EMAIL NO> - change your email address\n" +
				"CHANGEMYMOBILE <NEW PHONE NO> - change your mobile phone number in a format +44XXXXXXXXXX or 0XXXXXXXXXX\n" +
				"CHANGEMYLANDLINE <NEW PHONE NO> - change your landline phone number in a format 0XXXX XXX XXX\n" +
				"LOGOUT - Logs you out from the NewBank command line application.";
	}

	// deposits money into a specified account
	private String depositFunds(CustomerID customer, String[] requestParams) {
		// confirm that the correct number of parameters have been input
		if(requestParams.length == 3) {
			// confirm that input parameters are valid, and provide prompts to the user if not
			String userPrompts = "";
			double depositAmount = 0;
			try {
				depositAmount = Double.parseDouble(requestParams[1]);
				if(depositAmount <= 0) {
					// a deposit amount must be positive
					userPrompts += "\nDeposit amount '" + requestParams[1] + "' is not valid.";
				}
			} catch (NumberFormatException e) {
				userPrompts += "\nDeposit amount '" + requestParams[1] + "' is not valid.";
			}
			Account depositAccount = customers.get(customer.getKey()).getAccount(requestParams[2]);
			if(depositAccount == null) {
				userPrompts += "\nAccount for deposit '" + requestParams[2] + "' does not exist.";
			}
			if(userPrompts.length() > 0) {
				return "Deposit could not be made:" + userPrompts;
			}
			// deposit funds into the specified account
			depositAccount.depositFunds(depositAmount);
			return depositAmount + " deposited to " + depositAccount.getName() + "\n" + "Current balance in " +
					depositAccount.toString();
		} else {
			return "Invalid entry. Try DEPOSIT <amount> <account name>";
		}
	}

	// transfers money between two accounts belonging to the same customer
	private String transferFunds(CustomerID customer, String[] requestParams) {
		// confirm that the correct number of parameters have been input
		if(requestParams.length == 4) {
			// confirm that input parameters are valid, and provide prompts to the user if not
			String userPrompts = "";
			double transferAmount = 0;
			boolean inputsValid = true;
			try {
				transferAmount = Double.parseDouble(requestParams[1]);
				if(transferAmount <= 0) {
					// a transfer amount must be positive
					userPrompts += "\nTransfer amount '" + requestParams[1] + "' is not valid.";
					inputsValid = false;
				}
			} catch (NumberFormatException e) {
				userPrompts += "\nTransfer amount '" + requestParams[1] + "' is not valid.";
				inputsValid = false;
			}
			Account withdrawalAccount = customers.get(customer.getKey()).getAccount(requestParams[2]);
			if(withdrawalAccount == null) {
				userPrompts += "\nAccount for withdrawal '" + requestParams[2] + "' does not exist.";
				inputsValid = false;
			}
			Account depositAccount = customers.get(customer.getKey()).getAccount(requestParams[3]);
			if(depositAccount == null) {
				userPrompts += "\nAccount for deposit '" + requestParams[3] + "' does not exist.";
			}
			if(inputsValid && transferAmount > withdrawalAccount.getBalance()) {
				userPrompts += "\nInsufficient funds in " + withdrawalAccount.toString();
			}
			if(userPrompts.length() > 0) {
				return "Transfer could not be made:" + userPrompts;
			}
			// transfer funds between the specified accounts
			withdrawalAccount.withdrawFunds(transferAmount);
			depositAccount.depositFunds(transferAmount);
			
			return transferAmount + " transferred from " + withdrawalAccount.getName() + " to " +
					depositAccount.getName() + displayChangedAccounts(customer,withdrawalAccount,depositAccount);
		}
		return "Invalid entry. Try MOVE <amount> <account to withraw from> <account to deposit to>";
	}
	
	private String displayChangedAccounts(CustomerID customer, Account withdrawalAccount, Account depositAccount) {
		String response;
		response = "\nNew balances:\n";
		String accName = withdrawalAccount.getName();
		response += "Account name: " + accName + " Balance: " + customers.get(customer.getKey()).accountBalance(accName);
		accName = depositAccount.getName();
		response += "\nAccount name: " + accName + " Balance: " + customers.get(customer.getKey()).accountBalance(accName);
		return response;
	}

	// makes a payment to another customer in the same bank
	private String makePayment(CustomerID customer, String[] requestParams) {
		// confirm that the correct number of parameters have been input
		if(requestParams.length == 5) {
			// confirm that input parameters are valid, and provide prompts to the user if not
			String userPrompts = "";
			double paymentAmount = 0;
			boolean inputsValid = true;
			try {
				paymentAmount = Double.parseDouble(requestParams[1]);
				if(paymentAmount <= 0) {
					// a transfer amount must be positive
					userPrompts += "\nPayment amount '" + requestParams[1] + "' is not valid.";
					inputsValid = false;
				}
			} catch (NumberFormatException e) {
				userPrompts += "\nPayment amount '" + requestParams[1] + "' is not valid.";
				inputsValid = false;
			}
			Account withdrawalAccount = customers.get(customer.getKey()).getAccount(requestParams[2]);
			if(withdrawalAccount == null) {
				userPrompts += "\nAccount for withdrawal '" + requestParams[2] + "' does not exist.";
				inputsValid = false;
			} else if(!withdrawalAccount.canPay) {
				userPrompts += "\n'" + withdrawalAccount.getName() + "' account cannot perform payments to other customers.";
				inputsValid = false;
			}
			Customer payee = customers.get(requestParams[3]);
			if(payee == null) {
				// This doesn't work as expected (returns an infinite loop of "null")
				// TODO - fixing the invalid username/password issue that is on Trello might solve this problem
				userPrompts += "Payee '" + requestParams[3] + "' does not exist.\n";
			}
			Account payeeAccount = payee.getAccount(requestParams[4]);
			if (payeeAccount == null) {
				userPrompts += "\nPayee account, '" + requestParams[4] + "' does not exist.";
			}
			if(inputsValid && paymentAmount > withdrawalAccount.getBalance()) {
				userPrompts += "\nInsufficient funds in " + withdrawalAccount.toString();
			}
			if(userPrompts.length() > 0) {
				return "Payment could not be made:" + userPrompts;
			}
			// make payment
			withdrawalAccount.withdrawFunds(paymentAmount);
			payeeAccount.depositFunds(paymentAmount);
			return "Payment of " + paymentAmount + " successfully made.\n" +
					"Remaining balance in " + withdrawalAccount.toString();
		}
		return "Invalid entry. Try PAY <amount> <account to pay from> <payee name> <payee account>";
	}

	// set up a loan and add it to the loans marketplace
	private String lendMoney(CustomerID customerID, String[] requestParams) {
		Customer customer = customers.get(customerID.getKey());
		// confirm that the parameters entered are valid, and provide prompts to the user if not
		String userPrompts = "";
		double lendingAmount = 0;
		int lendingDuration = 0;
		boolean inputsValid = true;
		if (requestParams.length == 4) {
			try {
				lendingAmount = Double.parseDouble(requestParams[1]);
				if (lendingAmount <= 0) {
					// a lending amount must be positive
					userPrompts += "\nLending amount '" + requestParams[1] + "' is not valid.";
					inputsValid = false;
				}
			} catch (NumberFormatException e) {
				userPrompts += "\nLending amount '" + requestParams[1] + "' is not valid.";
				inputsValid = false;
			}
			Account lendingAccount = customer.getAccount(requestParams[2]);
			if (lendingAccount == null) {
				userPrompts += "\nAccount to lend from '" + requestParams[2] + "' does not exist.";
				inputsValid = false;
			} else if (!lendingAccount.canLoan) {
				userPrompts += "\n'" + lendingAccount.getName() + "' account cannot loan money to other customers.";
				inputsValid = false;
			} else if (lendingAccount.getBalance() < lendingAmount) {
				userPrompts += "\nInsufficient funds in " + lendingAccount;
				inputsValid = false;
			}
			try {
				lendingDuration = Integer.parseInt(requestParams[3]);
				if (lendingDuration <= 0) {
					// a lending duration must be positive
					userPrompts += "\nLending duration '" + requestParams[3] + "' is not valid.";
					inputsValid = false;
				}
			} catch (NumberFormatException e) {
				userPrompts += "\nLending duration '" + requestParams[3] + "' is not valid.";
				inputsValid = false;
			}
			// perform checks to ensure that what the customer is requesting is permitted by the bank
			if (inputsValid) {
				// check that the customer has no active loans to pay back
				if (customer.numLoansReceived() > 0) {
					userPrompts += "\nYou are not eligible to lend money while you have loans to pay back:\n" +
							customer.showLoansReceived(calendar.getTime());
					inputsValid = false;
				}
			}
			if (inputsValid) {
				// check that the customer is not trying to offer more loans than is permitted by the bank
				if (customer.numLoansOffered() == lenderLoanLimit) {
					userPrompts += "\nThe maximum number of loans you can offer is " + lenderLoanLimit + ". " +
							"Your current loans are:\n" + customer.showLoansOffered(calendar.getTime());
					inputsValid = false;
				}
				// check that the customer is not trying to lend more money than is permitted by the bank
				double customerLending = customer.getTotalLoansOffered();
				double customerLoanSizeLimit = lenderLoanSizeLimit * (customerLending + customer.getTotalFunds());
				if (customerLending + lendingAmount > customerLoanSizeLimit) {
					userPrompts += "\nThe maximum amount of money that you can lend is " + customerLoanSizeLimit +
							". The total value of loans you have already offered is " + customerLending + ".";
					inputsValid = false;
				}
			}
			// set up the loan if all of the criteria are met
			if (inputsValid) {
				// create a new loan
				Loan newLoan = new Loan(lendingAccount, lendingAmount, lendingDuration, calendar.getTime());
				// add loan to customer account
				customer.offerLoan(newLoan);
				// add loan to marketplace
				loanMarketPlace.put(newLoan.getLoanID(), newLoan);
				// confirm that loan has been set up
				return "The following loan has been set up:\n" + newLoan.displayDetails();
			}
			return "Loan could not be set up:" + userPrompts;
		}
		return "Invalid entry. Try LEND <amount to lend> <account to lend from> <duration to lend for (weeks)>";
	}

	// shows all loans available at the bank
	private String showLoans() {
		String loanList = "Loans that are currently available\n----------------------------------";
		for (Loan loan : loanMarketPlace.values()) {
			loanList += "\n" + loan.displayDetails();
		}
		if (loanList.length() == 0) {
			return "No loans currently available.";
		} else {
			return loanList;
		}
	}

	// allows a customer to take out a loan
	private String borrowMoney(CustomerID customerID, String[] requestParams) {
		Customer customer = customers.get(customerID.getKey());
		// confirm that the parameters are valid, and provide prompts to the user if not
		String userPrompts = "";
		boolean inputsValid = true;
		if (requestParams.length == 3) {
			if (!loanMarketPlace.containsKey(requestParams[1])) {
				userPrompts += "\nLoan ID '" + requestParams[1] + "' is not valid.";
				inputsValid = false;
			}
			Account borrowingAccount = customer.getAccount(requestParams[2]);
			if (borrowingAccount == null) {
				userPrompts += "\nAccount to pay loan into '" + requestParams[2] + "' does not exist.";
				inputsValid = false;
			}
			if (inputsValid) {
				// perform eligibility checks for the loan, and provide prompts to the user if criteria are not met
				Loan loan = loanMarketPlace.get(requestParams[1]);
				boolean eligibleForLoan = true;
				// perform eligibility checks
				if (loan.getLoanValue() > (customer.getTotalFunds() * borrowerLoanSizeLimit)) {
					userPrompts += "\nThe maximum size of loan you are eligible for is " +
							(borrowerLoanSizeLimit * customer.getTotalFunds()) + " (" +
							borrowerLoanSizeLimit + " times the total amount of money held in your accounts).";
					eligibleForLoan = false;
				}
				if (customer.numLoansReceived() == borrowerLoanLimit) {
					userPrompts += "\nThe maximum number of loans you can get is " + borrowerLoanLimit + ". " +
							"Your current loans are:" + customer.showLoansReceived(calendar.getTime());
					eligibleForLoan = false;
				}
				if (eligibleForLoan) {
					// accept loan and transfer funds to the borrowing account
					loan.acceptLoan(borrowingAccount, calendar.getTime());
					// add loan to customer account
					customer.receiveLoan(loan);
					// remove loan from marketplace
					loanMarketPlace.remove(loan.getLoanID());
					// confirm that loan has been successfully received
					return "The following loan has been received:\n" + loan.displayDetails();
				} else {
					return "You are not eligible for this loan:" + userPrompts;
				}
			}
			return "Unable to take out loan:" + userPrompts;
		}
		return "Invalid entry. Try BORROW <loan ID> <account to pay into>";
	}

	// allows a customer to make a repayment on their loan
	private String loanRepayment(CustomerID customerID, String[] requestParams) {
		Customer customer = customers.get(customerID.getKey());
		// confirm that the parameters entered are valid, and provide prompts to the user if not
		String userPrompts = "";
		double repaymentAmount = 0;
		boolean inputsValid = true;
		if (requestParams.length == 4) {
			Loan loanToRepay = customer.getLoan(requestParams[1]);
			if (loanToRepay == null) {
				userPrompts += "\nLoan ID '" + requestParams[1] + "' is not valid.";
				inputsValid = false;
			}
			try {
				repaymentAmount = Double.parseDouble(requestParams[2]);
				if (repaymentAmount <= 0) {
					userPrompts += "\nRepayment amount '" + requestParams[2] + "' must be positive.";
					inputsValid = false;
				}
			} catch (NumberFormatException e) {
				userPrompts += "\nRepayment amount '" + requestParams[2] + "' is not valid.";
				inputsValid = false;
			}
			Account repaymentAccount = customer.getAccount(requestParams[3]);
			if (repaymentAccount == null) {
				userPrompts += "\nAccount to repay from '" + requestParams[3] + "' does not exist.";
				inputsValid = false;
			} else if(!repaymentAccount.canPay) {
				userPrompts += "\n'" + repaymentAccount.getName() + "' account cannot be used to perform loan repayments.";
				inputsValid = false;
			}
			if (inputsValid) {
				// check that there are sufficient funds to make the repayment
				if (repaymentAccount.getBalance() < repaymentAmount) {
					userPrompts += "\nInsufficient funds to make repayment from " + repaymentAccount.toString();
					inputsValid = false;
				}
				// check that the repayment does not exceed the remaining balance on the loan
				double outstandingBalance = loanToRepay.getRepaymentAmount(calendar.getTime());
				if (repaymentAmount > outstandingBalance) {
					userPrompts += "\nRepayment exceeds outstanding balance on loan: " + outstandingBalance;
					inputsValid = false;
				}
				if (inputsValid) {
					// make the repayment
					loanToRepay.makeRepayment(calendar.getTime(), repaymentAmount, repaymentAccount);
					outstandingBalance = loanToRepay.getRepaymentAmount(calendar.getTime());
					return "Repayment of " + repaymentAmount + " made to " + loanToRepay.getLoanID() + ". " +
							"Outstanding balance is now " + outstandingBalance;
				}
				return "Unable to make repayment on loan:" + userPrompts;
			}
			return "Unable to make repayment on loan:" + userPrompts;
		}
		return "Invalid entry. Try REPAY <Loan ID> <amount to repay> <account to pay from>";
	}

	// skips ahead by a specified number of days to a future date in the bank's calendar
	private String timeTravel(String[] requestParams) {
		// confirm that the parameter entered is valid (must be a positive integer)
		if(requestParams.length == 2 && isNumeric(requestParams[1])) {
			try {
				int days = Integer.parseInt(requestParams[1]);
				if(days >= 0) {
					// update the calendar date and provide confirmation of time travel
					Date departureDate = calendar.getTime();
					calendar.add(calendar.DATE, days);
					Date arrivalDate = calendar.getTime();
					return "Travelled forward " + days + " days from " + departureDate + " to " + arrivalDate;

				} else {
					return "Invalid entry. The number of days must be positive.";
				}
			} catch (NumberFormatException e) {
				return "Invalid entry. The number of days must be an integer.";
			}
		}
		return "Invalid entry. Try TIMETRAVEL <number of days into the future>";
	}

}
