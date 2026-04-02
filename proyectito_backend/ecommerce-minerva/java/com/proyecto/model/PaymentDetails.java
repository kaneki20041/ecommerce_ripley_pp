package com.proyecto.model;

public class PaymentDetails {

	private String paymentMethod;
	private String status;
	private String paymentid;
	private String razorpayPaymentLinkid;
	private String razorpayPaymentLinkReferenceld;
	private String razorpayPaymentLinkStatus;
	private String razorpayPaymentid;

	public PaymentDetails() {

	}

	public PaymentDetails(String paymentMethod, String status, String paymentid, String razorpayPaymentLinkid,
			String razorpayPaymentLinkReferenceld, String razorpayPaymentLinkStatus, String razorpayPaymentid) {
		super();
		this.paymentMethod = paymentMethod;
		this.status = status;
		this.paymentid = paymentid;
		this.razorpayPaymentLinkid = razorpayPaymentLinkid;
		this.razorpayPaymentLinkReferenceld = razorpayPaymentLinkReferenceld;
		this.razorpayPaymentLinkStatus = razorpayPaymentLinkStatus;
		this.razorpayPaymentid = razorpayPaymentid;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPaymentid() {
		return paymentid;
	}
	public void setPaymentid(String paymentid) {
		this.paymentid = paymentid;
	}
	public String getRazorpayPaymentLinkid() {
		return razorpayPaymentLinkid;
	}
	public void setRazorpayPaymentLinkid(String razorpayPaymentLinkid) {
		this.razorpayPaymentLinkid = razorpayPaymentLinkid;
	}
	public String getRazorpayPaymentLinkReferenceld() {
		return razorpayPaymentLinkReferenceld;
	}
	public void setRazorpayPaymentLinkReferenceld(String razorpayPaymentLinkReferenceld) {
		this.razorpayPaymentLinkReferenceld = razorpayPaymentLinkReferenceld;
	}
	public String getRazorpayPaymentLinkStatus() {
		return razorpayPaymentLinkStatus;
	}
	public void setRazorpayPaymentLinkStatus(String razorpayPaymentLinkStatus) {
		this.razorpayPaymentLinkStatus = razorpayPaymentLinkStatus;
	}
	public String getRazorpayPaymentid() {
		return razorpayPaymentid;
	}
	public void setRazorpayPaymentid(String razorpayPaymentid) {
		this.razorpayPaymentid = razorpayPaymentid;
	}

}
