package uk.gov.ccew.rsi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ccew.rsi.dto.FormData;
import uk.gov.ccew.rsi.service.RSIService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RsiApplicationTests {

    @Autowired
    private RSIService service;

    @Test
    void shouldCreateAndRetrieveFormData() {

        FormData formData = new FormData();
        formData.setReferenceCode("REF001");
        formData.setStatus("CREATED");
        formData.setEmail("test@example.com");
        formData.setFirstname("John");
        formData.setSurname("Doe");
        formData.setPhoneNumber("07700900982");
        formData.setEmailSent(true);
        formData.setUserAnswers(Map.of("q1", "a1"));

        FormData created = service.create(formData);

        assertThat(created).isNotNull();
        assertThat(created.getReferenceCode()).isEqualTo("REF001");

        FormData fetched = service.getById(created.getId());
        assertThat(fetched).isNotNull();
        assertThat(fetched.getEmail()).isEqualTo("test@example.com");
    }
}
