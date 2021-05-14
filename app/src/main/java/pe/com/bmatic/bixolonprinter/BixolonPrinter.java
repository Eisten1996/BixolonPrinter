package pe.com.bmatic.bixolonprinter;

import android.content.Context;
import android.graphics.Bitmap;

import com.bxl.BXLConst;
import com.bxl.config.editor.BXLConfigLoader;

import java.nio.ByteBuffer;

import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;

public class BixolonPrinter {

    private Context context;

    private BXLConfigLoader bxlConfigLoader;
    private POSPrinter posPrinter;

    // ------------------- alignment ------------------- //
    public static int ALIGNMENT_LEFT = 1;
    public static int ALIGNMENT_CENTER = 2;
    public static int ALIGNMENT_RIGHT = 3;

    public BixolonPrinter(Context context) {
        this.context = context;

        posPrinter = new POSPrinter(this.context);

        bxlConfigLoader = new BXLConfigLoader(this.context);
        try {
            bxlConfigLoader.openFile();
        } catch (Exception e) {
            bxlConfigLoader.newFile();
        }
    }

    public boolean printerOpen(int portType, String logicalName, String address, boolean isAsyncMode) {
        if (setTargetDevice(portType, logicalName, BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER, address)) {
            try {
                posPrinter.open(logicalName);
                posPrinter.claim(5000 * 2);
                posPrinter.setDeviceEnabled(true);
                posPrinter.setCharacterSet(BXLConst.CS_858_EURO);
                posPrinter.setCharacterEncoding(BXLConst.CE_ASCII);
                posPrinter.setAsyncMode(isAsyncMode);

            } catch (JposException e) {
                e.printStackTrace();
                try {
                    posPrinter.close();

                } catch (JposException e1) {
                    System.out.println("Error -> " + e1.getMessage());
                }
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public boolean printerClose() {
        try {
            if (posPrinter.getClaimed()) {
                posPrinter.setDeviceEnabled(false);
                posPrinter.close();
            }
        } catch (JposException e) {
            System.out.println("Error -> " + e.getMessage());
        }
        return true;
    }

    private boolean setTargetDevice(int portType, String logicalName, int deviceCategory, String address) {
        try {
            for (Object entry : bxlConfigLoader.getEntries()) {
                JposEntry jposEntry = (JposEntry) entry;
                if (jposEntry.getLogicalName().equals(logicalName)) {
                    bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
                }
            }
            bxlConfigLoader.addEntry(logicalName, deviceCategory, BXLConfigLoader.PRODUCT_NAME_BK3_3, portType, address);
            bxlConfigLoader.saveFile();
        } catch (Exception e) {
            System.out.println("Error -> " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean printText(String data) {
        boolean ret = true;
        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, data);
        } catch (JposException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    public String setAtrribute(int alignment, int attribute) {
        String strOption = EscapeSequence.getString(0);

        if ((alignment & ALIGNMENT_LEFT) == ALIGNMENT_LEFT) {
            strOption += EscapeSequence.getString(4);
        }
        if ((alignment & ALIGNMENT_CENTER) == ALIGNMENT_CENTER) {
            strOption += EscapeSequence.getString(5);
        }
        if ((alignment & ALIGNMENT_RIGHT) == ALIGNMENT_RIGHT) {
            strOption += EscapeSequence.getString(6);
        }
        switch (attribute) {
            case 0:
                // TIPO A
                strOption += EscapeSequence.getString(1);
                break;
            case 1:
                // TIPO B
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(7);
                break;
            case 2:
                // TIPO A DOUBLE HIGHT
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(26);
                break;
            case 3:
                // TIPO B DOUBLE HIGHT
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(26);
                strOption += EscapeSequence.getString(7);
                break;
            case 4:
                // TIPO A DOUBLE WIDHT
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(18);
                break;
            case 5:
                // TIPO B DOUBLE WIDHT
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(18);
                strOption += EscapeSequence.getString(7);
                break;
            case 6:
                // TIPO A QUADRUPLE
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(26);
                strOption += EscapeSequence.getString(18);
                break;
            case 7:
                // TIPO B QUADRUPLE
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(26);
                strOption += EscapeSequence.getString(18);
                strOption += EscapeSequence.getString(7);
                break;
            case 8:
                // TYPE C DOUBLE WIDHT
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(20);
                strOption += EscapeSequence.getString(27);
                strOption += EscapeSequence.getString(7);
                break;
            case 9:
                // TYPE C DOUBLE HIGHT
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(20);
                strOption += EscapeSequence.getString(30);
                strOption += EscapeSequence.getString(7);
                break;
            case 10:
                // TYPE C QUADRUPLE
                strOption += EscapeSequence.getString(1);
                strOption += EscapeSequence.getString(20);
                strOption += EscapeSequence.getString(27);
                break;
        }
        return strOption;
    }

    public boolean cutPaper() {
        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }
            String cutPaper = EscapeSequence.ESCAPE_CHARACTERS + String.format("%dfP", 100);
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, cutPaper);
        } catch (JposException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
