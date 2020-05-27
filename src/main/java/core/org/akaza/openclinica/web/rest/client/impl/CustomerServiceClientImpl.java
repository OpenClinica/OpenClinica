package core.org.akaza.openclinica.web.rest.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.OCUserDTO;
import core.org.akaza.openclinica.web.rest.client.dto.CustomerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;

@Service
public class CustomerServiceClientImpl {
    private String sbsUrl = CoreResources.getField("SBSBaseUrl");
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final String GET_CUSTOMER_URL = "/customer-service/api/customers/{0}";
    private final String GET_CUSTOMER_UUID_URL = "/customer-service/api/customer-uuid?subdomain={0}";

    public CustomerDTO getCustomer(String accessToken, String customerUuid) {
        String endPoint = MessageFormat.format(GET_CUSTOMER_URL, customerUuid);
        String endPointUri = sbsUrl + endPoint;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity entity = new HttpEntity<OCUserDTO>(headers);
        ResponseEntity<CustomerDTO> response = null;
        try {
            response = restTemplate.exchange(endPointUri, HttpMethod.GET, entity, CustomerDTO.class);
        } catch (HttpClientErrorException e) {
            logger.error("Customer service error: {}", e.getResponseBodyAsString());
        }

        if (response == null) {
            return null;
        } else {
            return  response.getBody();
        }

    }

    public String getCustomerUuid(String accessToken) {
        int index = sbsUrl.indexOf("//");
        String subdomain = sbsUrl.substring(index  + 2,  sbsUrl.indexOf("."));
        String endPoint = MessageFormat.format(GET_CUSTOMER_UUID_URL, subdomain);
        String endPointUri = sbsUrl + endPoint;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity entity = new HttpEntity<OCUserDTO>(headers);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(endPointUri, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            logger.error("Customer service error: {}", e.getResponseBodyAsString());
        }

        if (response == null) {
            return null;
        } else {
            return response.getBody();
        }

    }


}
