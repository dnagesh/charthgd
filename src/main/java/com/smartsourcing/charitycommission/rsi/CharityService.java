package uk.gov.ccew.rsi.charity.service;


import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.ccew.rsi.charity.dto.CharityResponse;
import uk.gov.ccew.rsi.charity.exception.APIException;
import uk.gov.ccew.rsi.charity.exception.NotFoundException;

@Service
public class CharityService {
    private static final int MAX_CHARITY_NAME_LENGTH = 50;
    private final WebClient webClient = WebClient.create();


    public CharityResponse getByNumber(String charityNumber) {
        try {
            CharityResponse response = webClient.get()
                    .uri("http://external-api/charity/number/" + charityNumber)
                    .retrieve()
                    .onStatus(status -> status.value() == 400,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new IllegalArgumentException("Bad Request: Invalid charity number", ex)))
                    .onStatus(status -> status.value() == 404,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new NotFoundException("Charity not found", ex)))
                    .onStatus(status -> status.is5xxServerError(),
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new APIException("External service error", ex)))
                    .bodyToMono(CharityResponse.class)
                    .block();
            truncateCharityName(response);
            return response;
        } catch (IllegalArgumentException | NotFoundException | APIException ex) {
            // Log and rethrow or handle as needed
            throw ex;
        } catch (Exception ex) {
            // Log unexpected errors
            throw new APIException("Unexpected error occurred", ex);
        }
    }

    public CharityResponse getByName(String charityName) {
        try {
            CharityResponse response = webClient.get()
                    .uri("http://external-api/charity/name/" + charityName)
                    .retrieve()
                    .onStatus(status -> status.value() == 400,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new IllegalArgumentException("Bad Request: Invalid charity name", ex)))
                    .onStatus(status -> status.value() == 404,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new NotFoundException("Charity not found", ex)))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new APIException("API error", ex)))
                    .bodyToMono(CharityResponse.class)
                    .block();
            truncateCharityName(response);
            return response;
        } catch (IllegalArgumentException | NotFoundException | APIException ex) {
            // Log and rethrow or handle as needed
            throw ex;
        } catch (Exception ex) {
            // Log unexpected errors
            throw new APIException("Unexpected error occurred", ex);
        }
    }


    private void truncateCharityName(CharityResponse response) {
        if (response != null && response.getCharityName() != null &&
                response.getCharityName().length() > MAX_CHARITY_NAME_LENGTH) {
            response.setCharityName(response.getCharityName().substring(0, MAX_CHARITY_NAME_LENGTH));
        }
    }
}



