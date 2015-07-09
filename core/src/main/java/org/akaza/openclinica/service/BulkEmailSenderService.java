package org.akaza.openclinica.service;

import org.akaza.openclinica.core.EmailEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by krikorkrumlian on 7/9/15.
 */
@Service
public class BulkEmailSenderService {


    @Autowired
    private JavaMailSenderImpl mailSender;

    private ConcurrentLinkedDeque<MimeMessagePreparator> deque = new ConcurrentLinkedDeque<MimeMessagePreparator>();

    public void addMimeMessage(MimeMessagePreparator mimeMessage){
        deque.add(mimeMessage);
    }

    @Scheduled(fixedDelay=120000)
    private void sendEmail(){

        ArrayList<MimeMessagePreparator> mimeMessages = new ArrayList<MimeMessagePreparator>();
        System.out.println("Executing now..." +  mimeMessages.size() + " " + Thread.currentThread().getName() + " " + deque.size());
        MimeMessagePreparator mimeMessage = deque.pollFirst();

        if(mimeMessage == null) {
            System.out.println("Nothing left to do getting out...");
            return;
        }

        while(mimeMessage != null){
            mimeMessages.add(mimeMessage);

            if(mimeMessages.size() == 25){
                System.out.println("I've reached my limit...");
                break;
            }else{
                mimeMessage = deque.pollFirst();
            }

        }

        System.out.println("Sending email..." + mimeMessages.size());
        mailSender.send(mimeMessages.toArray(new MimeMessagePreparator[mimeMessages.size()]));
        sendEmail();

    }

}
