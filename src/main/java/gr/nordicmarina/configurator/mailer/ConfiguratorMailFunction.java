package gr.nordicmarina.configurator.mailer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

public class ConfiguratorMailFunction implements HttpFunction {
	private static final Gson gson = new Gson();

	// Simple function to return "Hello World"
	@Override
	public void service(HttpRequest request, HttpResponse response) throws IOException {
		BufferedWriter writer = response.getWriter();
		Preferences prefs = gson.fromJson(request.getReader(), Preferences.class);
		System.out.println(prefs);
		sendMail(prefs);
		writer.write("OK");

	}

	public void sendMail(Preferences prefs) throws IOException {
		Email from = new Email(System.getenv("sender"));
		Email to = new Email(System.getenv("recipient"));
		Mail mail = new Mail();
		mail.setFrom(from);
		mail.setTemplateId(System.getenv("sendgrid-templateId"));
		Personalization p = new Personalization();
		p.addDynamicTemplateData("email", prefs.getClientEmail());
		p.addDynamicTemplateData("contactMe", prefs.isContactMe());
		p.addDynamicTemplateData("boat", prefs.getBoat());
		p.addDynamicTemplateData("engine", prefs.getEngine());
		p.addDynamicTemplateData("optionals",prefs.getOptionals());
		p.addTo(to);
		
		mail.addPersonalization(p);
		
		SendGrid sg = new SendGrid(System.getenv("sendgrid-key"));
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			
			Response response = sg.api(request);
			System.out.println(response.getStatusCode());
			System.out.println(response.getBody());
			System.out.println(response.getHeaders());
		} catch (IOException ex) {
			throw ex;
		}

	}
	
	public class Preferences {
		private  String boat;
		private  String clientEmail;
		private  boolean contactMe;
		private  String engine;
		private String[] optionals;
		public String getBoat() {
			return boat;
		}
		public void setBoat(String boat) {
			this.boat = boat;
		}
		public String getClientEmail() {
			return clientEmail;
		}
		public void setClientEmail(String clientEmail) {
			this.clientEmail = clientEmail;
		}
		public boolean isContactMe() {
			return contactMe;
		}
		public void setContactMe(boolean contactMe) {
			this.contactMe = contactMe;
		}
		public String getEngine() {
			return engine;
		}
		public void setEngine(String engine) {
			this.engine = engine;
		}
		public String[] getOptionals() {
			return optionals;
		}
		public void setOptionals(String[] optionals) {
			this.optionals = optionals;
		}
		@Override
		public String toString() {
			return "Preferences [boat=" + boat + ", clientEmail=" + clientEmail + ", contactMe=" + contactMe
					+ ", engine=" + engine + ", optionals=" + Arrays.toString(optionals) + "]";
		}

		
	}

}