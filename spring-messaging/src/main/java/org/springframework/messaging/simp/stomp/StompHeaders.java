/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.messaging.simp.stomp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Represents STOMP frame headers.
 *
 * <p>In addition to the normal methods defined by {@link Map}, this class offers
 * the following convenience methods:
 * <ul>
 * <li>{@link #getFirst(String)} return the first value for a header name</li>
 * <li>{@link #add(String, String)} add to the list of values for a header name</li>
 * <li>{@link #set(String, String)} set a header name to a single string value</li>
 * </ul>
 *
 * @author Rossen Stoyanchev
 * @since 4.2
 * @see <a href="http://stomp.github.io/stomp-specification-1.2.html#Frames_and_Headers">
 *     http://stomp.github.io/stomp-specification-1.2.html#Frames_and_Headers</a>
 */
public class StompHeaders implements MultiValueMap<String, String>, Serializable {

	private static final long serialVersionUID = 7514642206528452544L;


	// Standard headers (as defined in the spec)

	public static final String CONTENT_TYPE = "content-type"; // SEND, MESSAGE, ERROR

	public static final String CONTENT_LENGTH = "content-length"; // SEND, MESSAGE, ERROR

	public static final String RECEIPT = "receipt"; // any client frame other than CONNECT

	// CONNECT

	public static final String HOST = "host";

	public static final String LOGIN = "login";

	public static final String PASSCODE = "passcode";

	public static final String HEARTBEAT = "heart-beat";

	// CONNECTED

	public static final String SESSION = "session";

	public static final String SERVER = "server";

	// SEND

	public static final String DESTINATION = "destination";

	// SUBSCRIBE, UNSUBSCRIBE

	public static final String ID = "id";

	public static final String ACK = "ack";

	// MESSAGE

	public static final String SUBSCRIPTION = "subscription";

	public static final String MESSAGE_ID = "message-id";

	// RECEIPT

	public static final String RECEIPT_ID = "receipt-id";


	private final Map<String, List<String>> headers;


	/**
	 * Create a new instance to be populated with new header values.
	 */
	public StompHeaders() {
		this(new LinkedMultiValueMap<String, String>(4), false);
	}

	private StompHeaders(Map<String, List<String>> headers, boolean readOnly) {
		Assert.notNull(headers, "'headers' must not be null");
		if (readOnly) {
			Map<String, List<String>> map = new LinkedMultiValueMap<String, String>(headers.size());
			for (Entry<String, List<String>> entry : headers.entrySet()) {
				List<String> values = Collections.unmodifiableList(entry.getValue());
				map.put(entry.getKey(), values);
			}
			this.headers = Collections.unmodifiableMap(map);
		}
		else {
			this.headers = headers;
		}
	}


	/**
	 * Set the content-type header.
	 * Applies to the SEND, MESSAGE, and ERROR frames.
	 */
	public void setContentType(MimeType mimeType) {
		Assert.isTrue(!mimeType.isWildcardType(), "'Content-Type' cannot contain wildcard type '*'");
		Assert.isTrue(!mimeType.isWildcardSubtype(), "'Content-Type' cannot contain wildcard subtype '*'");
		set(CONTENT_TYPE, mimeType.toString());
	}

	/**
	 * Return the content-type header value.
	 */
	public MimeType getContentType() {
		String value = getFirst(CONTENT_TYPE);
		return (StringUtils.hasLength(value) ? MimeTypeUtils.parseMimeType(value) : null);
	}

	/**
	 * Set the content-length header.
	 * Applies to the SEND, MESSAGE, and ERROR frames.
	 */
	public void setContentLength(long contentLength) {
		set(CONTENT_LENGTH, Long.toString(contentLength));
	}

	/**
	 * Return the content-length header or -1 if unknown.
	 */
	public long getContentLength() {
		String value = getFirst(CONTENT_LENGTH);
		return (value != null ? Long.parseLong(value) : -1);
	}

	/**
	 * Set the receipt header.
	 * Applies to any client frame other than CONNECT.
	 */
	public void setReceipt(String receipt) {
		set(RECEIPT, receipt);
	}

	/**
	 * Get the receipt header.
	 */
	public String getReceipt() {
		return getFirst(RECEIPT);
	}

	/**
	 * Set the host header.
	 * Applies to the CONNECT frame.
	 */
	public void setHost(String host) {
		set(HOST, host);
	}

	/**
	 * Get the host header.
	 */
	public String getHost() {
		return getFirst(HOST);
	}

	/**
	 * Set the login header.
	 * Applies to the CONNECT frame.
	 */
	public void setLogin(String login) {
		set(LOGIN, login);
	}

	/**
	 * Get the login header.
	 */
	public String getLogin() {
		return getFirst(LOGIN);
	}

	/**
	 * Set the passcode header.
	 * Applies to the CONNECT frame.
	 */
	public void setPasscode(String passcode) {
		set(PASSCODE, passcode);
	}

	/**
	 * Get the passcode header.
	 */
	public String getPasscode() {
		return getFirst(PASSCODE);
	}

	/**
	 * Set the heartbeat header.
	 * Applies to the CONNECT and CONNECTED frames.
	 */
	public void setHeartbeat(long[] heartbeat) {
		Assert.notNull(heartbeat);
		String value = heartbeat[0] + "," + heartbeat[1];
		Assert.isTrue(heartbeat[0] >= 0 && heartbeat[1] >= 0, "Heart-beat values cannot be negative: "  + value);
		set(HEARTBEAT, value);
	}

	/**
	 * Get the heartbeat header.
	 */
	public long[] getHeartbeat() {
		String rawValue = getFirst(HEARTBEAT);
		if (!StringUtils.hasText(rawValue)) {
			return null;
		}
		String[] rawValues = StringUtils.commaDelimitedListToStringArray(rawValue);
		return new long[] {Long.valueOf(rawValues[0]), Long.valueOf(rawValues[1])};
	}

	/**
	 * Whether heartbeats are enabled. Returns {@code false} if
	 * {@link #setHeartbeat} is set to "0,0", and {@code true} otherwise.
	 */
	public boolean isHeartbeatEnabled() {
		long[] heartbeat = getHeartbeat();
		return (heartbeat != null && heartbeat[0] != 0 && heartbeat[1] != 0);
	}

	/**
	 * Set the session header.
	 * Applies to the CONNECTED frame.
	 */
	public void setSession(String session) {
		set(SESSION, session);
	}

	/**
	 * Get the session header.
	 */
	public String getSession() {
		return getFirst(SESSION);
	}

	/**
	 * Set the server header.
	 * Applies to the CONNECTED frame.
	 */
	public void setServer(String server) {
		set(SERVER, server);
	}

	/**
	 * Get the server header.
	 * Applies to the CONNECTED frame.
	 */
	public String getServer() {
		return getFirst(SERVER);
	}

	/**
	 * Set the destination header.
	 */
	public void setDestination(String destination) {
		set(DESTINATION, destination);
	}

	/**
	 * Get the destination header.
	 * Applies to the SEND, SUBSCRIBE, and MESSAGE frames.
	 */
	public String getDestination() {
		return getFirst(DESTINATION);
	}

	/**
	 * Set the id header.
	 * Applies to the SUBSCR0BE, UNSUBSCRIBE, and ACK or NACK frames.
	 */
	public void setId(String id) {
		set(ID, id);
	}

	/**
	 * Get the id header.
	 */
	public String getId() {
		return getFirst(ID);
	}

	/**
	 * Set the ack header to one of "auto", "client", or "client-individual".
	 * Applies to the SUBSCRIBE and MESSAGE frames.
	 */
	public void setAck(String ack) {
		set(ACK, ack);
	}

	/**
	 * Get the ack header.
	 */
	public String getAck() {
		return getFirst(ACK);
	}

	/**
	 * Set the login header.
	 * Applies to the MESSAGE frame.
	 */
	public void setSubscription(String subscription) {
		set(SUBSCRIPTION, subscription);
	}

	/**
	 * Get the subscription header.
	 */
	public String getSubscription() {
		return getFirst(SUBSCRIPTION);
	}

	/**
	 * Set the message-id header.
	 * Applies to the MESSAGE frame.
	 */
	public void setMessageId(String messageId) {
		set(MESSAGE_ID, messageId);
	}

	/**
	 * Get the message-id header.
	 */
	public String getMessageId() {
		return getFirst(MESSAGE_ID);
	}

	/**
	 * Set the receipt-id header.
	 * Applies to the RECEIPT frame.
	 */
	public void setReceiptId(String receiptId) {
		set(RECEIPT_ID, receiptId);
	}

	/**
	 * Get the receipt header.
	 */
	public String getReceiptId() {
		return getFirst(RECEIPT_ID);
	}

	/**
	 * Return the first header value for the given header name, if any.
	 * @param headerName the header name
	 * @return the first header value, or {@code null} if none
	 */
	@Override
	public String getFirst(String headerName) {
		List<String> headerValues = headers.get(headerName);
		return headerValues != null ? headerValues.get(0) : null;
	}

	/**
	 * Add the given, single header value under the given name.
	 * @param headerName the header name
	 * @param headerValue the header value
	 * @throws UnsupportedOperationException if adding headers is not supported
	 * @see #put(String, List)
	 * @see #set(String, String)
	 */
	@Override
	public void add(String headerName, String headerValue) {
		List<String> headerValues = headers.get(headerName);
		if (headerValues == null) {
			headerValues = new LinkedList<String>();
			this.headers.put(headerName, headerValues);
		}
		headerValues.add(headerValue);
	}

	/**
	 * Set the given, single header value under the given name.
	 * @param headerName the header name
	 * @param headerValue the header value
	 * @throws UnsupportedOperationException if adding headers is not supported
	 * @see #put(String, List)
	 * @see #add(String, String)
	 */
	@Override
	public void set(String headerName, String headerValue) {
		List<String> headerValues = new LinkedList<String>();
		headerValues.add(headerValue);
		headers.put(headerName, headerValues);
	}

	@Override
	public void setAll(Map<String, String> values) {
		for (Entry<String, String> entry : values.entrySet()) {
			set(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public Map<String, String> toSingleValueMap() {
		LinkedHashMap<String, String> singleValueMap = new LinkedHashMap<String,String>(this.headers.size());
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			singleValueMap.put(entry.getKey(), entry.getValue().get(0));
		}
		return singleValueMap;
	}


	// Map implementation

	@Override
	public int size() {
		return this.headers.size();
	}

	@Override
	public boolean isEmpty() {
		return this.headers.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.headers.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.headers.containsValue(value);
	}

	@Override
	public List<String> get(Object key) {
		return this.headers.get(key);
	}

	@Override
	public List<String> put(String key, List<String> value) {
		return this.headers.put(key, value);
	}

	@Override
	public List<String> remove(Object key) {
		return this.headers.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<String>> map) {
		this.headers.putAll(map);
	}

	@Override
	public void clear() {
		this.headers.clear();
	}

	@Override
	public Set<String> keySet() {
		return this.headers.keySet();
	}

	@Override
	public Collection<List<String>> values() {
		return this.headers.values();
	}

	@Override
	public Set<Entry<String, List<String>>> entrySet() {
		return this.headers.entrySet();
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof StompHeaders)) {
			return false;
		}
		StompHeaders otherHeaders = (StompHeaders) other;
		return this.headers.equals(otherHeaders.headers);
	}

	@Override
	public int hashCode() {
		return this.headers.hashCode();
	}

	@Override
	public String toString() {
		return this.headers.toString();
	}


	/**
	 * Return a {@code StompHeaders} object that can only be read, not written to.
	 */
	public static StompHeaders readOnlyStompHeaders(Map<String, List<String>> headers) {
		return new StompHeaders(headers, true);
	}

}
