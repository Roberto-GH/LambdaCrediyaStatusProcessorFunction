package com.crediya;

import java.util.List;

public class SqsMessageBody {
    private String email;
    private String subject;
    private Integer status;
    private String message;
    private List<PaymentPlan> paymentPlan;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PaymentPlan> getPaymentPlan() {
        return paymentPlan;
    }

    public void setPaymentPlan(List<PaymentPlan> paymentPlan) {
        this.paymentPlan = paymentPlan;
    }

}
