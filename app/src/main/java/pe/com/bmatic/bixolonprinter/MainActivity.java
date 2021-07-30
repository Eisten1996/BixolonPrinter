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

    private Button btnPrinterPrint, btnPrinterPrint2;

    private int portType = BXLConfigLoader.DEVICE_BUS_USB;
    private String logicalName = BXLConfigLoader.PRODUCT_NAME_BK3_3;
    private String address = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnPrinterPrint = findViewById(R.id.btn_print);
        btnPrinterPrint.setOnClickListener(this);
        btnPrinterPrint2 = findViewById(R.id.btn_print2);
        btnPrinterPrint2.setOnClickListener(this);

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
            case R.id.btn_print:
                openPrint();
                printData();
                closePrint();
                break;
            case R.id.btn_print2:
                openPrint();
                printData2();
                closePrint();
                break;
        }
    }

    public void openPrint() {
        long TInit, TEnd, time;
        TInit = System.currentTimeMillis();
        getPrinterInstance().printerOpen(portType, logicalName, address, false);
        TEnd = System.currentTimeMillis();
        time = TEnd - TInit;
        System.out.println("Tiempo de demora conexion en milisegundos: " + time);
    }

    public void printData() {
        String text = "HOLA MUNDO";
        String data = "";
        getPrinterInstance().beginTransactionPrint();
        for (int i = 0; i < 10; i++) {
            data += getPrinterInstance().setAtrribute(i + 1, i) + text + " " + (i + 1) + "\n";
        }
        System.out.println("data--->" + data);
        getPrinterInstance().printText(data);
        getPrinterInstance().endTransactionPrint();
        getPrinterInstance().cutPaper();

        getPrinterInstance().ejectPaper(3);
    }

    public void printData2() {
        String text = "HOLA MUNDO";
        String data;
        getPrinterInstance().beginTransactionPrint();
        for (int i = 0; i < 10; i++) {
            data = getPrinterInstance().setAtrribute(i + 1, i) + text + " " + (i + 1) + "\n";
            System.out.println("data--->" + data);
            getPrinterInstance().printText(data);
        }
        getPrinterInstance().endTransactionPrint();
        getPrinterInstance().cutPaper();

        getPrinterInstance().ejectPaper(3);
    }

    public void closePrint() {
        getPrinterInstance().printerClose();
    }

    public class AppUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, final Throwable ex) {
            ex.printStackTrace();

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

}