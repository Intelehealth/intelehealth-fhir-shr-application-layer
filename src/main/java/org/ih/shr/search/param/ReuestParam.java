package org.ih.shr.search.param;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;

public class ReuestParam {

	public static String toQueryParam(@RequestParam Map<String, String> reqParam) {
		StringBuilder queryParam = new StringBuilder();
		for (Map.Entry<String, String> entry : reqParam.entrySet()) {
			queryParam.append("&" + entry.getKey() + "=" + entry.getValue());
		}
		String theSearchParamString = queryParam.substring(1);
		return theSearchParamString;
	}

}
