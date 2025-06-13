package at.jku.dke.task_app.clustering.services;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

public class ClusteringMessageService {

    private static final MessageSource messageSource = initMessageSource();

    private static MessageSource initMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages"); // refers to messages.properties etc.
        messageSource.setDefaultLocale(Locale.ENGLISH);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    public static MessageSource getMessageSource() {
        return messageSource;
    }


}
