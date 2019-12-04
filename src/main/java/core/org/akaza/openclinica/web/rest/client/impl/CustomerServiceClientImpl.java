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

@Service
public class CustomerServiceClientImpl {
    private String sbsUrl = CoreResources.getField("SBSBaseUrl");
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());


    public CustomerDTO getCustomer(String accessToken,String customerUuid) {
        String subDomainUrl="/customer-service/api/customers/"+customerUuid;
        String uri =sbsUrl + subDomainUrl;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        Study studyBean = null;
        HttpEntity entity = new HttpEntity<OCUserDTO>(headers);
        ResponseEntity<CustomerDTO> response = null;
        try {
            response = restTemplate.exchange(uri, HttpMethod.GET, entity, CustomerDTO.class);
        } catch (HttpClientErrorException e) {
            logger.error("KeyCloak error message: {}", e.getResponseBodyAsString());
        }

        if (response == null) {
            return null;
        } else {
            return  response.getBody();
        }

    }


}
