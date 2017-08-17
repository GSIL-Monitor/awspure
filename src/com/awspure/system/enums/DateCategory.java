package com.awspure.system.enums;

public enum DateCategory {

	YEAR(1), MONTH(2), DAY(3);

	private int value;

	private DateCategory(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
