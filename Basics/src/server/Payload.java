package server;

import java.io.Serializable;

public class Payload implements Serializable {

	/**
	 * baeldung.com/java-serial-version-uid
	 */
	private static final long serialVersionUID = -6687715510484845706L;

	private String clientName;

	public void setClientName(String s) {
		this.clientName = s;
	}

	public String getClientName() {
		return clientName;
	}

	private String message;

	public void setMessage(String s) {
		this.message = s;
	}

	public String getMessage() {
		return this.message;
	}

	private PayloadType payloadType;

	public void setPayloadType(PayloadType pt) {
		this.payloadType = pt;
	}

	public PayloadType getPayloadType() {
		return this.payloadType;
	}

	private int number;

	public void setNumber(int n) {
		this.number = n;
	}

	public int getNumber() {
		return this.number;
	}

	boolean isMuted = false;

	public void setMute(boolean bool) {
		this.isMuted = bool;
	}

	public boolean getMute() {
		return isMuted;
	}

	@Override
	public String toString() {
		return String.format("Type[%s], Number[%s], Message[%s]", getPayloadType().toString(), getNumber(),
				getMessage());
	}
}