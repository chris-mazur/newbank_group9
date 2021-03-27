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
        CustomerID id = bank.checkLogInDetails(userName, password);
        return id;
    }

    private void processUserRequests(CustomerID customer) {
        // keep getting requests from the client and processing them
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            out.println("We encountered an error. Please try again later.");
            closeStreams();
        }
    }

    private CustomerID createAccount() {
        String userName = "";
        String password = "";
        try {
            // ask for user name
            out.println("Enter Username to create account");
            userName = in.readLine();
            //make sure you aren't going to overwrite a previous customer
            while (!bank.usernameIsAvailable(userName)) {
                out.println("Username already taken. Please choose a different username");
                userName = in.readLine();
            }
            // ask for password
            out.println("Enter Password of length between 8 - 20 characters");
            password = in.readLine();
            while (!passwordFollowsRules(password)) {
                out.println("Password could not be set. Please enter a password of length between 8 - 20 characters.");
                password = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            out.println("We encountered an error. Please try again later.");
            closeStreams();
        }
        return bank.createNewCustomer(userName, password);
    }

    private CustomerID createUserAndAccount() {
        CustomerID user = createAccount();
        String[] accName = {"NEWACCOUNT","current"};
        bank.newCurrentAccount(user, accName);

        return user;
    }

    public void run() {
        try {
            boolean accountToLogIn = askDoesClientHaveAccount();
            CustomerID customer = null;
            while (customer == null) {
            if (accountToLogIn) {
                customer = loginUser();
            } else {
                customer = createUserAndAccount();
            }
            if (customer == null) {
                out.println("Failed");
            }
            }
            // if the user is authenticated then get requests from the user and process them
            out.println("Success! What do you want to do next?");
            processUserRequests(customer);
        } finally {
            closeStreams();
        }
    }

}
