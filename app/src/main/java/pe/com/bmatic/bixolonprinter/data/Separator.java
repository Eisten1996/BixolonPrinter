package pe.com.bmatic.bixolonprinter.data;

public class Separator {
    private String[] subMessages;


    public void separatorMessage(String message) {
        System.out.println("ENTRA A separatorMessage : " + message);
        char SEPARATOR = '\034';
        this.subMessages = message.split(String.valueOf(SEPARATOR));

    }

    public void separatorSubMessage() {
        for (String submessage : subMessages) {
            SubMessage subMessage = new SubMessage(submessage.trim());
            System.out.println("TEXTO:---->" + submessage);
            subMessage.separarSubTrama();
        }
    }
}
