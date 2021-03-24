package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;

public class NewBankClientHandler extends Thread {

    private final NewBank bank;
    private final BufferedReader in;
    private final PrintWriter out;

    private boolean clientHasAccount(String userInput) {
        return userInput.equals("y");
    }

    private boolean yesNoUserInput(String userInput) {
        return userInput.equals("y") || userInput.equals("n");
    }

    private boolean passwordFollowsRules(String password) {
        return password.length() > 7 && password.length() < 21;
    }

    public NewBankClientHandler(Socket s) throws IOException {
        bank = NewBank.getBank();
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
    }

    private void closeStreams() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private boolean askDoesClientHaveAccount() {
        //check if user has account
        out.println("Do you have an account with us? Please enter Y or N");
        String userInput = "";
        try {
            userInput = in.readLine().toLowerCase();
            while (!yesNoUserInput(userInput)) {
                //incorrect response given - wait for a correct response before continuing
                out.println("Please enter Y or N");
                userInput = in.readLine().toLowerCase();
            }

        } catch (IOException e) {
            e.printStackTrace();
            out.println("We encountered an error. Please try again later.");
            closeStreams();
        }
        return clientHasAccount(userInput);
    }

    private CustomerID loginUser() {
        String userName = "";
        String password = "";
        try {
            // ask for user name
            out.println("Enter Username");
            userName = in.readLine();
            // ask for password
            out.println("Enter Password");
            password = in.readLine();
            out.println("Checking Details...");
            // authenticate user and get customer ID token from bank for use in subsequent requests
        } catch (IOException e) {
            e.printStackTrace();
            out.println("We encountered an error. Please try again later.");
            closeStreams();
        }
        return bank.checkLogInDetails(userName, password);
    }

    public void run() {
        // keep getting requests from the client and processing them
        try {
            boolean accountToLogIn = askDoesClientHaveAccount();
            if (accountToLogIn) {
                CustomerID customer = loginUser();
                // if the user is authenticated then get requests from the user and process them
                if (customer != null) {
                    out.println("Log In Successful. What do you want to do?");
                    while (true) {
                        String request = in.readLine();
                        System.out.println("Request from " + customer.getKey());
                        String response = bank.processRequest(customer, request);
                        if (response.equals("LOGOUT")) {
                            out.println("Logging out...");
                            break;
                        }
                        out.println(response);
                    }
                } else {
                    out.println("Log In Failed");
                }
            } else {
                // ask for user name
                out.println("Enter Username to create account");
                String userName = in.readLine();
                //make sure you aren't going to overwrite a previous customer
                while(!bank.usernameIsAvailable(userName)) {
                    out.println("Username already taken. Please choose a different username");
                    userName = in.readLine();
                }
                // ask for password
                out.println("Enter Password of length between 8 - 20 characters");
                String password = in.readLine();
                while (!passwordFollowsRules(password)) {
                    out.println("Password could not be set. Please enter a password of length between 8 - 20 characters.");
                    password = in.readLine();
                }
                CustomerID customer = bank.createNewCustomer(userName, password);
                out.println( "Customer successfully created. What do you want to do next?");
                while (true) {
                    String request = in.readLine();
                    System.out.println("Request from " + customer.getKey());
                    String response = bank.processRequest(customer, request);
                    if (response.equals("LOGOUT")) {
                        out.println("Logging out...");
                        break;
                    }
                    out.println(response);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStreams();
        }
    }

}
