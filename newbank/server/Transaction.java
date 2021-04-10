package newbank.server;

import java.time.LocalDateTime;

public class Transaction {

    private static Integer id;
    private final LocalDateTime date = LocalDateTime.now();
    private String payer;
    private String payeeAccountName;
    private String payee;
    private String beneficiaryAccountName;
    private Double amount;

    public Transaction(String payer, String payeeAccountName, String payee, String beneficiaryAccountName, Double amount) {
        this.payer = payer;
        this.payeeAccountName = payeeAccountName;
        this.payee = payee;
        this.beneficiaryAccountName = beneficiaryAccountName;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", payee='" + payer + '\'' +
                ", payeeAccountName='" + payeeAccountName + '\'' +
                ", beneficiary='" + payee + '\'' +
                ", beneficiaryAccountName='" + beneficiaryAccountName + '\'' +
                ", amount=" + amount +
                '}';
    }

    public static Integer getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPayeeAccountName() {
        return payeeAccountName;
    }

    public void setPayeeAccountName(String payeeAccountName) {
        this.payeeAccountName = payeeAccountName;
    }

    public String getBeneficiaryAccountName() {
        return beneficiaryAccountName;
    }

    public void setBeneficiaryAccountName(String beneficiaryAccountName) {
        this.beneficiaryAccountName = beneficiaryAccountName;
    }
}
