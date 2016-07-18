package org.akaza.openclinica.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by krikorkrumlian on 7/9/15.
 */
@Service
public class BulkEmailSenderService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());


    @Autowired
    private JavaMailSenderImpl mailSender;


    private static ConcurrentLinkedDeque<MimeMessagePreparator> DEQUE = new ConcurrentLinkedDeque<MimeMessagePreparator>();

    public static void addMimeMessage(MimeMessagePreparator mimeMessage){
        DEQUE.add(mimeMessage);
    }

    @Scheduled(fixedDelay=120000)
    private void sendEmail(){

        ArrayList<MimeMessagePreparator> mimeMessages = new ArrayList<MimeMessagePreparator>();
        logger.info("Executing now..." +  mimeMessages.size() + " " + Thread.currentThread().getName() + " " + DEQUE.size());
        MimeMessagePreparator mimeMessage = DEQUE.pollFirst();

        if(mimeMessage == null) {
        	logger.info("Nothing left to do getting out...");
            return;
        }

        while(mimeMessage != null){
            mimeMessages.add(mimeMessage);
            logger.info("In while loop");

            if(mimeMessages.size() == 25){
            	logger.info("I've reached my limit...");
                break;
            }else{
            	logger.info("Else");
                mimeMessage = DEQUE.pollFirst();
            }
        }

        logger.info("Sending Boom email..." + mimeMessages.size());
        mailSender.send(mimeMessages.toArray(new MimeMessagePreparator[mimeMessages.size()]));
        logger.info("I think i sent my mail..." + mimeMessages.size());
        sendEmail();

    }

}
