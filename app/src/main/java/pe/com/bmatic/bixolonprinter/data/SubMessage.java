package pe.com.bmatic.bixolonprinter.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.regex.Pattern;

import pe.com.bmatic.bixolonprinter.functions.WritterPaper;

public class SubMessage {

    private WritterPaper writter = new WritterPaper("", "");

    private static final String PATH_IMG = "/storage/emulated/0/113_TicketeraWeb/assets/files/";

    private String prefix;
    private String submessage;

    SubMessage(String submessage) {
        this.submessage = submessage;
        this.prefix = this.submessage.substring(0, 2);

    }

    public void separarSubTrama() {
        if (this.isImage()) {
            this.setImage();
        } else if (isCutPaper()) {
            this.cutPaper();
        } else if (isCodeBars()) {
            System.out.println("Write Bar Code");
        } else {
            isText();
        }
    }

    private boolean isImage() {
        return this.prefix.equals("IC");
    }

    private boolean isCutPaper() {
        return this.prefix.equals("CP");
    }

    private boolean isCodeBars() {
        String sub1 = this.submessage.substring(2);
        System.out.println("sub1 ---> " + sub1);
        String sub = sub1.trim();
        System.out.println("sub ---> " + sub);
        if (sub1.length() > 1) {
            String isFunction = sub.substring(0, 1);
            System.out.println("isFunction --> " + isFunction);
            if (isFunction.equals("#")) {
                if (sub.startsWith("#PRINT.BARCODE8")) {
                    String codeBars = sub.substring(15).trim();
                    this.writter.writeBarCode(codeBars);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private void setImage() {
        String image = submessage.substring(2).split(Pattern.quote("\\"))[0];
        File imageFile = new File(PATH_IMG.concat(image));
        if (imageFile.exists()) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);

            this.writter.setImage(bitmap);
        } else {
            System.out.println("Image not found");
        }
    }

    private void cutPaper() {
        this.writter.cutPaper();
        System.out.println("Cut Paper");
    }

    private void isText() {
        String style = this.prefix;
        String text = "";
        if (this.submessage.length() > 1) {
            text = this.submessage.substring(2).trim();
        }

        System.out.println("Style:" + style + "Text:" + text);
        this.writter.setStyle(style);
        this.writter.setText(text);
        this.writter.setAlignment();
        this.writter.setFontStyle();
    }

}
