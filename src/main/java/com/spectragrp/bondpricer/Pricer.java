package com.spectragrp.bondpricer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Pricer {
	private double discountRate;
	
	public Pricer() {
		this.discountRate = getDiscountRate();
		
		//reset rate everyday
		ScheduledExecutorService scheduledExecutorService =
		        Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				//produces a rate between 2.5% - 3.0%
				Pricer.this.discountRate = getDiscountRate();
			}
			
		}, 86400, 86400, TimeUnit.SECONDS);
	}

	private double getDiscountRate() {
		return (Math.random()*0.5 + 2.5)/100;
	}

	/**
	 * @param issueDate
	 *            The time when bond is effective.
	 * @param maturityDate
	 * 			  The time when bond matures.
	 * @param coupon
	 *            Coupon rate per annum.
	 * @param parValue
	 *            Redemption (notional repayment).
	 * @param paymentsPerYear
	 *            Frequency (1,2,4), number of times coupon is paid per year.
	 * @return price Clean price.
	 */
	public double price(long issueDate, long maturityDate, double coupon, double parValue, int paymentsPerYear) {
		double price = 0.0;

		double timeToMaturity = maturityDate - issueDate;
		
		if (timeToMaturity > 0) {
			price += parValue;
		}

		double paymentTime = timeToMaturity;
		while (paymentTime > 0) {
			price += (coupon / paymentsPerYear)*100;

			// Discount back
			price = price / (1.0 + discountRate / paymentsPerYear);
			paymentTime -= (1.0 / paymentsPerYear)*365;
		}
		
		return price;
	}
}