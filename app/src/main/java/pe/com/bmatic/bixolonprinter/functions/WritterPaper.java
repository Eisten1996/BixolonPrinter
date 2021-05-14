package pe.com.bmatic.bixolonprinter.functions;

import android.graphics.Bitmap;

import pe.com.bmatic.bixolonprinter.MainActivity;

public class WritterPaper {

    private String style;
    private int alignment;
    private String text;

    public WritterPaper(String style, String text) {
        this.style = style;
        this.text = text;
    }

    public void setAlignment() {
        try {
            this.alignment = Integer.parseInt(style.substring(0, 1));
            System.out.println("setPosition:" + alignment);
        } catch (NumberFormatException e) {
            System.out.println("ERROR FORMAT:" + e.getMessage());
        }
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setFontStyle() {

        int position = 0;
        try {
            position = Integer.parseInt(style.substring(1, 2));
            System.out.println("setFontStyle:" + position);
        } catch (NumberFormatException e) {
            System.out.println("ERROR FORMAT:" + e.getMessage());
        }

        MainActivity.getPrinterInstance().printText(this.text + "\n", this.alignment, position);
    }

    public void setText(String text) {
        this.text = text;
    }


    public void setImage(Bitmap bitmap) {
        MainActivity.getPrinterInstance().printImage(bitmap, 2);
        MainActivity.getPrinterInstance().printText("\n", this.alignment, 2);

    }

    public void writeBarCode(String codeBars) {
        MainActivity.getPrinterInstance().printBarcode(codeBars);
    }

    public void cutPaper() {
        MainActivity.getPrinterInstance().cutPaper();
    }

}
