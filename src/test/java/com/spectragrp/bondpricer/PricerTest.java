package com.spectragrp.bondpricer;

import java.time.LocalDate;
import java.time.Month;

import junit.framework.TestCase;

/**
 * Unit test for bond pricer.
 */
public class PricerTest extends TestCase {

	public void testPrice() {
		Pricer pricer = new Pricer();
		
		// 2009-07-31
		LocalDate issueDate = LocalDate.of(2009, Month.JULY, 31);

		// 2029-01-15
		LocalDate maturityDate = LocalDate.of(2029, Month.JANUARY, 15);
		
		double coupon = .02500;
		
		double faceValue = 100;
		
		int frequency = 2;
		
		double price = pricer.price(issueDate.toEpochDay(), maturityDate.toEpochDay(), coupon, faceValue, frequency);
		System.out.println(price);
	}
}
