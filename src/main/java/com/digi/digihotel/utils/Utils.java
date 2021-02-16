package com.digi.digihotel.utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {

	public static Set<Integer> getSetFromClosedRange(int max) {
		return IntStream.rangeClosed(1, max).boxed().collect(Collectors.toSet());
	}

}
