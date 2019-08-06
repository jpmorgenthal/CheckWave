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
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.util.List;
import java.util.Properties;
import javax.jdo.PersistenceManager;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.wave.api.*;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class CheckwaveServlet extends AbstractRobotServlet {
	private static final Logger log = Logger.getLogger(CheckwaveServlet.class
			.getName());
	private final Pattern p = Pattern
			.compile("^([0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*@(([0-9a-zA-Z])+([-\\w]*[0-9a-zA-Z])*\\.)+[a-zA-Z]{2,9})$");

	@Override
	public void processEvents(RobotMessageBundle bundle) {
		// TODO Auto-generated method stub
		Wavelet wavelet = bundle.getWavelet();

		log.info(bundle.getRobotAddress());
		if (bundle.wasSelfAdded()) {
			Blip blip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView
					.append("\nTo be notified of changes on this Wave, create a blip with the following text and no other characters or return '[CHECKWAVE]/email address here\n");
			textView.append("\nexample:\n");
			textView.append("\n[CHECKWAVE]/checkwave@appspot.com\n");
			textView.append("\nTo unsubscribe: [CHECKWAVE]!CLEAR!<email address> or ALL");
			log.info("Handled Robot adding itself");
		}

		for (Event e : bundle.getEvents()) {
			if (e.getType() == EventType.BLIP_SUBMITTED) {
				if (e.getBlip().isDocumentAvailable() == false)
					return;
				TextView tv = e.getBlip().getDocument();
				if (tv == null || tv.getText() == null) {
					sendUpdates(wavelet, tv);
					return;
				}
				if (tv.getText().startsWith("[CHECKWAVE]")) {
					String[] commands = tv.getText().split("!");
					if (commands.length > 1) {
						processCommand(wavelet, tv, commands);
						return;
					}
					String[] emailID = tv.getText().split("/");
					if (emailID.length != 2) {
						tv.append("\nInvalid command format");
						return;
					}
					Matcher m = p.matcher(emailID[1]);
					if (!m.matches()) {
						tv.append("\nInvalid email address");
						return;
					}
					InternetAddress ia = null;
					try {
						ia = new InternetAddress(emailID[1], tv.getAuthor());
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					addFollower(wavelet, tv, ia);
				} else
					sendUpdates(wavelet, tv);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processCommand(Wavelet wavelet, TextView tv, String[] commands) {
		// TODO Auto-generated method stub
		PersistenceManager pm = PMF.get().getPersistenceManager();
		if (commands[1].toUpperCase().equals("LIST")) {
			String query = "select from " + EmailWaveIDMapping.class.getName()
					+ " where waveletID=='" + wavelet.getWaveId() + "'";
			List<EmailWaveIDMapping> wavemap = (List<EmailWaveIDMapping>) pm
					.newQuery(query).execute();
			if (wavemap.isEmpty()) {
				return;
			}
			String tvstr = "\n";
			for (EmailWaveIDMapping map : wavemap) {
				tvstr += map.getEmailAddress() + "\n";
			}
			tv.append(tvstr);
			return;
		}
		if (commands[1].toUpperCase().equals("CLEAR")) {
			javax.jdo.Query query = pm.newQuery(EmailWaveIDMapping.class);
			query.setFilter("waveletID=='" + wavelet.getWaveId() + "'");
			if (commands.length > 2 && !commands[2].toUpperCase().equals("ALL")) {
				Matcher m = p.matcher(commands[2]);
				if (!m.matches()) {
					tv.append("\nInvalid email address format");
					return;
				}
				query.setFilter("waveletID=='" + wavelet.getWaveId()
						+ "' && emailAddress=='" + commands[2] + "'");
			}
			try {
				query.deletePersistentAll();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				tv.append("\n" + e.getMessage());
				return;
			}
			tv.append("\n" + commands[2] + " removed from Wave notifications");
			return;
		}
	}

	private void addFollower(Wavelet wavelet, TextView tv,
			InternetAddress emailAddress) {
		// TODO Auto-generated method stub
		EmailWaveIDMapping map = new EmailWaveIDMapping(wavelet.getWaveId(),
				emailAddress);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(map);
		} finally {
			pm.close();
		}
		tv.append("\nAdded email address [" + emailAddress.getAddress()
				+ "] to the conversation\n");
	}

	@SuppressWarnings( { "deprecation", "unchecked" })
	private void sendUpdates(Wavelet wavelet, TextView tv) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String query = "select from " + EmailWaveIDMapping.class.getName()
				+ " where waveletID=='" + wavelet.getWaveId() + "'";
		List<EmailWaveIDMapping> wavemap = (List<EmailWaveIDMapping>) pm
				.newQuery(query).execute();
		if (wavemap.isEmpty()) {
			return;
		}
		try {
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			Message msg = new MimeMessage(session);
			InternetAddress [] toList = new InternetAddress[wavemap.size()];
			int i=0;
			for (EmailWaveIDMapping w: wavemap)
				toList[i++] = w.getEmail();
			msg.addRecipients(RecipientType.TO, toList);
			if (msg.getAllRecipients() != null) {
				String [] urls = wavelet.getWaveId().split("!");
				String url = null;
				urls[1] = URLEncoder.encode(urls[1]);
				urls[1] = URLEncoder.encode(urls[1]);
				if (urls[0].contains("wavesandbox"))
					url = "https://wave.google.com/a/wavesandbox.com/?#restored:wave:"+urls[0]+"!"+urls[1];
				else
					url = "https://wave.google.com/wave/?#restored:wave:" + urls[0]+"!"+urls[1];
				Date d = new Date(wavelet.getLastModifiedTime());
				String msgBody = "Do not reply to this email "+System.getProperty("line.separator"); 
				msgBody += "Wave " + url + " has been updated\n at "
						+ d.toGMTString()
						+ System.getProperty("line.separator");
				if (tv.getText() == null)
					msgBody += ("a non-text message was added to the wave");
				else
					msgBody += tv.getText();
				msg.setFrom(new InternetAddress("jpmorgenthal@gmail.com",
						"Check Wave Administrator"));
				msg.setSubject("Google Wave Update by CheckWave");
				msg.setText(msgBody);
				Transport.send(msg);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			log.warning(e.getMessage());
		}
	}
}
