package com.jpmorgenthal.checkwave;
/**
Copyright (c) 2009 jpmorgenthal.com

Permission is hereby granted, free of charge, to any person or organization 
obtaining a copy of this software and associated  documentation  files (the 
"Software"), to deal in the Software without restriction, including without 
limitation the rights to use, copy, modify, merge, publish, distribute, sub
license, and/or sell copies of the Software, and to permit persons  to whom 
the Software is furnished to do so, subject to the following conditions:

The above copyright notice and  this permission notice shall be included in 
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS",  WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING  BUT NOT  LIMITED  TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR  COPYRIGHT  HOLDERS  BE  LIABLE  FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY,  WHETHER  IN  AN  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
FROM,  OUT  OF  OR  IN  CONNECTION  WITH  THE  SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE
*/

import java.io.UnsupportedEncodingException;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.mail.internet.InternetAddress;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class EmailWaveIDMapping {
    @Persistent
    private String waveletID;

    @Persistent
    private String emailAddress;
    
    @Persistent
    private String emailPersonal;
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    public EmailWaveIDMapping(String waveID, InternetAddress email){
    	this.waveletID = waveID;
    	this.emailAddress = email.getAddress();
    	this.emailPersonal = email.getPersonal();
    }

	public void setWaveletID(String waveletID) {
		this.waveletID = waveletID;
	}

	public String getWaveletID() {
		return waveletID;
	}

	public InternetAddress getEmail() {
		InternetAddress ia;
		try {
			ia = new InternetAddress(this.emailAddress, this.emailPersonal);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return ia;
	}

	public Key getKey() {
		return key;
	}

	public void setEmailPersonal(String emailPersonal) {
		this.emailPersonal = emailPersonal;
	}

	public String getEmailPersonal() {
		return emailPersonal;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

}
