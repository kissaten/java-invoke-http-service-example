package com.example;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import org.apache.commons.codec.binary.Base64;

import java.net.URLDecoder;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class MainController {

  @RequestMapping("/home")
    String home() {
    return "home";
  }

  @RequestMapping(value="/submit/", method = RequestMethod.POST)
  @ResponseBody
  String submit(@RequestBody String body) {
    System.out.println("body => " + body);
    Map<String,String> records = parseParams(body);

    String message = records.get("message");
    String phone = records.get("phone");

    sendSms(phone, message);

    return "ok";
  }

  private Map<String,String> parseParams(String body) {
    String[] args = body.split("&");

    Map<String,String> records = new HashMap<String,String>();

    for (String arg : args) {
      String[] parts = arg.split("=");
      String key = parts[0];
      String val = parts.length > 1 ? URLDecoder.decode(parts[1]) : null;
      System.out.println(key + " => " + val);
      records.put(key, val);
    }

    return records;
  }

  public void sendSms(String phoneNumber, String message) {
    String blowerIoUrlStr = System.getenv("BLOWERIO_URL");

    if (null != blowerIoUrlStr) {
      try {
        String data = "to=" + URLEncoder.encode(phoneNumber, "UTF-8") +
          "&message=" + URLEncoder.encode(message, "UTF-8");

        URL blowerIoUrl = new URL(blowerIoUrlStr + "messages");
        final String username = blowerIoUrl.getUserInfo().split(":")[0];
        final String password = blowerIoUrl.getUserInfo().split(":")[1];

        HttpsURLConnection con = (HttpsURLConnection)blowerIoUrl.openConnection();
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);

        String userpass = username + ":" + password;
        String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
        con.setRequestProperty ("Authorization", basicAuth);

        con.addRequestProperty("Accept", "application/json");
        con.getOutputStream().write(data.getBytes("UTF-8"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String tmp = "BLOWERIO Response:\n";
        while((tmp = reader.readLine()) != null) {
            System.out.println(tmp);
        }
      } catch (Exception e) {
        String errMsg = "There was an SMS error: " + e.getMessage();
        e.printStackTrace();
      }
    } else {
      System.out.println("No BlowerIO URL set");
    }
  }

  public static void main(String[] args) throws Exception {
      SpringApplication.run(MainController.class, args);
  }
}
