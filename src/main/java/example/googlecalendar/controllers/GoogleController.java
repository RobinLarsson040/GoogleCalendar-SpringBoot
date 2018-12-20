package example.googlecalendar.controllers;

import example.googlecalendar.entities.CalendarDto;
import example.googlecalendar.entities.EventDto;
import example.googlecalendar.services.GoogleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.util.List;
import java.util.Map;


@RestController
public class GoogleController {

    private final GoogleService googleService;
    private final OAuth2AuthorizedClientService authorizedClientService;


    @Autowired
    public GoogleController(OAuth2AuthorizedClientService authorizedClientService, GoogleService googleService
    ) {
        this.authorizedClientService = authorizedClientService;
        this.googleService = googleService;
    }

    @GetMapping("/loginSuccess")
    public String getLoginInfo(OAuth2AuthenticationToken authentication) throws Exception {
        OAuth2AuthorizedClient client = loadClientInfo(authentication);
        Map userInfo = getUserInfo(client);
        //OPTIONAL SAVE USER DETAILS SOMEWHERE HERE
        //EXAMPLE VALUES:
        //USER_NAME: userInfo.get("name");
        //USER_EMAIL: userInfo.get("email");
        //TOKEN_VALUE: client.getAccessToken().getTokenValue();
        System.out.println("ACCESS_TOKEN VALUE: " + client.getAccessToken().getTokenValue());
        System.out.println("REFRESH_TOKEN VALUE: " + client.getRefreshToken().getTokenValue());
        System.out.println(userInfo.toString());
        return "Successfully logged in with user: " + userInfo.get("name");
    }

    @GetMapping("/getEvents")
    public List<EventDto> getEvents(OAuth2AuthenticationToken authentication,
                                    @RequestParam(value = "calendarId", required = false, defaultValue = "primary")
                                            String calendarId) throws Exception {
        OAuth2AuthorizedClient client = loadClientInfo(authentication);
        String tokenValue = client.getAccessToken().getTokenValue();
        return googleService.getEvents(tokenValue, calendarId);
    }

    @GetMapping("/getCalendars")
    public List<CalendarDto> getCalendars(OAuth2AuthenticationToken authentication) throws Exception {
        OAuth2AuthorizedClient client = loadClientInfo(authentication);

        return googleService.getCalendars(client.getAccessToken().getTokenValue());
    }


    private OAuth2AuthorizedClient loadClientInfo(OAuth2AuthenticationToken authentication) throws Exception {
        try {
            OAuth2AuthorizedClient client = authorizedClientService
                    .loadAuthorizedClient(
                            authentication.getAuthorizedClientRegistrationId(),
                            authentication.getName());
            return client;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private Map getUserInfo(OAuth2AuthorizedClient client) throws Exception {
        String userInfoEndpointUri = client.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUri();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken()
                    .getTokenValue());
            HttpEntity entity = new HttpEntity("", headers);
            ResponseEntity<Map> response = restTemplate
                    .exchange(userInfoEndpointUri, HttpMethod.GET, entity, Map.class);
            Map userAttributes = response.getBody();
            return userAttributes;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
