package newbank.server;

import java.util.Date;

public class SavingsAccount extends Account {

    private double interestRate;
    private double interestAccrued;
    private double totalInterestPaid;
    private Date lastUpdated;

    public SavingsAccount(String accountSortCode, int accountNumber, String accountName, double openingBalance, double interestRate, Date currentDate) {
        super(accountSortCode, accountNumber, accountName, openingBalance);
        this.canPay = false;
        this.canLoan = false;
        this.accountType = "Savings";
        this.interestRate = interestRate;
        this.lastUpdated = currentDate;
        this.totalInterestPaid = 0;
        this.interestAccrued = 0;

    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getTotalInterestPaid() {
        return totalInterestPaid;
    }

    public double getInterestAccrued() {
        return interestAccrued;
    }

    public void updateInterestAccrued(Date currentDate) {
        long timeElapsed = currentDate.getTime() - lastUpdated.getTime(); // milliseconds
        long interestPeriod = timeElapsed / (1000 * 60 * 60 * 24); // days
        interestAccrued += accountBalance * (interestRate / 365.24) * interestPeriod;
        lastUpdated = currentDate;
    }

    public double payInterest(Date currentDate) {
        updateInterestAccrued(currentDate);
        double interestToPay = interestAccrued;
        totalInterestPaid += interestAccrued;
        interestAccrued = 0;
        return interestToPay;
    }

    @Override
    public String toString() {
        return (accountType + " - " + accountName + " (" + accountSortCode + " " + accountNumber + "): " + String.format("%.2f",accountBalance)
            + "\n   Current Interest Rate: " + interestRate
            + "\n   Interest paid to date: " + String.format("%.2f",totalInterestPaid)
            + "\n   Unpaid interest accrued to date: " + String.format("%.2f",interestAccrued));
    }


}
