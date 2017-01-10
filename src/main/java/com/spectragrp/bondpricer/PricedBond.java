package com.spectragrp.bondpricer;

public class PricedBond {
	final double price;
	final Bond bond;
	
	PricedBond(Bond bond, double price) {
		this.bond = bond;
		this.price = price;
	}
}
