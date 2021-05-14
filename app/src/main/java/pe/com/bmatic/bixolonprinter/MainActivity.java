package pe.com.bmatic.bixolonprinter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bixolon.commonlib.BXLCommonConst;
import com.bixolon.commonlib.log.LogService;
import com.bxl.config.editor.BXLConfigLoader;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static BixolonPrinter bxlPrinter = null;

    private Button btnPrinterOpen;
    private Button btnPrinterPrint;
    private Button btnPrinterClose;

    private int portType = BXLConfigLoader.DEVICE_BUS_USB;
    private String logicalName = BXLConfigLoader.PRODUCT_NAME_BK3_3;
    private String address = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnPrinterOpen = findViewById(R.id.btnPrinterOpen);
        btnPrinterOpen.setOnClickListener(this);
        btnPrinterPrint = findViewById(R.id.btn_print);
        btnPrinterPrint.setOnClickListener(this);
        btnPrinterClose = findViewById(R.id.btn_close);
        btnPrinterClose.setOnClickListener(this);


        bxlPrinter = new BixolonPrinter(getApplicationContext());

        Thread.setDefaultUncaughtExceptionHandler(new AppUncaughtExceptionHandler());

        String strLogPath = "";
        File[] mediaDirs = MainActivity.this.getExternalMediaDirs();
        if (mediaDirs != null && mediaDirs.length > 0) {
            strLogPath = mediaDirs[0].getPath();
        }
        if (strLogPath.length() > 0) {
            File dir = new File(strLogPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            LogService.InitDebugLog(true, true, BXLCommonConst._LOG_LEVEL_HIGH, 128, 128, (1024 * 1024) * 10, 0, strLogPath, "bixolon.log");
        }
    }

    public static BixolonPrinter getPrinterInstance() {
        return bxlPrinter;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPrinterOpen:
                getPrinterInstance().printerOpen(portType, logicalName, address, true);
                break;
            case R.id.btn_print:
                String data = "10  \u001CICLogo_bk.jpg\\\\  \u001C20 POLICLINICO METROPOLITAÑO \u001C10  \u001C20FECHA :  25/03/2021 \u001C10  \u001C20 FARMACIA \u001C10  \u001C28 FC3 \u001C10  \u001C10 POR FAVOR, FIRME Y COLOQUE SU NÚMERO DE DNI Y \u001C10 TELÉFONO EN LA RECETA, ANTES DE SER LLAMADO. \u001C10 \u001C## #PRINT.BARCODE81234567  \u001CCP";


                getPrinterInstance().printer(data);


//                getPrinterInstance().printText("HELLO WORD\n",2,0,1);
//                getPrinterInstance().posPrinterCheckHealth();

//                printBottomToTop();

//                printPageModeSample();
                break;
            case R.id.btn_close:
                getPrinterInstance().printerClose();
                break;
        }
    }

    public class AppUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, final Throwable ex) {
            ex.printStackTrace();

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

//    private void printLeftToRight() {
//        int width = getPrinterInstance().getPrinterMaxWidth();
//        int x = 5, y = 50;
//        while (true) {
//            // Position x, y
//            getPrinterInstance().setPageModePosition(x, y);
//            getPrinterInstance().printText("X : " + x + ", Y : " + y, 0, 0, 1);
//
//            if (y >= 1250 || x >= width) {
//                break;
//            }
//            System.out.println("X:\t" + x + "Y:\t" + y + "\n");
//            x += 10;
//            y += 50;
//        }
//    }
//
//    private void printBottomToTop() {
//        int width = getPrinterInstance().getPrinterMaxWidth();
//        int x = 5, y = 50;
//        while (true) {
//            // Position x, y
//            getPrinterInstance().setPageModePosition(x, y);
//            getPrinterInstance().printText("X : " + x + ", Y : " + y, 0, 0, 1);
//
//            if (y >= width || x >= 1250) {
//                break;
//            }
//
//            x += 50;
//            y += 50;
//        }
//    }
//
//    private void printPageModeSample() {
//        int xPos = 0, yPos = 0;
//        int width = getPrinterInstance().getPrinterMaxWidth();
//        int height = 1300;
//
//        getPrinterInstance().beginTransactionPrint();
//        getPrinterInstance().startPageMode(xPos, yPos, width, height, 2);
//        int x = 5, y = 50;
//        while (true) {
//            // Position x, y
//            getPrinterInstance().setPageModePosition(x, y);
//            getPrinterInstance().printText("X : " + x + ", Y : " + y, 0, 0, 1);
//
//            if (y >= width || x >= 1250) {
//                break;
//            }
//
//            x += 50;
//            y += 50;
//        }
//
//        getPrinterInstance().endPageMode(true);
//        getPrinterInstance().endTransactionPrint();
//
//    }


}