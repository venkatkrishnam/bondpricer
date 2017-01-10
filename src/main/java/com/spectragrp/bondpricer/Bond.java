package com.spectragrp.bondpricer;

import java.time.LocalDate;

public class Bond {
	String cusip;
	String issuer;
	String currency;
	LocalDate issueDate;
	LocalDate maturityDate;
	double coupon;
	double parValue;
	int paymentsPerYear;
	String settlementDelay;
	String description;
}
