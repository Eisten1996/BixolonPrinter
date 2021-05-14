package pe.com.bmatic.bixolonprinter;

import android.content.Context;
import android.graphics.Bitmap;

import com.bxl.BXLConst;
import com.bxl.config.editor.BXLConfigLoader;

import java.nio.ByteBuffer;

import jpos.JposConst;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;
import jpos.events.StatusUpdateEvent;
import jpos.events.StatusUpdateListener;
import pe.com.bmatic.bixolonprinter.data.Separator;

public class BixolonPrinter implements StatusUpdateListener {

    private Context context;

    private BXLConfigLoader bxlConfigLoader;
    private POSPrinter posPrinter;
    private Separator separator = new Separator();

    private static final String TAG = "PrinterBixolon";

    private static final int SUCCESS = 0;
    public static final int ERR_NO_RESPONSE = -1;
    private static final int ERR_RECEIPT_END = 2;
    private static final int ERR_PAPER_NEAR_END = 1;

    private StatusUpdateEvent status;

    // ------------------- alignment ------------------- //
    public static int ALIGNMENT_LEFT = 1;
    public static int ALIGNMENT_CENTER = 2;
    public static int ALIGNMENT_RIGHT = 4;

    public BixolonPrinter(Context context) {
        this.context = context;

        posPrinter = new POSPrinter(this.context);
        posPrinter.addStatusUpdateListener(this);

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
                    // logger.info("Error -> " + e1.getMessage());
                    this.setStatusPrinter(ERR_NO_RESPONSE);
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
            // logger.info("Error -> " + e.getMessage());
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
            // logger.info("Error -> " + e.getMessage());
            this.setStatusPrinter(ERR_NO_RESPONSE);
            return false;
        }

        return true;
    }

    public boolean printImage(Bitmap bitmap, int alignment) {
        boolean ret = true;

        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

            if (alignment == ALIGNMENT_LEFT) {
                alignment = POSPrinterConst.PTR_BM_LEFT;
            } else if (alignment == ALIGNMENT_CENTER) {
                alignment = POSPrinterConst.PTR_BM_CENTER;
            } else {
                alignment = POSPrinterConst.PTR_BM_RIGHT;
            }

            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.put((byte) POSPrinterConst.PTR_S_RECEIPT);
            buffer.put((byte) 80); // brightness
            buffer.put((byte) 0x01); // compress
            buffer.put((byte) 0x00); // dither

            posPrinter.printBitmap(buffer.getInt(0), bitmap, posPrinter.getRecLineWidth() / 2, alignment);
        } catch (JposException e) {
            e.printStackTrace();

            ret = false;
        }

        return ret;
    }

    public boolean printBarcode(String data) {
        boolean ret = true;

        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

            posPrinter.printBarCode(POSPrinterConst.PTR_S_RECEIPT, data, POSPrinterConst.PTR_BCS_Code128, 150, 8, POSPrinterConst.PTR_BC_CENTER, POSPrinterConst.PTR_BC_TEXT_NONE);
        } catch (JposException e) {
            e.printStackTrace();
            ret = false;
        }

        return ret;
    }

    public boolean printText(String data, int alignment, int attribute) {
        boolean ret = true;

        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

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
                case 1:
                    strOption += EscapeSequence.getString(1);
                    break;
                case 2:
                    strOption += EscapeSequence.getString(2);
                    break;
                case 3:
                    strOption += EscapeSequence.getString(1);
                    strOption += EscapeSequence.getString(26);
                    break;
                case 4:
                    strOption += EscapeSequence.getString(2);
                    strOption += EscapeSequence.getString(26);
                    break;
                case 5:
                    strOption += EscapeSequence.getString(1);
                    strOption += EscapeSequence.getString(18);
                    break;
                case 6:
                    strOption += EscapeSequence.getString(2);
                    strOption += EscapeSequence.getString(18);
                    break;
                case 7:
                    strOption += EscapeSequence.getString(1);
                    strOption += EscapeSequence.getString(26);
                    strOption += EscapeSequence.getString(18);

                    break;
                case 8:
                    strOption += EscapeSequence.getString(2);
                    strOption += EscapeSequence.getString(26);
                    strOption += EscapeSequence.getString(18);
                    break;
                case 9:
                    strOption += EscapeSequence.getString(3);
                    strOption += EscapeSequence.getString(28);
                    break;
                case 10:
                    strOption += EscapeSequence.getString(3);
                    strOption += EscapeSequence.getString(18);
                    break;
                default:
                    strOption += EscapeSequence.getString(17);
                    strOption += EscapeSequence.getString(25);
                    break;
            }

            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, strOption + data);

        } catch (JposException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
//            switch (e.getErrorCodeExtended()){
//                case POSPrinterConst.JPOS_EPTR_COVER_OPEN:
//                    System.out.println("Cover open");
//                case POSPrinterConst.JPOS_EPTR_REC_EMPTY:
//                    System.out.println("Paper empty");
//                case JposConst.JPOS_SUE_POWER_OFF_OFFLINE:
//                    System.out.println("Power off");
//                default:
//                    System.out.println("Unknown");
//            }
            ret = false;
        }

        return ret;
    }

    public boolean cutPaper() {
        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

            String cutPaper = EscapeSequence.ESCAPE_CHARACTERS + String.format("%dfP", 100);  // Feed Full Cut
//            String cutPaper = EscapeSequence.ESCAPE_CHARACTERS + String.format("%dfP", 90); // Feed Partial Cut
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, cutPaper);    // Execute Feed cut

            // posPrinter.cutPaper(90);    // Normal Partial Cut
            //posPrinter.cutPaper(100);   // Normal Full Cut
        } catch (JposException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public int print(String message) {
        try {
            int st = getStatus(status);
            if (!isPrintable(status)) {
                if (st != 0) {
                    return st;
                }
                return ERR_NO_RESPONSE;
            } else {
                separator.separatorMessage(message);
                separator.separatorSubMessage();
//                logger.info("END CHECKEDBLOCK: " + st);
                System.out.println("END CHECKEDBLOCK: " + st);
                return st;
            }

        } catch (Exception e) {
//            logger.info("JposException " + e.getMessage());
            System.out.println("Exception " + e.getMessage());
            return ERR_NO_RESPONSE;
        }
    }

    private boolean isPrintable(StatusUpdateEvent status) {
        if (status == null) {
//            logger.info("ISPRINTABLE: STATUS NULL");
            System.out.println("ISPRINTABLE: STATUS NULL");
            return false;
        }

        if (status.getStatus() != JposConst.JPOS_SUE_POWER_OFF_OFFLINE) {
//            logger.info("ISPRINTABLE: STATUS ONLINE");
            System.out.println("ISPRINTABLE: STATUS ONLINE");
            if (status.getStatus() == POSPrinterConst.PTR_SUE_REC_NEAREMPTY) {
//                logger.info("ISPRINTABLE: PAPERDETECTIONERROR");
                System.out.println("ISPRINTABLE: PAPERDETECTIONERROR");
                return false;
            }
            if (status.getStatus() == POSPrinterConst.PTR_SUE_COVER_OPEN) {
//                logger.info("ISPRINTABLE: COVEROPENERROR");
                System.out.println("ISPRINTABLE: COVEROPENERROR");
                return false;
            }
            if (status.getStatus() == POSPrinterConst.PTR_SUE_REC_EMPTY) {
//                logger.info("ISPRINTABLE: PAPERDETECTIONERROR");
                System.out.println("ISPRINTABLE: PAPERDETECTIONERROR");
                return false;
            }
            return true;
        }
        return false;
    }

    public int getStatus(StatusUpdateEvent status) {
        System.out.println("status.getStatus()--->" + status.getStatus());
        if (status.getStatus() == POSPrinterConst.PTR_SUE_COVER_OPEN) {
//            logger.info("COVER OPEN");
            System.out.println("COVER OPEN");
            return ERR_NO_RESPONSE;
        }
        if (status.getStatus() == POSPrinterConst.PTR_SUE_REC_EMPTY) {
//            logger.info("PAPER_EMPTY");
            System.out.println("PAPER_EMPTY");
            return ERR_RECEIPT_END;
        }
        if (status.getStatus() == POSPrinterConst.PTR_SUE_REC_NEAREMPTY) {
//            logger.info("PAPER_NEAR_END");
            System.out.println("PAPER_NEAR_END");
            return ERR_PAPER_NEAR_END;
        }
//        Log.d(TAG, "PRINT SUCCESS");
        System.out.println(TAG + "PRINT SUCCESS");
        return SUCCESS;
    }

    public void printer(String message) {
        int printerStatus = -1;
        MyCallBack myCallBack = new MyCallBack();
        printerStatus = print(message);
        System.out.println("Estado de la impresora bixolon -> " + printerStatus);
        myCallBack.myTaskDone(printerStatus);
//        logger.info("Estado de la impresora bixolon -> " + printerStatus);
//        myCallBack.myTaskDone(printerStatus);
    }

    @Override
    public void statusUpdateOccurred(StatusUpdateEvent statusUpdateEvent) {

        System.out.println("statusUpdateEvent ----> " + statusUpdateEvent.getStatus());
//        logger.info("statusUpdateEvent: " + statusUpdateEvent.getStatus());
        status = statusUpdateEvent;

    }

    private void setStatusPrinter(int printerStatus) {
//        this.plugin.method(printerStatus);
        this.printerClose();
    }

    public class MyCallBack {
        public void myTaskDone(int statusPrinter) {
            setStatusPrinter(statusPrinter);
        }
    }
}
