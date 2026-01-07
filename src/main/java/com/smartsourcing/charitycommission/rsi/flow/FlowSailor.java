package uk.gov.ccew.rsi.flow.service;

import uk.gov.ccew.rsi.flow.model.UserStep;

import java.util.Map;
import java.util.Optional;

public interface FlowSailor {

void startOrResume(String businessKey);
Optional<UserStep> getCurrentStep(String businessKey);
void completeCurrent(String businessKey, Map<String,String> formData);
Optional<UserStep> next(String businessKey, Map<String,String> formData);
Optional<UserStep> back(String businessKey, String target);
}
