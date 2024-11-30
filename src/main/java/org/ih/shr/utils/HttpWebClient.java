package org.ih.shr.utils;

import java.io.UnsupportedEncodingException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class HttpWebClient {

	
	static ExchangeStrategies exchangeStrategies = ExchangeStrategies
			.builder()
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(
					10000000)).build();

	
	
	
	
	
	
	
	public static String post(String baseURL, String APIURL, String username, String password, String paylaod)
		    throws UnsupportedEncodingException {
			
			WebClient webClient = WebClient.builder().baseUrl(baseURL)
			       // .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(username, password))
			        .exchangeStrategies(exchangeStrategies).build();
			return webClient.post().uri(APIURL).
					header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			        .body(Mono.just(paylaod), String.class).retrieve().bodyToMono(String.class).block();
			
		}
	
	public static String postWithBasicAuth(String baseURL, String APIURL, String username, String password, String paylaod)
		    throws UnsupportedEncodingException {
			System.err.println(baseURL+""+APIURL+"-"+username+"-"+password+"-"+paylaod);
			WebClient webClient = WebClient.builder().baseUrl(baseURL)
			       .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(username, password))
			        .exchangeStrategies(exchangeStrategies).build();
			return webClient.post().uri(APIURL).
					header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			        .body(Mono.just(paylaod), String.class).retrieve().bodyToMono(String.class).block();
			
	}
	
		
}
