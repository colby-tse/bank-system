/**
 * Represents the UI of the bank system.
 * @author Colby Tse
 * @version 1.0
 * @since 1.0
 */

import java.io.*;
import java.util.*;

public class UI {

    //------------------------------------------------------------------------------------------------------------------
    // Properties
    //------------------------------------------------------------------------------------------------------------------
    
    private Bank bank;

    //------------------------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------------------------

    public UI(Bank bank) {
        this.bank = bank;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Accessors and Mutators
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the bank linked to this UI.
     * @return A Bank linked to this UI.
     */
    public Bank getBank() {
        return this.bank;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Core Functions
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Update the user interface with current data.
     */
    public void update() {
        clearTerminal();
        this.printLoginInfo();
        this.printSysMsg();
    }

    /**
     * Prints the account data of the current session.
     */
    public void printLoginInfo() {
        if (this.getBank().getCurrentSession() == null) {
            System.out.println("[Not logged in]");
        } else {
            System.out.println(String.format("[Logged in: %s, Current balance: $%,.2f]",
                this.getBank().getCurrentSession().getID(),
                this.getBank().getCurrentSession().getBalance()));
        }
        System.out.println();
    }

    /**
     * Prints the system message.
     */
    public void printSysMsg() {
        if (bank.getSysMsg().length() > 0) {
            System.out.println(bank.getSysMsg() + "\n");
        }
        this.getBank().setSysMsg(""); // Reset system message
    }

    /**
     * Clears the terminal.
     */
    public static void clearTerminal() {
        System.out.print("\033[H\033[2J");  
        System.out.flush();
    }
}