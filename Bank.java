/**
 * Represents a bank system.
 * @author Colby Tse
 * @version 1.0
 * @since 1.0
 */

import java.io.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.*;

public class Bank {

	public static final String COMMANDS =
		"""
		HELP: Outputs this help string
		LOGIN: Log in using valid ID and password
		LOGOUT: Log out of current user
		REGISTER: Register for an account using valid ID and password
		CHANGE PASSWORD: Allows current user to change password
		WITHDRAW: Withdraws a valid amount from account
		DEPOSIT: Deposits a valid amount to account
		TRANSFER: Transfers a valid amount to another account
		EXIT: Ends the banking process
		RESET: Clears all data in banking system (Admin only)""";

	//------------------------------------------------------------------------------------------------------------------
	// Properties
	//------------------------------------------------------------------------------------------------------------------

	private Account currentSession;
	private ArrayList<Account> accounts;
	private String sysMsg;

	//------------------------------------------------------------------------------------------------------------------
	// Constructors
	//------------------------------------------------------------------------------------------------------------------

	public Bank() throws Exception {
		this.currentSession = null;
		this.accounts = new ArrayList<Account>();
		String accountsCSV = "accounts.csv";
		this.loadAccountData(accountsCSV);
		this.sysMsg = "";
	}

	//------------------------------------------------------------------------------------------------------------------
	// Accessors and Mutators
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Gets the account currently logged in.
	 * @return An Account that is currently logged in.
	 */
	public Account getCurrentSession() {
		return this.currentSession;
	}

	/**
	 * Sets the account currently logged in.
	 * @param account An Account to be logged into.
	 */
	public void setCurrentSession(Account account) {
		this.currentSession = account;
	}

	/**
	 * Gets a list of accounts registered with the bank.
	 * @return An ArrayList representing the accounts registered with the bank.
	 */
	public ArrayList<Account> getAccounts() {
		return this.accounts;
	}

	/**
	 * Sets the list of accounts registered with the bank.
	 * @param accounts An ArrayList containing the list of accounts registered with the bank.
	 */
	public void setAccounts(ArrayList<Account> accounts) {
		this.accounts = accounts;
	}

	/**
	 * Gets the account with the given ID.
	 * @param id A String containing the ID of an account.
	 * @return An Account with the given ID.
	 */
	public Account getAccount(String id) {
		for (Account account : this.getAccounts()) {
			if (account.getID().equals(id)) {
				return account;
			}
		}
		return null;
	}

	/**
	 * Gets the system message which contains messages notifying the user of a successful and invalid operation.
	 * @return A String representing the system message.
	 */
	public String getSysMsg() {
		return this.sysMsg;
	}

	/**
	 * Sets the system message.
	 * @param sysMsg A String containing the system message.
	 */
	public void setSysMsg(String sysMsg) {
		this.sysMsg = sysMsg;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Commands Functions
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Process user input and perform operation commanded.
	 * @param command A String containing the operation to perform 
	 */
	public void processCommand(String command) throws Exception {
		clearTerminal();
		switch (command.toUpperCase()) {
			case "HELP":
				this.setSysMsg(COMMANDS);
				break;
			case "LOGIN":
				this.login();
				break;
			case "LOGOUT":
				this.setCurrentSession(null);
				break;
			case "REGISTER":
				this.register();
				break;
			case "CHANGE PASSWORD":
				this.changePw();
				break;
			case "WITHDRAW":
				this.transaction("withdraw");
				break;
			case "DEPOSIT":
				this.transaction("deposit");
				break;
			case "TRANSFER":
				this.transfer();
				break;
			case "EXIT":
				saveAccountData("accounts.csv");
				System.exit(0);
			case "RESET":
				this.reset();
				break;
			default:
				this.setSysMsg("Please enter a valid command.");
				break;
		}
	}

	/**
	 * Prompts the user to login using a valid ID and password.
	 */
	public void login() throws Exception {
		// Check if logged in
		if (this.getCurrentSession() != null) {
			this.setSysMsg("Already logged in.");
			return;
		}

		// Create the console object
		Console console = System.console();
		if (console == null) {
			System.out.println("No console available.");
			System.exit(0);
		}
		
		// Get ID and password
		String id = console.readLine("Enter ID: ");
		if (id.equals("")) {
			this.setSysMsg("Login cancelled.");
			return;
		}
		char[] ch = console.readPassword("Enter password: ");
		String pw = String.valueOf(ch);
		if (pw.equals("")) {
			this.setSysMsg("Login cancelled.");
			return;
		}
		
		// Search account database to find matching account
		for (Account account : this.getAccounts()) {
			if (account.getID().equals(id) && account.getDecryptedPw().equals(pw)) {
				this.setCurrentSession(account);
				break;
			}
		}
		
		// Update system message
		if (this.getCurrentSession() == null) {
			this.setSysMsg("Login failed.");
		} else {
			this.setSysMsg("Login successful.");
		}
	}

	/**
	 * Prompts the user to register using a valid ID and password.
	 */
	public void register() throws Exception {
		// Create the console object
		Console console = System.console();
		if (console == null) {
			System.out.println("No console available.");
			System.exit(0);
		}
  
        // Get ID and check if valid
        String id = console.readLine("Enter a unique ID: ");
        while (id.matches(".*\\s+.*") || id.matches("^.*[^a-zA-Z0-9 ].*$")) {
        	clearTerminal();
        	id = console.readLine("Invalid ID. Please enter a valid and unique ID: ");
        }
        while (!this.isUniqueID(id)) {
        	clearTerminal();
        	id = console.readLine("ID taken. Please enter a unique ID: ");
        }
        if (id.equals("")) {
        	this.setSysMsg("Registration cancelled.");
        	return;
        }
  
        // Get password and check if valid
        char[] ch = console.readPassword("Enter password: ");
        String pw = String.valueOf(ch);
        while (pw.matches(".*\\s+.*") ||
        	pw.matches("^.*[^a-zA-Z0-9 ].*$")) {
        	clearTerminal();
        	pw = console.readLine("Invalid password. Please enter a valid password: ");
        }
        if (pw.equals("")) {
        	this.setSysMsg("Registration cancelled.");
        	return;
        }

        // Create and add account to database
        this.getAccounts().add(new Account(id, pw, 0));
        this.setSysMsg("Registration successful.");
	}

	/**
	 * Prompts the current session's user to change their password.
	 */
	public void changePw() throws Exception {
		// Check if current session exists
        if (this.getCurrentSession() == null) {
			this.setSysMsg(String.format("You must login to change your password."));
			return;
		}

		// Create the console object
        Console console = System.console();
        if (console == null) {
            System.out.println("No console available.");
            System.exit(0);
        }

        // Prompt old password to authenticate user
        char[] ch = console.readPassword("Enter old password: ");
        String pw = String.valueOf(ch);
        if (!this.getCurrentSession().getDecryptedPw().equals(pw)) {
        	this.setSysMsg("Wrong password. Operation cancelled.");
        	return;
        }

        // Get new password and check if valid
        ch = console.readPassword("Enter new password: ");
        pw = String.valueOf(ch);
        while (pw.matches(".*\\s+.*") || pw.matches("^.*[^a-zA-Z0-9 ].*$")) {
        	clearTerminal();
        	pw = console.readLine("Invalid password. Please enter a valid password: ");
        }
        if (pw.equals("")) {
        	this.setSysMsg("Operation cancelled.");
        	return;
        }

        // Set new password
        this.getCurrentSession().setPw(pw);
	}

	/**
	 * Prompts the current session's user to perform a deposit or withdrawal.
	 * @param type A String containing the transaction type.
	 */
	public void transaction(String type) throws Exception {
		// Check if current session exists
		if (this.getCurrentSession() == null) {
			this.setSysMsg(String.format("You must login to %s.", type));
			return;
		}

		// Create the console object
        Console console = System.console();
        if (console == null) {
            System.out.println("No console available.");
            return;
        }

		// Get amount to withdraw or deposit and check if valid
		String raw = console.readLine(String.format("Enter amount to %s: ", type));
		double amount;
		try {
			amount = Double.valueOf(raw);
			if (amount < 0) {
				throw new NumberFormatException();
			} else if (type.equals("withdraw") && this.getCurrentSession().getBalance() - amount < 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			clearTerminal();
			this.setSysMsg(String.format("Invalid %s amount.", type));
			return;
		}

		// Prompt password to authenticate user
        char[] ch = console.readPassword("Enter password to confirm transaction: ");
        String pw = String.valueOf(ch);
        if (!this.getCurrentSession().getDecryptedPw().equals(pw)) {
        	this.setSysMsg("Wrong password. Transfer cancelled.");
        	return;
        }

		// Update balance
		if (type.equals("withdraw")) {
			this.getCurrentSession().setBalance(this.getCurrentSession().getBalance() - amount);
		} else {
			this.getCurrentSession().setBalance(this.getCurrentSession().getBalance() + amount);
		}

		// Capitalise the first letter for system message
		type = Character.toUpperCase(type.charAt(0)) + type.substring(1, type.length());

		// Update system message with success message
		this.setSysMsg(String.format("%s successful.", type));
	}

	/**
	 * Prompts the current session's user to transfer some amount to another account.
	 */
	public void transfer() throws Exception {
		// Check if current session exists
		if (this.getCurrentSession() == null) {
			this.setSysMsg("You must login to transfer.");
			return;
		}

		// Create the console object
		Console console = System.console();
		if (console == null) {
            System.out.println("No console available.");
            return;
        }

		// Get account to transfer to
		Account recipient = this.getAccount(
			console.readLine("Enter ID to transfer to: "));
		if (recipient == null) {
			this.setSysMsg("Invalid ID.");
			return;
		}

		// Get amount to transfer and check if valid
		String raw = console.readLine("Enter amount to transfer: ");
		double amount;
		try {
			amount = Double.valueOf(raw);
			if (amount < 0 || this.getCurrentSession().getBalance() - amount < 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			clearTerminal();
			this.setSysMsg("Invalid transfer amount.");
			return;
		}

		// Prompt password to authenticate user
        char[] ch = console.readPassword("Enter password to confirm transaction: ");
        String pw = String.valueOf(ch);
        if (!this.getCurrentSession().getDecryptedPw().equals(pw)) {
        	this.setSysMsg("Wrong password. Transfer cancelled.");
        	return;
        }

		// Update balances
		this.getCurrentSession().setBalance(this.getCurrentSession().getBalance() - amount);
		recipient.setBalance(recipient.getBalance() + amount);

		// Update system message with success message
		this.setSysMsg("Transfer successful.");
	}

	/**
	 * Clears all data in banking system.
	 */
	public void reset() throws Exception {
		// Check if admin account
		if (this.getCurrentSession() == null) {
			setSysMsg("Only admin can perform a reset.");
			return;
		} else if (!this.getCurrentSession().getID().equals("admin")) {
			setSysMsg("Only admin can perform a reset.");
			return;
		}

		// Create the console object
        Console console = System.console();
        if (console == null) {
            System.out.println("No console available.");
            return;
        }

		// Prompt admin to enter password
        String confirm = console.readLine("Enter Y to confirm reset: ");
        if (!confirm.equals("Y")) {
        	this.setSysMsg("Reset cancelled.");
        	return;
        }

        // Prompt admin to confirm reset by entering password again
        char[] ch = console.readPassword("Enter password to reset ALL data: ");
        String pw = String.valueOf(ch);
        if (!this.getCurrentSession().getDecryptedPw().equals(pw)) {
        	this.setSysMsg("Wrong password. Reset cancelled.");
        	return;
        }

	// Assign new ArrayList of accounts containing admin account
	ArrayList<Account> resetted = new ArrayList<Account>();
	resetted.add(this.getCurrentSession());
	this.setAccounts(resetted);

        // Save changes to file
        this.saveAccountData("accounts.csv");

	// Update system message with success message
	this.setSysMsg("Reset successful.");
	}

	//------------------------------------------------------------------------------------------------------------------
	// Helper Functions
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Read and load account data from file from given pathname.
	 * @param pathname The file path of file containing account data.
	 */
	public void loadAccountData(String pathname) throws Exception {
		try {
			Scanner sc = new Scanner(new File(pathname));
			sc.useDelimiter("\n");

			sc.next(); // Skip column headings
			while (sc.hasNext()) {
				String[] info = sc.next().split(",");

				// Decode the base64 encoded encrypted password
				byte[] encryptedPw = Base64.getDecoder().decode(info[1]);

				// Decode the base64 encoded key
				byte[] decodedKey = Base64.getDecoder().decode(info[2]);

				// Rebuild key using SecretKeySpec
				SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

				// Add account to this bank's account database
				this.getAccounts().add(new Account(info[0], encryptedPw, key, Double.valueOf(info[3])));
			}
			sc.close();
		} catch (Exception e) {
			System.out.println("Failed to load data.");
			System.exit(0);
		}
	}

	/**
	 * Save account data to file at given pathname.
	 * @param pathname The file path of file to save account data to.
	 */
	public void saveAccountData(String pathname) throws Exception {
		try {
			FileWriter fw = new FileWriter(pathname);

			// Write headings of columns to file
			fw.append("id,encrypted,key,balance\n");

			// Write all account data to file
			for (Account account : this.getAccounts()) {
				fw.append(
					account.getID() + "," +
					new String(Base64.getEncoder().encodeToString(account.getEncryptedPw())) + "," +
					new String(Base64.getEncoder().encodeToString(account.getKey().getEncoded())) + "," +
					account.getBalance() + "\n"
				);
			}

			fw.flush();
			fw.close();
		} catch (Exception e) {
			System.out.println("Failed to save account data.");
		}
	}

	/**
	 * Return whether the given ID is unique.
	 * @return A boolean based on whether the given ID is unique.
	 */
	public boolean isUniqueID(String id) {
		for (Account account : this.getAccounts()) {
			if (account.getID().equals(id)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Clears the terminal.
	 */
	public static void clearTerminal() {
		System.out.print("\033[H\033[2J");  
		System.out.flush();
	}

	//------------------------------------------------------------------------------------------------------------------
	// Main Method
	//------------------------------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);

		// Create Bank system and UI
		Bank bank = new Bank();
		UI ui = new UI(bank);

		// Create admin account if doesn't exist
		boolean adminExists = false;
		for (Account account : bank.getAccounts()) {
			if (account.getID().equals("admin")) {
				adminExists = true;
			}
		}
		if (!adminExists) {
			bank.getAccounts().add(new Account("admin", "admin", 0));
		}

		// Main loop
		while (true) {
			ui.update();

			System.out.print("> ");
			bank.processCommand(sc.nextLine());
		}
	}
}
