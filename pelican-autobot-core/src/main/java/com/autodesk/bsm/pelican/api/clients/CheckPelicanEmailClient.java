package com.autodesk.bsm.pelican.api.clients;

import com.autodesk.bsm.pelican.constants.EmailConstants;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * This util method is designed to help validate email templates
 *
 * @author mandas
 */
public class CheckPelicanEmailClient {

    private static String mailPort = "143";
    private static String getMailFolder = "INBOX";
    private static String mailBody = null;
    private static EnvironmentVariables environmentVariables;
    private static final String EXPORT_COMPLIANCE = "Export Compliance";
    private static final String PAYMENT_ERROR = "Payment Error";
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckPelicanEmailClient.class.getSimpleName());

    /**
     * Contructor which will get Email params from environmentVariables file
     *
     * @param environmentVariables : PelicanEnvironment
     */
    public CheckPelicanEmailClient(final EnvironmentVariables environmentVariables) {
        LOGGER.info("CheckPelicanEmail constructor");
        CheckPelicanEmailClient.environmentVariables = environmentVariables;
    }

    /**
     * Method to cleanup old emails from mailbox
     */
    public static void mailBoxCleanup() {

        int mailReadIndex;
        int totalMails;
        int counter = 0;
        LOGGER.info("Cleaning up QA mail box...");

        try {
            // create properties field
            final Properties properties = new Properties();
            properties.put("mail.imap.host", environmentVariables.getImapHost());
            properties.put("mail.imap.port", mailPort);
            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imaps.ssl.trust", "*");
            final Session emailSession = Session.getDefaultInstance(properties);

            // create the IMAP store object and connect with the pop server
            final Store store = emailSession.getStore(environmentVariables.getEmailStoreType());

            store.connect(environmentVariables.getImapHost(), environmentVariables.getEmailUsername(),
                environmentVariables.getEmailPassword());

            // create the folder object and open it
            final Folder emailFolder = store.getFolder(getMailFolder);

            totalMails = emailFolder.getMessageCount();

            // Leaving 250 email out in mail box all time
            mailReadIndex = totalMails - 250;

            emailFolder.open(Folder.READ_WRITE);

            // retrieve the messages (Start, end) from the open folder in to an
            // Message array. Note: Message array starts from "1", but NOT "0".
            final Message[] messages = emailFolder.getMessages(1, mailReadIndex);

            LOGGER.info("Total # Emails under " + environmentVariables.getEmailUsername() + " are : " + totalMails);

            final String nowMinusTwoDays = DateTimeUtils.getNowMinusDays(2);
            final Date targetDate =
                new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH).parse(nowMinusTwoDays);

            for (int loop = 0; loop <= mailReadIndex; loop++) {
                final Message message = messages[loop];
                // capture current date minus 2 days
                Date receivedDateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_RECEIVED_MAILBOX)
                    .parse(message.getReceivedDate().toString());
                final String receivedDate =
                    new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH).format(receivedDateFormat);
                receivedDateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH).parse(receivedDate);

                if (receivedDateFormat.compareTo(targetDate) < 0) {
                    counter++;
                    message.setFlag(Flags.Flag.DELETED, true);
                } else {
                    LOGGER.info("Received date format is equal to target date so breaking the loop.");
                    break;
                }
            }

            // close the store and folder objects with argument "True" means
            // delete the messages which are flagged for delete
            emailFolder.close(true);
            store.close();
            LOGGER.info("DELETED " + counter + " email from Pelican ssttest mail box");
        } catch (MessagingException | ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * This Method would get Mails from Inbox of the ssttest@mail.autodesk.com and filter first 500 mails with the
     * arguments of this mail and return the mail body (if found)
     *
     * @param mailSubject : Subject of the Mail you are looking for
     * @param mailRecipient : Recipient of the Mail you are looking for
     * @param validKeyMailElement : Key Text which SHOULD be part of the mail
     */
    private static String getMail(final String mailSubject, final String mailRecipient,
        final String validKeyMailElement) {
        String mailContent = null;
        String addressString;
        boolean isMailFound = false;
        int totalMails;
        int mailReadIndex;
        try {
            // create properties field
            final Properties properties = new Properties();
            properties.put("mail.imap.host", environmentVariables.getImapHost());
            properties.put("mail.imap.port", mailPort);
            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imaps.ssl.trust", "*");
            final Session emailSession = Session.getDefaultInstance(properties);

            // create the IMAP store object and connect with the pop server
            final Store store = emailSession.getStore(environmentVariables.getEmailStoreType());

            store.connect(environmentVariables.getImapHost(), environmentVariables.getEmailUsername(),
                environmentVariables.getEmailPassword());

            // create the folder object and open it
            final Folder emailFolder = store.getFolder(getMailFolder);

            // NOTE: getMessageCount will only work if the mail folder is in
            // Close state
            totalMails = emailFolder.getMessageCount();

            // Fetch only totalMails count of 1000
            mailReadIndex = totalMails - 1000;

            emailFolder.open(Folder.READ_WRITE);

            // retrieve the messages (Start, end) from the open folder in to an
            // Message array. Note: Message array starts from "1", but NOT "0".
            final Message[] messages = emailFolder.getMessages(mailReadIndex, totalMails);

            LOGGER.info("Total # Emails under " + environmentVariables.getEmailUsername() + " are : " + totalMails);

            LOGGER.info("Searching for Email with: Subject - " + mailSubject + ", Email Recipient - " + mailRecipient
                + ", KeyElement - " + validKeyMailElement);

            // Loops starts from latest email order, starting with 350th mail
            for (int i = (totalMails - mailReadIndex); i >= 0 && !isMailFound; i--) {

                final Message message = messages[i];
                Address[] recipients;
                recipients = message.getAllRecipients();
                for (final Address address : recipients) {
                    addressString = address.toString();

                    // Do Not Delete this piece of code, this helps in debug issues
                    // LOGGER.info("Message " + loop + " Subject :"
                    // + message.getSubject()
                    // + message.getSubject().contains(mailSubject)
                    // + "\n receipient:" + addressString
                    // + addressString.contains(mailRecipient) + "\n body:"
                    // + message.getContent().toString());

                    if (addressString != null && message.getSubject().contains(mailSubject)
                        && addressString.contains(mailRecipient)
                        && message.getContent().toString().contains(validKeyMailElement)) {

                        LOGGER.info("---------------------------------");
                        LOGGER.info("Email Number " + (i));
                        LOGGER.info("All Recipients: " + addressString);
                        LOGGER.info("Subject: " + message.getSubject());
                        LOGGER.info("From: " + message.getFrom()[0]);
                        LOGGER.info("Date: " + message.getReceivedDate());

                        mailContent = message.getContent().toString();
                        LOGGER
                            .debug("############################ Start of Mail Body ############################# \n\n"
                                + mailContent
                                + "\n\n############################ End of Mail Body ############################# ");

                        // if email found, break the loop, Happy deleting
                        isMailFound = true;
                        break;
                    }
                }
            }

            // close the store and folder objects with argument "True" means
            // delete the messages which are flagged for delete
            emailFolder.close(true);
            store.close();

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
        return mailContent;
    }

    /**
     * Helper method for Auto Renewal Mail validation
     *
     * @param purchaseOrderID , Order ID
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void autoRenewal(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist) {

        Util.waitInSeconds(TimeConstants.LONG_WAIT);

        mailBody = getMail(EmailConstants.AUTO_RENEWAL_COMPLETE, recipientMail, purchaseOrderID);
        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "AutoRenewal mail validations failed");
        } else {
            Assert.fail("Failed to find AutoRenewal mail in mail box for PO : " + purchaseOrderID);
        }

    }

    /**
     * Helper method for Auto Renewal Reminder Mail validation
     *
     * @param validKeyMailElement
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void renewalReminder(final String validKeyMailElement, final String recipientMail,
        final List<String> validationChecklist, final String subjectOfEmail) {

        mailBody = getMail(subjectOfEmail, recipientMail, validKeyMailElement);

        validationChecklist.add(EmailConstants.AUTO_RENEWAL_REMINDER_BODY);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Auto Renewal Reminder mail validations failed");
        } else {
            Assert.fail("Failed to find Auto Renewal Reminder mail in mail box");
        }
    }

    /**
     * Helper method for Auto Renewal Cancel Mail validation
     *
     * @param purchaseOrderID , Order ID
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     * @param deleteMail , flag which will decide the email "to be" or "not to be" deleted after reading
     */
    public static void cancelAutomaticBilling(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist, final Boolean deleteMail) {
        mailBody = getMail(EmailConstants.AUTO_RENEWAL_CANCELLED_PRE + validationChecklist.get(0)
            + EmailConstants.AUTO_RENEWAL_CANCELLED_PRE, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Auto Renewal Cancel mail validations failed");
        } else {
            Assert.fail("Failed to find Cancel Automatic billing mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * Helper method for Auto Renewal Restart Mail validation
     *
     * @param recipientMail
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void restartAutoEmail(final String recipientMail, final String productName,
        final ArrayList<String> validationChecklist, final String subscriptionId) {
        mailBody = getMail(EmailConstants.AUTO_RENEWAL_RESTART + productName, recipientMail, subscriptionId);

        validationChecklist.add(EmailConstants.AUTO_RENEWAL_RESTART_BODY);
        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Restart Auto Renewal mail validations failed");
        } else {
            Assert.fail("Failed to find Auto Renewal Restart mail in mail box");
        }
    }

    /**
     * Helper method for Credit Card Failure Mail validation
     * <p>
     * NOTE: validationsChecklist should be strong to find the email template
     *
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     * @param deleteMail , flag which will decide the email "to be" or "not to be" deleted after reading
     */
    public static void creditCardFailure(final String recipientMail, final ArrayList<String> validationChecklist,
        final Boolean deleteMail) {
        mailBody = getMail(EmailConstants.CREDIT_CARD_FAILURE, recipientMail, null);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Credit Card Failure mail validations failed");
        } else {
            Assert.fail("Failed to find Credit Card Failure mail in mail box");
        }
    }

    /**
     * Helper method for Credit Note Memo Mail validation
     *
     * @param purchaseOrderID , Order ID
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void creditNoteMemo(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(EmailConstants.CREDIT_NOTE_MEMO, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Credit Note Memo mail validations failed");
        } else {
            Assert.fail("Failed to find Credit Note Memo mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * Helper method for Invoice Mail validation
     *
     * @param purchaseOrderID , Order ID
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     * @param deleteMail , flag which will decide the email "to be" or "not to be" deleted after reading
     */
    public static void invoice(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist, final Boolean deleteMail) {
        mailBody = getMail(EmailConstants.INVOICE, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Invoice mail validations failed");
        } else {
            Assert.fail("Failed to find Invoice mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * Helper method for Invoice Mail validation
     *
     * @param purchaseOrderID , Order ID
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void orderComplete(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(EmailConstants.ORDER_COMPLETE, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Order Complete mail validations failed");
        } else {
            Assert.fail("Failed to find Order Complete mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * Helper method for Extension Order Complete Mail.
     *
     * @param purchaseOrderID , Order ID
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void extensionOrderComplete(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(EmailConstants.EXTENSION_ORDER_COMPLETE, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Extension Order Complete mail validations failed");
        } else {
            Assert.fail("Failed to find Extension Order Complete mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * This method to validate Add Seats Order Complete email
     *
     * @param purchaseOrderID
     * @param recipientMail
     * @param validationChecklist
     */
    public static void addSeatsOrderComplete(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(EmailConstants.ADD_SEATS_ORDER_COMPLETE, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Add Seats Order Complete mail validations failed");
        } else {
            Assert.fail("Failed to find Add Seats Order Complete mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * Helper method for SCF Mail validation
     *
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */

    public static void scfEmail(final String recipientMail, final List<String> validationChecklist) {

        if (validationChecklist.size() > 0) {
            mailBody = getMail(EmailConstants.SCF_EMAIL_SUBJECT, recipientMail, validationChecklist.get(0));
        } else {
            Assert.fail("SCF email validation checklist is empty");
        }

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true, "SCF email validations failed");
        } else {
            Assert.fail("Failed to find SCF mail in mail box");
        }
    }

    /**
     * Helper method for Order Fulfillment Mail validation
     *
     * @param purchaseOrderID , Order ID
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void orderFulfillment(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(EmailConstants.ORDER_FULFILLMENT, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Order Fulfillment mail validations failed");
        } else {
            Assert.fail("Failed to find Order Fulfillment mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * Helper method for Payment method changed Mail validation
     *
     * @param productName
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void paymentMethodChanged(final String productName, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(EmailConstants.PAYMENT_METHOD_CHANGED + productName, recipientMail, productName);

        validationChecklist.add(EmailConstants.PAYMENT_METHOD_CHANGED_BODY);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Payment Method changed mail validations failed");
        } else {
            Assert.fail("Failed to find Payment Method Changed mail in mail box");
        }
    }

    /**
     * Helper method for Refund Confirmation Mail validation
     *
     * @param purchaseOrderID , Order ID
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void refundConfirmation(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(EmailConstants.REFUND_CONFIRMATION, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Refund Confirmation mail validations failed");
        } else {
            Assert.fail("Failed to find Refund Confirmation mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * Helper method for Subscription Expired Mail validation
     *
     * @param purchaseOrderID , Order ID
     * @param recipientMail , email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     * @param deleteMail , flag which will decide the email "to be" or "not to be" deleted after reading
     */
    public static void subscriptionExpired(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist, final Boolean deleteMail) {
        mailBody = getMail(EmailConstants.SUBSCRIPTION_EXPIRED, recipientMail, null);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Subscription Expired mail validations failed");
        } else {
            Assert.fail("Failed to find Subscription Expired mail in mail box");
        }
    }

    /**
     * Helper method for Order declined email due to EC status validation
     *
     * @param mailSubject : unique word which exists in subject of an email (in this case product line is a keyWord)
     * @param recipientMail : email address of the user who has placed the order
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     */
    public static void declinedOrder(final String mailSubject, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(mailSubject, recipientMail, EXPORT_COMPLIANCE);
        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Declined order mail validations failed");
        } else {
            Assert.fail("Failed to find Declined Order due to EC status mail in mail box");
        }
    }

    /**
     * Helper method for Delinquent Order/ Payment Error email.
     *
     * @param mailSubject
     * @param recipientMail
     * @param validationChecklist
     * @param subscriptionId TODO
     */
    public static void delinquentOrder(final String mailSubject, final String recipientMail,
        final ArrayList<String> validationChecklist, final String subscriptionId) {
        mailBody = getMail(mailSubject, recipientMail, subscriptionId);
        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Payment Error Mail Validations Failed.");
        } else {
            Assert.fail("Failed to find Payment Error Mail.");
        }
    }

    /**
     * Validate Mail Body with the list in the Arraylist
     *
     * @param mailBody : Complete Mail body which is found from GetMail
     * @param validationChecklist : ArrayList which would have all the must have content for the email
     * @return True for success , False for failure in assertion
     */
    private static boolean validateMailBody(final String mailBody, final List<String> validationChecklist) {
        LOGGER.info("Starting validation for mail");
        for (final String validationCheck : validationChecklist) {
            if (!(mailBody.contains(validationCheck))) {
                LOGGER.info("Failed to find: " + validationCheck);
                return false;
            } else {
                LOGGER.info("Found " + validationCheck + " as part of the Email Body");
            }
        }
        return true;
    }

    /**
     * This method to validate Add Seats Invoice email
     *
     * @param purchaseOrderID
     * @param recipientMail
     * @param validationChecklist
     */
    public static void addSeatsInvoice(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(EmailConstants.INVOICE, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Add Seats Invoice mail validations failed");
        } else {
            Assert.fail("Failed to find Add Seats Invoice mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * This method to validate Add Seats Credit Note email
     *
     * @param purchaseOrderID
     * @param recipientMail
     * @param validationChecklist
     */
    public static void addSeatsCreditNote(final String purchaseOrderID, final String recipientMail,
        final ArrayList<String> validationChecklist) {
        mailBody = getMail(EmailConstants.CREDIT_NOTE_MEMO, recipientMail, purchaseOrderID);

        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Add Seats Credit Note mail validations failed");
        } else {
            Assert.fail("Failed to find Add Seats Credit Note mail in mail box for PO : " + purchaseOrderID);
        }
    }

    /**
     * Method to validate expiration reminder email.
     *
     * @param expirationDate
     * @param recipientMail
     * @param validationChecklist
     * @param subjectOfEmail
     */
    public static void expirationReminder(final String expirationDate, final String recipientMail,
        final List<String> validationChecklist) {
        mailBody = getMail(EmailConstants.EXPIRATION_REMINDER_SUBJECT, recipientMail, expirationDate);
        LOGGER.info("mailbody " + mailBody);
        validationChecklist.add(EmailConstants.EXPIRATION_REMINDER_BODY);
        if (mailBody != null) {
            Assert.assertEquals(validateMailBody(mailBody, validationChecklist), true,
                "Expiration Reminder mail validations failed");
        } else {
            Assert.fail("Failed to find Expiration Reminder mail in mail box.");
        }
    }

}
