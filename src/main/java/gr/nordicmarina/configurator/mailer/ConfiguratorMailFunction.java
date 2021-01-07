package gr.nordicmarina.configurator.mailer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

public class ConfiguratorMailFunction implements HttpFunction {
	private static final Gson gson = new Gson();

	// Simple function to return "Hello World"
	@Override
	public void service(HttpRequest request, HttpResponse response) throws IOException {

		response.appendHeader("Access-Control-Allow-Origin", "*");

		if ("OPTIONS".equals(request.getMethod())) {
			response.appendHeader("Access-Control-Allow-Methods", "POST");
			response.appendHeader("Access-Control-Allow-Headers", "Content-Type");
			response.appendHeader("Access-Control-Max-Age", "3600");
			response.setStatusCode(HttpURLConnection.HTTP_NO_CONTENT);
			return;
		}

		BufferedWriter writer = response.getWriter();
		Preferences prefs = gson.fromJson(request.getReader(), Preferences.class);
		
		sendInternalMail(new Email(System.getenv("SENDER")), new Email(System.getenv("RECIPIENT")),
				System.getenv("SG_TEMPLATE_ID"), prefs);

		if (prefs.isSendACopyToMe() && Objects.nonNull(prefs.getEmail())) {
			sendInternalMail(new Email(System.getenv("SENDER")), new Email(prefs.getEmail()),
					System.getenv("SG_TEMPLATE_ID"), prefs);
		}
		writer.write("OK");

	}
	
	public void sendSmtp(Email from, Email to, String templateId, Preferences prefs) throws IOException {
		
	}

	public void sendInternalMail(Email from, Email to, String templateId, Preferences prefs) throws IOException {

		Mail 	mail = new Mail(); //mail = new Mail(from, "test no dynamic template",to, new Content("text/plain", "heh"));

		mail.setFrom(from);
		mail.setTemplateId(templateId);
		
		Personalization p = new Personalization();
		p.addDynamicTemplateData("root", prefs);
		/*
		p.addDynamicTemplateData("firstName", prefs.getFirstName());
		p.addDynamicTemplateData("lastName", prefs.getLastName());
		p.addDynamicTemplateData("phone", prefs.getPhone());
		p.addDynamicTemplateData("email", prefs.getEmail());
		p.addDynamicTemplateData("contactMe", prefs.isContactMe());
		p.addDynamicTemplateData("sendACopyToMe", prefs.isSendACopyToMe());
		p.addDynamicTemplateData("boat", prefs.getBoat());
		
		p.addDynamicTemplateData("price", prefs.getPrice());
		
		if (prefs.getEngine()!=null) {
			p.addDynamicTemplateData("engine", prefs.getEngine());

		}
		if (prefs.getPackages()!=null && prefs.getPackages().length >0) {
			p.addDynamicTemplateData("packages",  Arrays.asList(prefs.getPackages()));

		}
		if (prefs.getOptionals()!=null && prefs.getOptionals().length >0) {
			//p.addDynamicTemplateData("optionals", Arrays.asList(prefs.getOptionals()).stream().map(pq->gson.toJson(pq)).collect(Collectors.toList()));
			p.addDynamicTemplateData("optionals", Arrays.asList(prefs.getOptionals()));

		}
		
		*/
		p.addTo(to);
		//p.setSubject("I am interested");
		mail.addPersonalization(p);
		//mail.setSubject("personalization no dyn");
		String mailBody = mail.build();
		System.out.println("1:" +mailBody);
		// mailBody = mail.build();
		System.out.println("2:" +mailBody);
		
		SendGrid sg = new SendGrid(System.getenv("SG_KEY"));
		Request request = new Request();
		request.setMethod(Method.POST);
		request.setEndpoint("mail/send");
		request.setBody(mail.build());

		System.out.println("Sending to " + to.getEmail());
		Response response = sg.api(request);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		System.out.println(response.getHeaders());

	}

	public class Preferences {
		private String firstName;
		private String lastName;
		private Boat boat;
		private String email;
		private String phone;
		private boolean contactMe;
		private boolean sendACopyToMe;
		private Item engine;
		private Item[] optionals;
		private Item[] packages;
		private Integer price;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public Boat getBoat() {
			return boat;
		}

		public void setBoat(Boat boat) {
			this.boat = boat;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public boolean isContactMe() {
			return contactMe;
		}

		public void setContactMe(boolean contactMe) {
			this.contactMe = contactMe;
		}

		public boolean isSendACopyToMe() {
			return sendACopyToMe;
		}

		public void setSendACopyToMe(boolean sendACopyToMe) {
			this.sendACopyToMe = sendACopyToMe;
		}

		public Item getEngine() {
			return engine;
		}

		public void setEngine(Item engine) {
			this.engine = engine;
		}

		public Item[] getOptionals() {
			return optionals;
		}

		public void setOptionals(Item[] optionals) {
			this.optionals = optionals;
		}


		public Integer getPrice() {
			return price/100; // to cents
		}

		public void setPrice(Integer price) {
			this.price = price;
		}

		public Item[] getPackages() {
			return packages;
		}

		public void setPackages(Item[] packages) {
			this.packages = packages;
		}

		@Override
		public String toString() {
			return "Preferences [firstName=" + firstName + ", lastName=" + lastName + ", boat=" + boat + ", email="
					+ email + ", phone=" + phone + ", contactMe=" + contactMe + ", sendACopyToMe=" + sendACopyToMe
					+ ", engine=" + engine + ", optionals=" + Arrays.toString(optionals) + ", packages="
					+ Arrays.toString(packages) + ", price=" + price + "]";
		}
		
		

	}

	public class Item {
		private String description;
		private String type;
		private Integer price;
		private String[] details;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Integer getPrice() {
			return price/100; //to cents
		}

		public void setPrice(Integer priceInclVatInCents) {
			this.price = priceInclVatInCents;
		}

		@Override
		public String toString() {
			return "Item [description=" + description + ", type=" + type + ", price="
					+ price + "]";
		}

		public String[] getDetails() {
			return details;
		}

		public void setDetails(String[] details) {
			this.details = details;
		}

	}
	
	class Boat {
		String model;
		Item[] standard;
		public String getModel() {
			return model;
		}
		public void setModel(String model) {
			this.model = model;
		}
		public Item[] getStandard() {
			return standard;
		}
		public void setStandard(Item[] standard) {
			this.standard = standard;
		}
		
	}

}