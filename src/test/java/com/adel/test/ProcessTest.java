package com.example;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static io.camunda.zeebe.spring.test.ZeebeTestAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ZeebeSpringTest
public class ProcessTest {

    @Autowired
    private ZeebeClient zeebe;

    @MockBean
    private TweetService tweetService;

    @Test
    void testTweetApprovalProcess() {
        
        String tweetText = "Hello world";
        String bossName = "Zeebot";
        
        ProcessInstanceEvent processInstance = zeebe.newCreateInstanceCommand()
            .bpmnProcessId("Process_1jaff3v") // ID вашего BPMN процесса
            .latestVersion()
            .variables("{\"tweet\":\"" + tweetText + "\", \"boss\":\"" + bossName + "\"}")
            .send()
            .join();

        waitForUserTaskAndComplete("user_task_review_tweet", Collections.singletonMap("approved", true));
        
        waitForProcessInstanceCompleted(processInstance);
        
        assertThat(processInstance)
            .hasPassedElement("end_event_tweet_published")
            .hasNotPassedElement("end_event_tweet_rejected")
            .isCompleted();
        
        Mockito.verify(tweetService).tweet(tweetText);
        Mockito.verifyNoMoreInteractions(tweetService);
    }

    @Test
    void testTweetRejectionProcess() {
        
        String tweetText = "Controversial tweet";
        String bossName = "Zeebot";
        
        ProcessInstanceEvent processInstance = zeebe.newCreateInstanceCommand()
            .bpmnProcessId("Process_1jaff3v")
            .latestVersion()
            .variables("{\"tweet\":\"" + tweetText + "\", \"boss\":\"" + bossName + "\"}")
            .send()
            .join();

        waitForUserTaskAndComplete("user_task_review_tweet", Collections.singletonMap("approved", false));
        
        waitForProcessInstanceCompleted(processInstance);
        
        assertThat(processInstance)
            .hasPassedElement("end_event_tweet_rejected")
            .hasNotPassedElement("end_event_tweet_published")
            .isCompleted();
        
        Mockito.verifyNoInteractions(tweetService);
    }

    public interface TweetService {
        void tweet(String message);
    }
}